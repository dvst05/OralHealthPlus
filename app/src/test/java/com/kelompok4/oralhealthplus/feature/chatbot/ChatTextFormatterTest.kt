package com.kelompok4.oralhealthplus.feature.chatbot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatTextFormatterTest {

    @Test
    fun parseSegments_stripsDoubleAsterisksAndMarksBoldSegments() {
        val raw = "Halo! Saya **Dokter Gigi Virtual OralHealth+**, siap membantu."
        val segments = ChatTextFormatter.parseSegments(raw)

        val plainText = segments.joinToString("") { it.text }
        assertEquals("Halo! Saya Dokter Gigi Virtual OralHealth+, siap membantu.", plainText)
        assertFalse(plainText.contains("*"))
        assertTrue(segments.any { it.isBold && it.text == "Dokter Gigi Virtual OralHealth+" })
    }

    @Test
    fun parseSegments_convertsMarkdownBulletsAndHeadingsCleanly() {
        val raw = """
            ### Langkah Perawatan
            * Sikat gigi **dua kali** sehari
            - Gunakan benang gigi setiap malam
        """.trimIndent()

        val segments = ChatTextFormatter.parseSegments(raw)
        val plainText = segments.joinToString("") { it.text }

        assertFalse(plainText.contains("###"))
        assertFalse(plainText.contains("**"))
        assertTrue(plainText.contains("Langkah Perawatan"))
        assertTrue(plainText.contains("• Sikat gigi dua kali sehari"))
        assertTrue(plainText.contains("• Gunakan benang gigi setiap malam"))
        assertTrue(segments.any { it.isBold && it.text == "Langkah Perawatan" })
        assertTrue(segments.any { it.isBold && it.text == "dua kali" })
    }

    @Test
    fun parseSegments_cleansStrayMarkdownSymbolsAndBackticks() {
        val raw = "Gunakan `pasta gigi fluoride` secara **rutin dan teratur"
        val segments = ChatTextFormatter.parseSegments(raw)
        val plainText = segments.joinToString("") { it.text }

        assertFalse(plainText.contains("`"))
        assertFalse(plainText.contains("**"))
        assertEquals("Gunakan pasta gigi fluoride secara rutin dan teratur", plainText)
    }

    @Test
    fun parseSegments_formatsGeminiStyleNumberedAndMultiSpaceBulletPoints() {
        val raw = """
            1. **Sikat Gigi 2x Sehari:** Lakukan pagi dan malam sebelum tidur.
            *   **Pilih Pasta Gigi Fluoride:** Membantu melindungi enamel gigi.
        """.trimIndent()

        val segments = ChatTextFormatter.parseSegments(raw)
        val plainText = segments.joinToString("") { it.text }

        assertFalse(plainText.contains("*"))
        assertTrue(plainText.contains("1. Sikat Gigi 2x Sehari: Lakukan pagi dan malam sebelum tidur."))
        assertTrue(plainText.contains("• Pilih Pasta Gigi Fluoride: Membantu melindungi enamel gigi."))
        assertTrue(segments.any { it.isBold && it.text == "Sikat Gigi 2x Sehari:" })
        assertTrue(segments.any { it.isBold && it.text == "Pilih Pasta Gigi Fluoride:" })
    }
}
