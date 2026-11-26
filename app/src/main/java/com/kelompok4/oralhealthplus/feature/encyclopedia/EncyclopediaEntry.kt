package com.kelompok4.oralhealthplus.feature.encyclopedia

/**
 * Satu penyakit di ensiklopedia.
 * [id] HARUS sama persis dengan nama kelas di assets/labels.txt milik Anggota 1
 * (Calculus, Caries, Gingivitis, Hypodontia, Mouth_Ulcer, Tooth_Discoloration).
 */
data class EncyclopediaEntry(
    val id: String,
    val nama: String,
    val ringkasan: String,
    val gejala: List<String>,
    val pencegahan: List<String>,
    val kataKunci: List<String>,
    val sumber: String,
)
