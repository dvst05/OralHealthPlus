package com.kelompok4.oralhealthplus.feature.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

private const val SISI_MAKS = 1024  // kecilkan foto besar agar hemat memori; model hanya butuh 224x224

/**
 * Uri foto (galeri atau kamera) -> Bitmap ARGB_8888 (software), sudah tegak. Jalan di Dispatchers.IO.
 * API 28+: ImageDecoder otomatis memutar sesuai EXIF.
 * API 24-27: orientasi EXIF dibaca dan diterapkan manual.
 */
suspend fun muatBitmap(context: Context, uri: Uri): Bitmap = withContext(Dispatchers.IO) {
    if (Build.VERSION.SDK_INT >= 28) {
        val sumber = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(sumber) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val sisi = max(info.size.width, info.size.height)
            if (sisi > SISI_MAKS) {
                val f = SISI_MAKS.toFloat() / sisi
                decoder.setTargetSize((info.size.width * f).roundToInt(), (info.size.height * f).roundToInt())
            }
        }
    } else {
        muatBitmapLama(context, uri)
    }
}

private fun muatBitmapLama(context: Context, uri: Uri): Bitmap {
    fun buka() = context.contentResolver.openInputStream(uri) ?: error("Tidak bisa membuka foto")

    // Baca ukurannya dulu supaya foto kamera yang besar tidak menghabiskan memori
    val ukuran = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    buka().use { BitmapFactory.decodeStream(it, null, ukuran) }

    val opsi = BitmapFactory.Options().apply {
        inPreferredConfig = Bitmap.Config.ARGB_8888
        inSampleSize = hitungSampleSize(ukuran.outWidth, ukuran.outHeight, SISI_MAKS)
    }
    val bitmap = buka().use { BitmapFactory.decodeStream(it, null, opsi) }
        ?: error("Format foto tidak didukung")

    val orientasi = buka().use {
        ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }
    val m = Matrix()
    when (orientasi) {
        ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> { m.postRotate(90f); m.postScale(-1f, 1f) }
        ExifInterface.ORIENTATION_TRANSVERSE -> { m.postRotate(270f); m.postScale(-1f, 1f) }
        else -> return bitmap
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
}

/** Pangkat dua terbesar yang masih menyisakan sisi terpanjang >= [sisiMaks]. */
internal fun hitungSampleSize(lebar: Int, tinggi: Int, sisiMaks: Int): Int {
    var s = 1
    while (max(lebar, tinggi) / (s * 2) >= sisiMaks) s *= 2
    return s
}
