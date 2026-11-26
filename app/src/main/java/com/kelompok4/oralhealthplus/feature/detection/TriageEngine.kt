package com.kelompok4.oralhealthplus.feature.detection

/**
 * Triase urgensi dari decision table.
 *
 * Aturan:
 *  1. confidence < AMBANG_YAKIN (atau bukan angka)  -> DITOLAK, minta ulangi foto
 *  2. selain itu: urgensi = tabel[kelas][tingkat], dengan
 *       tingkat "sedang" = AMBANG_YAKIN <= p < AMBANG_TINGGI
 *       tingkat "tinggi" = p >= AMBANG_TINGGI
 *  3. kelas tidak dikenal -> KONTROL_RUTIN (pilihan aman: arahkan ke pemeriksaan)
 *
 * !! ISI TABEL DI BAWAH HARUS SAMA DENGAN decision table di dokumen UTP kalian.
 * !! Nilai di sini adalah USULAN awal; ganti sesuai tabel UTP / masukan dosen.
 * Kunci memakai nama kelas yang sudah dinormalisasi (huruf kecil, tanpa spasi/simbol)
 * dan dicocokkan dengan "contains", jadi "Data caries" tetap cocok dengan "caries".
 * Cocokkan kunci dengan isi labels.txt hasil Colab.
 */
object TriageEngine {
    const val AMBANG_YAKIN = 0.60f   // samakan dengan Colab & flowchart
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

    /** true jika kelas punya baris sendiri di decision table (bukan memakai nilai default). */
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
