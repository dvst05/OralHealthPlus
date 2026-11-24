package com.kelompok4.oralhealthplus.feature.education

/**
 * Menghitung skor relevansi satu konten terhadap kata kunci penyakit.
 *
 * Metode: Weighted Zone Scoring (Manning dkk., 2008, Introduction to Information Retrieval, bab 6.1).
 * Teks dibagi menjadi dua zona, judul dan deskripsi; tiap zona punya bobot sendiri.
 *
 *   skor = (cocokJudul x BOBOT_JUDUL) + (cocokDeskripsi x BOBOT_DESKRIPSI) + bonusKebaruan
 *
 * - cocokJudul / cocokDeskripsi = jumlah kata kunci BERBEDA yang muncul di zona itu.
 *   Kata kunci yang diulang-ulang tetap dihitung satu, supaya judul yang "spam kata kunci" tidak menang.
 * - Bonus kebaruan hanya diberikan jika konten sudah relevan (skor kata kunci > 0),
 *   supaya artikel baru yang tidak nyambung tidak ikut lolos.
 *
 * Nilai bobot di bawah adalah ASUMSI AWAL kelompok, belum hasil pengujian/optimasi.
 */
object RelevanceScorer {

    const val BOBOT_JUDUL = 3
    const val BOBOT_DESKRIPSI = 1

    const val BONUS_SANGAT_BARU = 2   // terbit <= 7 hari terakhir
    const val BONUS_BARU = 1          // terbit <= 30 hari terakhir

    private const val SATU_HARI_MS = 24L * 60 * 60 * 1000

    /**
     * Mengubah teks menjadi bentuk baku: huruf kecil, tanda baca jadi spasi, spasi ganda dirapikan.
     * Contoh: "Gingivitis: Gejala & Penyebab!" -> "gingivitis gejala penyebab"
     */
    fun normalisasi(teks: String?): String =
        (teks ?: "")
            .lowercase()
            .replace(Regex("[^\\p{L}\\p{Nd}]+"), " ")
            .trim()

    /** Jumlah kata kunci berbeda yang muncul utuh (bukan potongan kata) di dalam teks. */
    fun hitungCocok(teks: String?, kataKunci: List<String>): Int {
        val isi = " ${normalisasi(teks)} "
        return kataKunci
            .map { normalisasi(it) }
            .filter { it.isNotEmpty() }
            .distinct()
            .count { isi.contains(" $it ") }
    }

    /** Bonus kebaruan berdasarkan selisih hari terbit dengan [sekarangMillis]. */
    fun bonusKebaruan(publishedAtMillis: Long?, sekarangMillis: Long): Int {
        if (publishedAtMillis == null) return 0
        val selisihHari = (sekarangMillis - publishedAtMillis) / SATU_HARI_MS
        return when {
            selisihHari < 0 -> 0            // tanggal di masa depan dianggap tidak valid
            selisihHari <= 7 -> BONUS_SANGAT_BARU
            selisihHari <= 30 -> BONUS_BARU
            else -> 0
        }
    }

    fun skor(konten: EducationContent, kataKunci: List<String>, sekarangMillis: Long): Int {
        val skorKataKunci =
            hitungCocok(konten.judul, kataKunci) * BOBOT_JUDUL +
                hitungCocok(konten.deskripsi, kataKunci) * BOBOT_DESKRIPSI
        if (skorKataKunci == 0) return 0
        return skorKataKunci + bonusKebaruan(konten.publishedAtMillis, sekarangMillis)
    }
}
