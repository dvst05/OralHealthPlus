package com.kelompok4.oralhealthplus.feature.auth

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.kelompok4.oralhealthplus.feature.detection.muatBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object ProfilePhotoStorage {
    private const val AVATAR_DIR = "avatars"
    private const val MAX_AVATAR_PX = 512

    suspend fun saveProfilePhoto(
        context: Context,
        username: String,
        sourceUri: Uri,
        previousPath: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val rawBitmap = muatBitmap(context, sourceUri)
            val squareBitmap = centerCropAndScale(rawBitmap, MAX_AVATAR_PX)
            val dir = File(context.filesDir, AVATAR_DIR).apply { if (!exists()) mkdirs() }
            val safeUser = username.lowercase().replace(Regex("[^a-z0-9_]"), "_")
            val targetFile = File(dir, "avatar_${safeUser}_${System.currentTimeMillis()}.jpg")

            FileOutputStream(targetFile).use { out ->
                squareBitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }

            if (squareBitmap !== rawBitmap) squareBitmap.recycle()
            rawBitmap.recycle()

            deleteProfilePhotoFile(previousPath)
            Result.success(targetFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loadProfilePhotoBitmap(path: String): Bitmap? {
        if (path.isBlank()) return null
        val file = File(path)
        if (!file.exists() || !file.isFile) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (_: Exception) {
            null
        }
    }

    fun deleteProfilePhotoFile(path: String) {
        if (path.isBlank()) return
        try {
            val file = File(path)
            if (file.exists() && file.isFile) {
                file.delete()
            }
        } catch (_: Exception) {
            // Ignore cleanup failure
        }
    }

    private fun centerCropAndScale(source: Bitmap, targetSize: Int): Bitmap {
        val side = min(source.width, source.height)
        val x = (source.width - side) / 2
        val y = (source.height - side) / 2
        val cropped = Bitmap.createBitmap(source, x, y, side, side)
        if (side <= targetSize) return cropped
        val scaled = Bitmap.createScaledBitmap(cropped, targetSize, targetSize, true)
        if (scaled !== cropped) cropped.recycle()
        return scaled
    }
}
