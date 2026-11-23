package com.kelompok4.oralhealthplus.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kelompok4.oralhealthplus.R

// File font ada di res/font/ (poppins_light.ttf ... poppins_bold.ttf)
val Poppins = FontFamily(
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

// Ukuran diambil dari Figma, dibulatkan (mis. 15.186 -> 15, 12.655 -> 13).
val Typography = Typography(
    // Judul layar: "Result", "Gingivitis"
    headlineMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    // Judul bagian: "Learn", "More Feature", "Get Benefit from..."
    titleLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    // Judul subbagian: "Learn About Gingivitis"
    titleMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 15.sp),
    // Judul kartu: judul artikel, "Chatbot Consultant"
    titleSmall = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp),
    // Teks tombol: "Scan Now", "Finish"
    labelLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    // Teks chip: "Certainty 87%", "Routine Check-up"
    labelMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 11.sp),
    // Label kecil di kartu: "Article", "Video"
    labelSmall = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 8.sp),
    // Teks menu ikon: "Videos", "Articles"
    bodyLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    // Paragraf penjelasan penyakit
    bodyMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 10.sp),
    // Deskripsi tipis: "Discuss Anything About Oral Disease"
    bodySmall = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Light, fontSize = 10.sp),
)
