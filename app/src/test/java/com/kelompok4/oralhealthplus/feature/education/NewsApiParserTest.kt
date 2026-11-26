package com.kelompok4.oralhealthplus.feature.education

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NewsApiParserTest {

    @Test
    fun responsSukses_artikelTerbaca() {
        val json = """
            {"status":"ok","totalResults":1,"articles":[
              {"source":{"name":"Detik"},"title":"Judul","description":"Isi","url":"https://a.id/1",
               "urlToImage":"https://a.id/1.jpg","publishedAt":"2026-11-20T10:00:00Z"}
            ]}
        """.trimIndent()
        val hasil = NewsApiParser.parse(json) as NewsApiParser.Hasil.Sukses
        val a = hasil.artikel.single()
        assertEquals("Judul", a.judul)
        assertEquals("Detik", a.sumber)
        assertEquals(ContentType.ARTIKEL, a.tipe)
        assertEquals(NewsApiParser.parseTanggal("2026-11-20T10:00:00Z"), a.publishedAtMillis)
    }

    @Test
    fun artikelRemovedDanTanpaUrl_dibuang_judulKosongTetapDiteruskan() {
        val json = """
            {"status":"ok","articles":[
              {"title":"[Removed]","url":"https://removed.com"},
              {"title":"Tanpa URL","url":null},
              {"title":null,"url":"https://a.id/2"},
              null
            ]}
        """.trimIndent()
        val hasil = NewsApiParser.parse(json) as NewsApiParser.Hasil.Sukses
        // Judul null diteruskan: yang membuangnya ContentRecommender, sesuai flowchart.
        assertEquals(listOf("https://a.id/2"), hasil.artikel.map { it.url })
    }

    @Test
    fun responsError_pesanDariApi() {
        val hasil = NewsApiParser.parse("""{"status":"error","code":"apiKeyInvalid","message":"Your API key is invalid."}""")
        assertEquals("Your API key is invalid.", (hasil as NewsApiParser.Hasil.Gagal).pesan)
    }

    @Test
    fun jsonRusak_gagalTanpaCrash() {
        assertTrue(NewsApiParser.parse("<html>502</html>") is NewsApiParser.Hasil.Gagal)
    }

    @Test
    fun tanggal_formatDenganDetikPecahan_danTidakValid() {
        assertEquals(
            NewsApiParser.parseTanggal("2026-11-20T10:00:00Z"),
            NewsApiParser.parseTanggal("2026-11-20T10:00:00.123Z"),
        )
        assertNull(NewsApiParser.parseTanggal("kemarin"))
        assertNull(NewsApiParser.parseTanggal(null))
    }

    @Test
    fun fileContohOffline_bisaDibaca() {
        val json = File("src/main/assets/newsapi_sample.json").readText()
        val hasil = NewsApiParser.parse(json) as NewsApiParser.Hasil.Sukses
        assertEquals(8, hasil.artikel.size) // 9 artikel, 1 berlabel [Removed]
    }
}
