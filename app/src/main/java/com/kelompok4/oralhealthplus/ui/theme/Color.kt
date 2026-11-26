package com.kelompok4.oralhealthplus.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Semua warna diambil dari Figma HIFI (file "PAPB").
// Nama mengikuti PERAN warna, bukan nama warnanya, supaya mudah dipakai anggota lain.

// Warna utama
val BluePrimary = Color(0xFF4A628A)      // judul, teks utama, tombol "Try Again", tab aktif
val BlueLight = Color(0xFF7AB2D3)        // garis di bawah judul layar, ujung gradien tombol
val BlueDark = Color(0xFF3A5075)         // teks di dalam chip (Article, Certainty 87%)

// Latar
val White = Color(0xFFFFFFFF)            // latar semua layar dan kartu
val SurfaceBlue = Color(0xFFE7F6FF)      // kartu "Scan Now" di Home, garis tepi kartu menu
val ChipBlue = Color(0xFFE8F0F8)         // latar chip label (Article, Video, Certainty)

// Status
val GreenSuccess = Color(0xFF319F43)     // tombol "Finish"
val AmberSoft = Color(0xFFFFF1D2)        // latar chip urgensi "Routine Check-up"
val AmberText = Color(0xFF7A4E00)        // teks chip urgensi
val RedError = Color(0xFFE33629)         // tombol Logout (awal gradien) dan pesan error
val RedLight = Color(0xFFFF958D)         // tombol Logout (akhir gradien)

// Gradien tombol utama ("Scan Now"), dari kiri ke kanan
val PrimaryGradient = Brush.horizontalGradient(listOf(BluePrimary, BlueLight))

// Gradien tombol Logout di layar Profil, dari kiri ke kanan
val LogoutGradient = Brush.horizontalGradient(listOf(RedError, RedLight))

// Turunan warna Figma untuk kotak status (latar lembut). Tidak ada di Figma, dibuat dari warna statusnya.
val GreenSoft = Color(0xFFE6F4E8)        // latar kotak sukses / "Buka" (dari GreenSuccess)
val RedSoft = Color(0xFFFDECEA)          // latar kotak error / "Tutup" / darurat (dari RedError)

// Warna pendukung yang tidak ada di Figma
val StarAmber = Color(0xFFD97706)        // bintang rating klinik dan dokter
val GreyDisabled = Color(0xFFE5E5E5)     // latar tombol nonaktif
val GreyDisabledText = Color(0xFF9A9A9A) // teks tombol nonaktif
