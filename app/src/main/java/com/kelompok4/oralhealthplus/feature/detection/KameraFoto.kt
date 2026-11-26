package com.kelompok4.oralhealthplus.feature.detection

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

/** Harus sama dengan <cache-path> di res/xml/deteksi_file_paths.xml. */
private const val FOLDER_FOTO = "foto_deteksi"

/** File sementara di cache aplikasi, tempat aplikasi kamera menulis foto resolusi penuh. */
fun buatFileFotoKamera(context: Context): File {
    val folder = File(context.cacheDir, FOLDER_FOTO).apply { mkdirs() }
    folder.listFiles()?.forEach { it.delete() }   // sisa foto lama
    return File.createTempFile("mulut_", ".jpg", folder)
}

/** Harus sama dengan android:authorities pada <provider> di AndroidManifest.xml. */
fun uriFotoKamera(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.deteksi.fileprovider", file)

/**
 * Intent kamera tidak butuh izin CAMERA, KECUALI aplikasi mendeklarasikannya di manifest
 * tetapi belum diberi izin: saat itu membuka kamera melempar SecurityException.
 */
fun perluIzinKamera(context: Context): Boolean {
    val diizinkan = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
    if (diizinkan) return false
    @Suppress("DEPRECATION")
    val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
    return info.requestedPermissions?.contains(Manifest.permission.CAMERA) == true
}
