package com.kelompok4.oralhealthplus.feature.education

/**
 * Hasil yang siap ditampilkan layar.
 * [personal] = true berarti berdasarkan hasil deteksi; false berarti konten umum (state Kosong/Default).
 */
data class HasilRekomendasi(
    val artikel: List<ScoredContent>,
    val video: List<ScoredContent>,
    val personal: Boolean,
)

/**
 * Menyatukan alur fungsi 4 (lihat flowchart):
 *  hasil deteksi tersedia? -> ambil kata kunci -> ambil artikel + video -> skor/saring/urut.
 *  Jika label tidak ada / tidak dikenal, atau tidak ada konten yang relevan -> konten umum.
 *  Jika artikel gagal diambil -> exception diteruskan ke ViewModel (state Gagal).
 *
 * [kataKunciUntuk] biasanya = EncyclopediaRepository::kataKunciUntuk (Section 3).
 */
class EducationRepository(
    private val sumberArtikel: ArticleSource,
    private val kataKunciUntuk: (String?) -> List<String>,
    private val daftarVideo: List<EducationContent> = VideoCatalog.semua,
    private val sekarang: () -> Long = { System.currentTimeMillis() },
) {

    suspend fun muatRekomendasi(labelDeteksi: String?): HasilRekomendasi {
        val kataKunci = kataKunciUntuk(labelDeteksi)
        if (kataKunci.isNotEmpty()) {
            val personal = hitung(kataKunci)
            if (personal.artikel.isNotEmpty() || personal.video.isNotEmpty()) {
                return personal.copy(personal = true)
            }
        }
        return hitung(KATA_KUNCI_UMUM).copy(personal = false)
    }

    private suspend fun hitung(kataKunci: List<String>): HasilRekomendasi {
        val waktu = sekarang()
        val artikelMentah = sumberArtikel.cariArtikel(kataKunci)
        return HasilRekomendasi(
            artikel = ContentRecommender.rekomendasikan(artikelMentah, kataKunci, waktu),
            video = ContentRecommender.rekomendasikan(daftarVideo, kataKunci, waktu),
            personal = false,
        )
    }

    companion object {
        /** Kata kunci konten umum kesehatan mulut, dipakai saat belum ada hasil deteksi. */
        val KATA_KUNCI_UMUM = listOf(
            "kesehatan gigi", "kesehatan mulut", "sikat gigi", "menyikat gigi",
            "oral health", "oral hygiene", "dental care", "brush your teeth", "teeth",
        )
    }
}
