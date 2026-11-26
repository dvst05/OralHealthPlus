package com.kelompok4.oralhealthplus.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Warna modul Chatbot & Deteksi
val OralPrimaryBlue = Color(0xFF4A628A)
val OralPrimaryLight = Color(0xFF7AB2D3)
val OralSecondarySoft = Color(0xFFB9E5E8)
val OralBackgroundLight = Color(0xFFF8FAFC)
val OralSurfaceWhite = Color(0xFFFFFFFF)
val OralTextPrimary = Color(0xFF0F172A)
val OralTextMuted = Color(0xFF475569)
val OralBorderHairline = Color(0xFFE2E8F0)
val OralErrorRed = Color(0xFF9B1C1C)

// Semua warna diambil dari Figma HIFI (file "PAPB") untuk modul Home, Onboarding, & Edukasi
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
val RedError = Color(0xFFD9534F)         // tombol Logout / pesan error

// Gradien tombol utama ("Scan Now"), dari kiri ke kanan
val PrimaryGradient = Brush.horizontalGradient(listOf(BluePrimary, BlueLight))