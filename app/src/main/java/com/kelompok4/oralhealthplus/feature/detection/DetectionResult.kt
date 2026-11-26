package com.kelompok4.oralhealthplus.feature.detection

/** Tingkat urgensi hasil triase. */
enum class Urgensi { EDUKASI_MANDIRI, KONTROL_RUTIN, SEGERA_KE_DOKTER }

/**
 * Antarmuka yang disepakati di jobdesk:
 * DetectionResult(kelas, confidence, urgensi, ditolak).
 * Jangan ubah bentuknya tanpa diskusi kelompok (dipakai Anggota 2 dan 4).
 * `urgensi` null jika `ditolak` = true.
 */
data class DetectionResult(
    val kelas: String,
    val confidence: Float,
    val urgensi: Urgensi?,
    val ditolak: Boolean
)

/** Keluaran mentah Classifier sebelum triase. */
data class Prediction(
    val kelas: String,
    val confidence: Float,
    val probabilitas: FloatArray,
    val waktuInferensiMs: Long
)
