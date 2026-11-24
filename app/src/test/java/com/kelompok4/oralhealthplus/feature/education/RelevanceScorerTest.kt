package com.kelompok4.oralhealthplus.feature.education

import org.junit.Assert.assertEquals
import org.junit.Test

class RelevanceScorerTest {

    private val hari = 24L * 60 * 60 * 1000
    private val sekarang = 1_800_000_000_000L
    private val kataKunci = listOf("gingivitis", "gusi berdarah", "radang gusi")

    @Test
    fun normalisasi_hurufKecilDanHapusTandaBaca() {
        assertEquals("gingivitis gejala penyebab", RelevanceScorer.normalisasi("Gingivitis: Gejala & Penyebab!"))
        assertEquals("", RelevanceScorer.normalisasi(null))
    }

    @Test
    fun hitungCocok_kataKunciDiulangTetapDihitungSatu() {
        assertEquals(1, RelevanceScorer.hitungCocok("Gingivitis gingivitis GINGIVITIS", kataKunci))
    }

    @Test
    fun hitungCocok_kataKunciMultiKata() {
        assertEquals(2, RelevanceScorer.hitungCocok("Gusi berdarah? Bisa jadi radang gusi", kataKunci))
    }

    @Test
    fun hitungCocok_tidakMencocokkanPotonganKata() {
        // "gusi" tidak boleh dianggap cocok dengan kata kunci "radang gusi"
        assertEquals(0, RelevanceScorer.hitungCocok("Merawat gusi anak", listOf("radang gusi")))
    }

    @Test
    fun bonusKebaruan_batasHari() {
        assertEquals(2, RelevanceScorer.bonusKebaruan(sekarang - 7 * hari, sekarang))
        assertEquals(1, RelevanceScorer.bonusKebaruan(sekarang - 8 * hari, sekarang))
        assertEquals(1, RelevanceScorer.bonusKebaruan(sekarang - 30 * hari, sekarang))
        assertEquals(0, RelevanceScorer.bonusKebaruan(sekarang - 31 * hari, sekarang))
        assertEquals(0, RelevanceScorer.bonusKebaruan(null, sekarang))
        assertEquals(0, RelevanceScorer.bonusKebaruan(sekarang + hari, sekarang))
    }

    @Test
    fun skor_sesuaiRumus() {
        // judul: gingivitis (1 x 3) ; deskripsi: gusi berdarah, radang gusi (2 x 1) ; bonus 3 hari lalu = 2 -> 7
        val konten = EducationContent(
            judul = "Kenali Gingivitis Sejak Dini",
            deskripsi = "Gusi berdarah adalah tanda radang gusi.",
            url = "a", tipe = ContentType.ARTIKEL,
            publishedAtMillis = sekarang - 3 * hari,
        )
        assertEquals(7, RelevanceScorer.skor(konten, kataKunci, sekarang))
    }

    @Test
    fun skor_artikelBaruTapiTidakRelevanTetapNol() {
        val konten = EducationContent(
            judul = "Tips Diet Sehat", deskripsi = "Makan sayur setiap hari",
            url = "b", tipe = ContentType.ARTIKEL, publishedAtMillis = sekarang,
        )
        assertEquals(0, RelevanceScorer.skor(konten, kataKunci, sekarang))
    }
}
