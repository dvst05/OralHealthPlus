package com.kelompok4.oralhealthplus.feature.education

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Data dummy 5 artikel ini sama dengan tabel perhitungan manual di laporan,
 * jadi hasil test = bukti bahwa kode dan hitungan manual cocok.
 * Kata kunci Gingivitis: gingivitis, gusi berdarah, radang gusi.
 */
class ContentRecommenderTest {

    private val hari = 24L * 60 * 60 * 1000
    private val sekarang = 1_800_000_000_000L
    private val kataKunci = listOf("gingivitis", "gusi berdarah", "radang gusi")

    private fun artikel(id: String, judul: String?, deskripsi: String?, hariLalu: Long?) =
        EducationContent(
            judul = judul, deskripsi = deskripsi, url = id, tipe = ContentType.ARTIKEL,
            publishedAtMillis = hariLalu?.let { sekarang - it * hari },
        )

    private val lima = listOf(
        artikel("A1", "Gingivitis: Gejala dan Penyebab", "Gusi berdarah saat sikat gigi bisa jadi tanda radang gusi.", 3),
        artikel("A2", "Cara Mencegah Gusi Berdarah", "Rawat kebersihan mulut setiap hari.", 20),
        artikel("A3", "Manfaat Flossing untuk Gigi", "Flossing membantu mencegah gingivitis.", 60),
        artikel("A4", "Tips Memilih Sikat Gigi", "Pilih bulu sikat yang lembut.", 1),
        artikel("A5", "GINGIVITIS - Gejala dan Penyebab!!", "Duplikat dari A1 dengan penulisan berbeda.", 2),
    )

    @Test
    fun limaArtikel_urutanSesuaiTabelManual() {
        // A1 = 3 + 2 + 2 = 7 ; A2 = 3 + 0 + 1 = 4 ; A3 = 0 + 1 + 0 = 1 ; A4 = 0 (dibuang) ; A5 = duplikat A1 (dibuang)
        val hasil = ContentRecommender.rekomendasikan(lima, kataKunci, sekarang)
        assertEquals(listOf("A1", "A2", "A3"), hasil.map { it.konten.url })
        assertEquals(listOf(7, 4, 1), hasil.map { it.skor })
    }

    @Test
    fun duplikat_yangPertamaDipertahankan() {
        val hasil = ContentRecommender.saringDuplikat(lima)
        assertTrue(hasil.any { it.url == "A1" })
        assertTrue(hasil.none { it.url == "A5" })
    }

    @Test
    fun judulKosongAtauNull_dibuang() {
        val daftar = listOf(
            artikel("K1", null, "gingivitis", 1),
            artikel("K2", "   ", "gingivitis", 1),
            artikel("K3", "Gingivitis", null, 1),
        )
        val hasil = ContentRecommender.rekomendasikan(daftar, kataKunci, sekarang)
        assertEquals(listOf("K3"), hasil.map { it.konten.url })
    }

    @Test
    fun skorSama_yangLebihBaruDiAtas_tanpaTanggalPalingBawah() {
        val daftar = listOf(
            artikel("Lama", "Gingivitis pada remaja", null, 100),
            EducationContent("Video gingivitis", null, "Video", ContentType.VIDEO),
            artikel("Baru", "Gingivitis pada lansia", null, 50),
        )
        val hasil = ContentRecommender.rekomendasikan(daftar, kataKunci, sekarang)
        assertEquals(listOf("Baru", "Lama", "Video"), hasil.map { it.konten.url })
    }

    @Test
    fun hanyaLimaTeratas() {
        val daftar = (1..8).map { artikel("G$it", "Gingivitis bagian $it", null, it.toLong()) }
        assertEquals(5, ContentRecommender.rekomendasikan(daftar, kataKunci, sekarang).size)
    }

    @Test
    fun tidakAdaYangRelevan_hasilKosong() {
        val daftar = listOf(artikel("X", "Resep Nasi Goreng", "Enak dan mudah", 1))
        assertTrue(ContentRecommender.rekomendasikan(daftar, kataKunci, sekarang).isEmpty())
    }

    @Test
    fun daftarKosong_hasilKosong() {
        assertTrue(ContentRecommender.rekomendasikan(emptyList(), kataKunci, sekarang).isEmpty())
    }
}
