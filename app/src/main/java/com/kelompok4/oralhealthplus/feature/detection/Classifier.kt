package com.kelompok4.oralhealthplus.feature.detection

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.exp

data class Prediction(
    val kelas: String,
    val confidence: Float,
    val probabilitas: FloatArray,
    val waktuInferensiMs: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Prediction) return false
        return kelas == other.kelas &&
                confidence == other.confidence &&
                probabilitas.contentEquals(other.probabilitas) &&
                waktuInferensiMs == other.waktuInferensiMs
    }

    override fun hashCode(): Int {
        var result = kelas.hashCode()
        result = 31 * result + confidence.hashCode()
        result = 31 * result + probabilitas.contentHashCode()
        result = 31 * result + waktuInferensiMs.hashCode()
        return result
    }
}

class Classifier(
    context: Context,
    private val modelFile: String = "oralhealth_mobilenetv2.tflite",
    private val labelsFile: String = "labels.txt"
) : AutoCloseable {

    private val appContext: Context = context.applicationContext
    private val kunci = Any()
    private var interpreter: Interpreter? = null
    private var sudahDitutup = false

    val labels: List<String> by lazy {
        appContext.assets.open(labelsFile).bufferedReader().use { it.readLines() }
            .map { it.trim() }.filter { it.isNotEmpty() }
    }

    /**
     * Preprocess + inferensi di Dispatchers.Default (tidak memblokir main thread).
     * Model baru dimuat pada pemanggilan pertama, jadi kegagalan memuat model sampai ke
     * pemanggil sebagai exception (layar Error), bukan membuat aplikasi crash saat dibuka.
     * `waktuInferensiMs` hanya mengukur preprocess + inferensi, tanpa waktu memuat model.
     */
    suspend fun classify(bitmap: Bitmap): Prediction = withContext(Dispatchers.Default) {
        synchronized(kunci) {   // Interpreter tidak thread-safe, dan close() tidak boleh memotong run()
            val mesin = siapkanInterpreter()
            val mulai = System.nanoTime()
            val input = preprocess(bitmap)
            val keluaran = Array(1) { FloatArray(labels.size) }
            mesin.run(input, keluaran)
            val probs = pastikanSoftmax(keluaran[0])
            val idx = probs.indices.maxByOrNull { probs[it] } ?: 0
            Prediction(labels[idx], probs[idx], probs, (System.nanoTime() - mulai) / 1_000_000)
        }
    }

    private fun siapkanInterpreter(): Interpreter {
        check(!sudahDitutup) { "Classifier sudah ditutup" }
        interpreter?.let { return it }
        val baru = Interpreter(muatModel(appContext, modelFile))
        val jumlahKeluaran = baru.getOutputTensor(0).shape().last()
        if (jumlahKeluaran != labels.size) {
            baru.close()
            error("labels.txt berisi ${labels.size} kelas, model punya $jumlahKeluaran keluaran")
        }
        interpreter = baru
        return baru
    }

    /**
     * Sama dengan pipeline Colab: resize 224x224 (bilinear, tanpa crop),
     * urutan RGB, piksel 0-255 dibagi 255 -> 0-1, bentuk NHWC float32.
     */
    private fun preprocess(src: Bitmap): ByteBuffer {
        // Bitmap HARDWARE (dari kamera/ImageDecoder) tidak bisa dibaca pikselnya
        // Bitmap.Config.HARDWARE baru ada di API 26; tanpa cek versi, API 24-25 crash (NoSuchFieldError).
        val hardware = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && src.config == Bitmap.Config.HARDWARE
        val aman = if (hardware) src.copy(Bitmap.Config.ARGB_8888, false) else src
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

    override fun close() = synchronized(kunci) {
        sudahDitutup = true
        interpreter?.close()
        interpreter = null
    }

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
