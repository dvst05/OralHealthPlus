package com.kelompok4.oralhealthplus.feature.chatbot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatbotSecurityGateTest {

    @Test
    fun isDomainRelevant_greetingsAndOralHealth_returnsTrue() {
        // Greetings
        assertTrue(ChatbotSecurityGate.isDomainRelevant("halo"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("Halo dok"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("selamat pagi dokter"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("terima kasih banyak"))

        // Common Dental & Oral Symptoms & Terms
        assertTrue(ChatbotSecurityGate.isDomainRelevant("gigi saya ngilu saat minum air es"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("gusi sering berdarah saat sikat gigi"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("ada sariawan perih di bibir bawah"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("cara memakai benang gigi yang benar"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("rekomendasi pasta gigi untuk gigi sensitif"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("gigi berlubang dan berdenyut parah"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("karang gigi menumpuk di bagian dalam"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("bau mulut tak kunjung hilang"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("rahang sakit saat mengunyah"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("tambalan gigi saya lepas kemarin"))
    }

    @Test
    fun isDomainRelevant_quickPromptsAndScanDiseases_returnsTrue() {
        val diseaseLabels = listOf(
            "Gingivitis",
            "Caries",
            "Calculus",
            "Mouth_Ulcer",
            "Tooth_Discoloration",
            "Hypodontia"
        )
        diseaseLabels.forEach { label ->
            val namaPenyakit = label.replace("_", " ")
            assertTrue(
                "Quick prompt untuk $namaPenyakit harus lolos domain check",
                ChatbotSecurityGate.isDomainRelevant("Jelaskan hasil scan $namaPenyakit saya & perawatan awalnya")
            )
            assertTrue(
                "Quick prompt produk untuk $namaPenyakit harus lolos domain check",
                ChatbotSecurityGate.isDomainRelevant("Rekomendasi pasta & sikat gigi untuk $namaPenyakit")
            )
        }
        assertTrue(ChatbotSecurityGate.isDomainRelevant("Kapan saya harus segera periksa ke dokter gigi?"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("Rekomendasi sikat dan pasta gigi yang cocok"))
        assertTrue(ChatbotSecurityGate.isDomainRelevant("Tanda-tanda awal radang gusi dan karies"))
    }

    @Test
    fun isDomainRelevant_unrelatedTopics_returnsFalse() {
        assertFalse(ChatbotSecurityGate.isDomainRelevant("cara bikin nasi goreng enak"))
        assertFalse(ChatbotSecurityGate.isDomainRelevant("buatkan kode python scraping web"))
        assertFalse(ChatbotSecurityGate.isDomainRelevant("siapa presiden amerika serikat"))
        assertFalse(ChatbotSecurityGate.isDomainRelevant("rekomendasi hp gaming 2 jutaan"))
        assertFalse(ChatbotSecurityGate.isDomainRelevant("jadwal bioskop hari ini"))
        assertFalse(ChatbotSecurityGate.isDomainRelevant("harga emas hari ini"))
        assertFalse(ChatbotSecurityGate.isDomainRelevant("ramalan zodiak scorpio minggu ini"))
    }

    @Test
    fun evaluateMessage_rateLimiterDisabled_allowsRapidRelevantMessages() {
        val baseTime = 1000000L
        val state = SecurityGateState(lastMessageTimestamp = baseTime)

        val result = ChatbotSecurityGate.evaluateMessage(
            message = "Gigi saya ngilu",
            currentTimeMillis = baseTime + 500L,
            state = state
        )

        assertTrue(result is SecurityEvaluationResult.Allowed)
    }

    @Test
    fun evaluateMessage_validMessage_returnsAllowedAndResetsStrike() {
        val baseTime = 1000000L
        val state = SecurityGateState(
            lastMessageTimestamp = baseTime - 5000L,
            violationCount = 2,
            suspensionLevel = 0,
            suspensionUntil = 0L
        )

        val result = ChatbotSecurityGate.evaluateMessage(
            message = "Gusi saya berdarah saat sikat gigi",
            currentTimeMillis = baseTime,
            state = state
        )

        assertTrue(result is SecurityEvaluationResult.Allowed)
        val allowed = result as SecurityEvaluationResult.Allowed
        assertEquals(0, allowed.updatedState.violationCount)
    }

    @Test
    fun evaluateMessage_outOfScope_rejectsWithoutSuspendingWhenBlockingDisabled() {
        val baseTime = 1000000L
        var state = SecurityGateState(lastMessageTimestamp = 0L)

        // Percobaan 1, 2, 3, 4 di luar topik semuanya hanya ditolak, tidak memblokir akses
        val prompts = listOf(
            "cara bikin nasi goreng",
            "siapa presiden amerika",
            "buatkan kode python",
            "jadwal bioskop"
        )
        prompts.forEachIndexed { idx, prompt ->
            val result = ChatbotSecurityGate.evaluateMessage(prompt, baseTime + (idx * 3000L), state)
            assertTrue(result is SecurityEvaluationResult.RejectedOutOfScope)
            val rejected = result as SecurityEvaluationResult.RejectedOutOfScope
            assertEquals(0, rejected.currentViolations)
            assertEquals(0, rejected.updatedState.suspensionLevel)
            assertEquals(0L, rejected.updatedState.suspensionUntil)
            state = rejected.updatedState
        }
    }

    @Test
    fun evaluateMessage_existingSuspensionState_ignoredWhenBlockingDisabled() {
        val baseTime = 1000000L
        val state = SecurityGateState(
            suspensionLevel = 1,
            suspensionUntil = baseTime + 3600000L
        )

        val result = ChatbotSecurityGate.evaluateMessage(
            message = "Halo dok gigi saya sakit",
            currentTimeMillis = baseTime,
            state = state
        )

        assertTrue(result is SecurityEvaluationResult.Allowed)
    }
}
