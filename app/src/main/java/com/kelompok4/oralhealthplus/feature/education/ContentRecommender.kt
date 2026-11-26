package com.kelompok4.oralhealthplus.feature.education

/**
 * Alur rekomendasi konten, sesuai flowchart fungsi 4:
 *  1. Buang konten yang judulnya kosong.
 *  2. Normalisasi judul, buang judul yang sudah pernah muncul (duplikat). Yang pertama muncul dipertahankan.
 *  3. Hitung skor relevansi.
 *  4. Buang yang skornya 0 (tidak relevan).
 *  5. Urutkan skor tertinggi; jika skor sama, yang lebih baru di atas (tanpa tanggal ditaruh paling bawah).
 *  6. Ambil [batas] teratas.
 *
 * Hasil kosong berarti layar menampilkan konten umum (state Kosong/Default).
 */
object ContentRecommender {

    const val BATAS_DEFAULT = 5

    fun saringDuplikat(daftar: List<EducationContent>): List<EducationContent> {
        val sudahAda = HashSet<String>()
        return daftar.filter { konten ->
            val kunci = RelevanceScorer.normalisasi(konten.judul)
            kunci.isNotEmpty() && sudahAda.add(kunci)
        }
    }

    fun rekomendasikan(
        daftar: List<EducationContent>,
        kataKunci: List<String>,
        sekarangMillis: Long = System.currentTimeMillis(),
        batas: Int = BATAS_DEFAULT,
    ): List<ScoredContent> =
        saringDuplikat(daftar.filter { !it.judul.isNullOrBlank() })
            .map { ScoredContent(it, RelevanceScorer.skor(it, kataKunci, sekarangMillis)) }
            .filter { it.skor > 0 }
            .sortedWith(
                compareByDescending<ScoredContent> { it.skor }
                    .thenByDescending { it.konten.publishedAtMillis ?: Long.MIN_VALUE }
            )
            .take(batas)
}
