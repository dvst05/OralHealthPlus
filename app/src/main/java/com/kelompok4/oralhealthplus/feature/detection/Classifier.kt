package com.kelompok4.oralhealthplus.feature.detection

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.exp

/*
 * SETUP (app/build.gradle.kts)
 *   dependencies { implementation("org.tensorflow:tensorflow-lite:2.14.0") }
 *   android { androidResources { noCompress += "tflite" } }   // wajib agar bisa di-mmap
 * Taruh di app/src/main/assets/ :
 *   oralhealth_mobilenetv2.tflite  dan  labels.txt   (keduanya dari Colab)
 */
class Classifier(
    context: Context,
    modelFile: String = "oralhealth_mobilenetv2.tflite",
    labelsFile: String = "labels.txt"
) : AutoCloseable {

    val labels: List<String> = context.assets.open(labelsFile).bufferedReader()
        .readLines().map { it.trim() }.filter { it.isNotEmpty() }

    private val interpreter: Interpreter = Interpreter(muatModel(context, modelFile))

    init {
        val jumlahKeluaran = interpreter.getOutputTensor(0).shape().last()
        require(jumlahKeluaran == labels.size) {
            "labels.txt berisi ${labels.size} kelas, model punya $jumlahKeluaran keluaran"
        }
    }

    /** Preprocess + inferensi di Dispatchers.Default (tidak memblokir main thread). */
    suspend fun classify(bitmap: Bitmap): Prediction = withContext(Dispatchers.Default) {
        val mulai = System.nanoTime()
        val input = preprocess(bitmap)
        val keluaran = Array(1) { FloatArray(labels.size) }
        synchronized(interpreter) { interpreter.run(input, keluaran) }  // Interpreter tidak thread-safe
        val probs = pastikanSoftmax(keluaran[0])
        val idx = probs.indices.maxByOrNull { probs[it] } ?: 0
        Prediction(labels[idx], probs[idx], probs, (System.nanoTime() - mulai) / 1_000_000)
    }

    /**
     * Sama dengan pipeline Colab: resize 224x224 (bilinear, tanpa crop),
     * urutan RGB, piksel 0-255 dibagi 255 -> 0-1, bentuk NHWC float32.
     */
    private fun preprocess(src: Bitmap): ByteBuffer {
        // Bitmap HARDWARE (dari kamera/ImageDecoder) tidak bisa dibaca pikselnya
        val aman = if (src.config == Bitmap.Config.HARDWARE) src.copy(Bitmap.Config.ARGB_8888, false) else src
        val kecil = Bitmap.createScaledBitmap(aman, UKURAN, UKURAN, true)
        val piksel = IntArray(UKURAN * UKURAN)
        kecil.getPixels(piksel, 0, UKURAN, 0, 0, UKURAN, UKURAN)
        val buf = ByteBuffer.allocateDirect(4 * UKURAN * UKURAN * 3).order(ByteOrder.nativeOrder())
        for (p in piksel) {
            buf.putFloat(((p shr 16) and 0xFF) / 255f)  // R
            buf.putFloat(((p shr 8) and 0xFF) / 255f)   // G
            buf.putFloat((p and 0xFF) / 255f)           // B
        }
        buf.rewind()
        return buf
    }

    /**
     * Model Keras sudah berakhir dengan softmax, jadi keluaran TFLite biasanya sudah
     * berupa probabilitas. Softmax hanya diterapkan jika jumlahnya bukan ~1 (logits).
     */
    private fun pastikanSoftmax(x: FloatArray): FloatArray {
        val sudahProb = x.all { it in 0f..1f } && kotlin.math.abs(x.sum() - 1f) < 1e-3f
        return if (sudahProb) x else softmax(x)
    }

    override fun close() = interpreter.close()

    companion object {
        const val UKURAN = 224

        /** Softmax stabil secara numerik (kurangi nilai maksimum dulu). */
        fun softmax(logits: FloatArray): FloatArray {
            val m = logits.max()
            val e = FloatArray(logits.size) { exp(logits[it] - m) }
            val total = e.sum()
            return FloatArray(e.size) { e[it] / total }
        }

        private fun muatModel(context: Context, nama: String): MappedByteBuffer =
            context.assets.openFd(nama).use { fd ->
                FileInputStream(fd.fileDescriptor).channel
                    .map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
            }
    }
}
