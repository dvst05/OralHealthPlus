package com.kelompok4.oralhealthplus.feature.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

private const val SISI_MAKS = 1024  // kecilkan foto besar agar hemat memori; model hanya butuh 224x224

/**
 * Uri galeri -> Bitmap ARGB_8888 (software). Jalan di Dispatchers.IO.
 * API 28+: ImageDecoder otomatis memutar sesuai EXIF. API < 28: orientasi EXIF belum
 * ditangani (tambahkan androidx.exifinterface jika minSdk < 28 dan foto sering terputar).
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
        val opsi = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opsi) }
            ?: error("Tidak bisa membuka foto")
    }
}
