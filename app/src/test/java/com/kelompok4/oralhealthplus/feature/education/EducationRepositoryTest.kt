package com.kelompok4.oralhealthplus.feature.education

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class EducationRepositoryTest {

    private val sekarang = 1_800_000_000_000L
    private val hari = 24L * 60 * 60 * 1000

    private fun artikel(judul: String, hariLalu: Long = 1) =
        EducationContent(judul, null, "url-$judul", ContentType.ARTIKEL, publishedAtMillis = sekarang - hariLalu * hari)

    private val sumberContoh = ArticleSource {
        listOf(
            artikel("Gejala gingivitis pada orang dewasa"),
            artikel("Tips menjaga kesehatan gigi"),
            artikel("Resep nasi goreng"),
        )
    }

    private val kataKunci: (String?) -> List<String> = { label ->
        if (label == "Gingivitis") listOf("gingivitis", "gusi berdarah") else emptyList()
    }

    private val videoContoh = listOf(
        EducationContent("Video gingivitis", null, "v1", ContentType.VIDEO),
        EducationContent("Video kesehatan gigi", null, "v2", ContentType.VIDEO),
    )

    private fun repo(sumber: ArticleSource = sumberContoh) =
        EducationRepository(sumber, kataKunci, videoContoh) { sekarang }

    @Test
    fun adaHasilDeteksi_rekomendasiPersonal() = runBlocking {
        val hasil = repo().muatRekomendasi("Gingivitis")
        assertTrue(hasil.personal)
        assertEquals(listOf("Gejala gingivitis pada orang dewasa"), hasil.artikel.map { it.konten.judul })
        assertEquals(listOf("v1"), hasil.video.map { it.konten.url })
    }

    @Test
    fun belumAdaHasilDeteksi_kontenUmum() = runBlocking {
        val hasil = repo().muatRekomendasi(null)
        assertFalse(hasil.personal)
        assertEquals(listOf("Tips menjaga kesehatan gigi"), hasil.artikel.map { it.konten.judul })
    }

    @Test
    fun labelTidakDikenal_kontenUmum() = runBlocking {
        assertFalse(repo().muatRekomendasi("Penyakit_Baru").personal)
    }

    @Test
    fun tidakAdaYangRelevan_kontenUmum() = runBlocking {
        val sumber = ArticleSource { listOf(artikel("Tips menjaga kesehatan gigi")) }
        val repoTanpaVideoRelevan = EducationRepository(sumber, kataKunci, emptyList()) { sekarang }
        val hasil = repoTanpaVideoRelevan.muatRekomendasi("Gingivitis")
        assertFalse(hasil.personal)
        assertEquals(1, hasil.artikel.size)
    }

    @Test(expected = IOException::class)
    fun sumberGagal_errorDiteruskan(): Unit = runBlocking {
        repo(ArticleSource { throw IOException("internet mati") }).muatRekomendasi("Gingivitis")
        Unit
    }

    @Test
    fun query_frasaDiberiKutip_danDigabungOr() {
        assertEquals(
            "gingivitis OR \"gusi berdarah\"",
            NewsApiArticleSource.buatQuery(listOf("gingivitis", "gusi berdarah", "gingivitis", " ")),
        )
    }

    @Test
    fun query_tidakMelebihiBatasNewsApi() {
        val banyak = (1..200).map { "katakunci$it" }
        assertTrue(NewsApiArticleSource.buatQuery(banyak).length <= NewsApiArticleSource.MAKS_PANJANG_QUERY)
    }
}
