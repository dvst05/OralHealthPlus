package com.kelompok4.oralhealthplus.feature.detection

import com.kelompok4.oralhealthplus.data.model.DetectionResult
import com.kelompok4.oralhealthplus.data.model.Urgensi

object TriageEngine {
    const val AMBANG_YAKIN = 0.60f
    const val AMBANG_TINGGI = 0.80f

    /** kunci kelas -> (urgensi jika confidence sedang, urgensi jika confidence tinggi) */
    private val tabel: Map<String, Pair<Urgensi, Urgensi>> = mapOf(
        "calculus" to (Urgensi.KONTROL_RUTIN to Urgensi.KONTROL_RUTIN),
        "caries" to (Urgensi.KONTROL_RUTIN to Urgensi.SEGERA_KE_DOKTER),
        "gingivitis" to (Urgensi.EDUKASI_MANDIRI to Urgensi.KONTROL_RUTIN),
        "mouthulcer" to (Urgensi.EDUKASI_MANDIRI to Urgensi.KONTROL_RUTIN),
        "toothdiscoloration" to (Urgensi.EDUKASI_MANDIRI to Urgensi.EDUKASI_MANDIRI),
        "hypodontia" to (Urgensi.KONTROL_RUTIN to Urgensi.KONTROL_RUTIN),
    )

    fun normalisasi(label: String): String = label.lowercase().replace(Regex("[^a-z]"), "")

    fun dikenal(kelas: String): Boolean = cariBaris(kelas) != null

    private fun cariBaris(kelas: String): Pair<Urgensi, Urgensi>? {
        val kunci = normalisasi(kelas)
        return tabel.entries.firstOrNull { kunci.contains(it.key) }?.value
    }

    fun evaluate(kelas: String, confidence: Float): DetectionResult {
        if (confidence.isNaN() || confidence < AMBANG_YAKIN) {
            return DetectionResult(kelas, confidence, urgensi = null, ditolak = true)
        }
        val baris = cariBaris(kelas)
        val urgensi = when {
            baris == null -> Urgensi.KONTROL_RUTIN
            confidence >= AMBANG_TINGGI -> baris.second
            else -> baris.first
        }
        return DetectionResult(kelas, confidence, urgensi, ditolak = false)
    }
}