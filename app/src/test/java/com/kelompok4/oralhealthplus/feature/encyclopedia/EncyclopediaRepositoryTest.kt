package com.kelompok4.oralhealthplus.feature.encyclopedia

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class EncyclopediaRepositoryTest {

    /** Label resmi dari assets/labels.txt Anggota 1. Kalau label berubah, ubah juga di sini. */
    private val labelResmi = listOf(
        "Calculus", "Caries", "Gingivitis", "Hypodontia", "Mouth_Ulcer", "Tooth_Discoloration",
    )

    /** Unit test berjalan dari folder modul app, jadi file asli bisa dibaca langsung. */
    private fun jsonAsli(): String = File("src/main/assets/encyclopedia.json").readText()

    @Test
    fun fileAsli_mencakupSemuaLabelDeteksi() {
        val repo = EncyclopediaRepository.dariJson(jsonAsli())
        labelResmi.forEach { label ->
            assertTrue("Label $label belum ada di encyclopedia.json", repo.cari(label) != null)
            assertTrue("Label $label belum punya kata kunci", repo.kataKunciUntuk(label).isNotEmpty())
        }
    }

    @Test
    fun kataKunci_labelGingivitis() {
        val repo = EncyclopediaRepository.dariJson(jsonAsli())
        assertTrue(repo.kataKunciUntuk("Gingivitis").contains("gusi berdarah"))
    }

    @Test
    fun labelTidakDikenalAtauNull_kataKunciKosong() {
        val repo = EncyclopediaRepository.dariJson(jsonAsli())
        assertTrue(repo.kataKunciUntuk("Normal").isEmpty())
        assertTrue(repo.kataKunciUntuk(null).isEmpty())
        assertNull(repo.cari(null))
    }

    @Test
    fun entriTidakLengkap_dibuang() {
        val json = """
            {"penyakit": [
              {"id": "A", "nama": "Lengkap", "kataKunci": ["satu"]},
              {"id": "B", "nama": "Tanpa kata kunci", "kataKunci": []},
              {"nama": "Tanpa id", "kataKunci": ["dua"]},
              null
            ]}
        """.trimIndent()
        val repo = EncyclopediaRepository.dariJson(json)
        assertEquals(listOf("A"), repo.semua().map { it.id })
    }

    @Test
    fun jsonRusak_tidakCrashDanKosong() {
        assertTrue(EncyclopediaRepository.dariJson("{ ini bukan json").semua().isEmpty())
    }
}
