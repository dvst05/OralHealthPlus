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
    fun evaluateMessage_rateLimiting_rejectsTooFastMessages() {
        val baseTime = 1000000L
        val state = SecurityGateState(lastMessageTimestamp = baseTime)

        // Sending after only 500 ms (< 2000 ms cooldown)
        val result = ChatbotSecurityGate.evaluateMessage(
            message = "Gigi saya ngilu",
            currentTimeMillis = baseTime + 500L,
            state = state
        )

        assertTrue(result is SecurityEvaluationResult.RejectedSpam)
        val spam = result as SecurityEvaluationResult.RejectedSpam
        assertEquals(2L, spam.waitSeconds)
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
        // Should reset violations on valid message
        assertEquals(0, allowed.updatedState.violationCount)
    }

    @Test
    fun evaluateMessage_outOfScopeViolations_escalateToLevel1SuspensionOn3rdStrike() {
        val baseTime = 1000000L
        var state = SecurityGateState(lastMessageTimestamp = 0L)

        // Strike 1
        val result1 = ChatbotSecurityGate.evaluateMessage("cara bikin nasi goreng", baseTime, state)
        assertTrue(result1 is SecurityEvaluationResult.RejectedOutOfScope)
        val out1 = result1 as SecurityEvaluationResult.RejectedOutOfScope
        assertEquals(1, out1.currentViolations)
        assertEquals(3, out1.maxViolations)
        state = out1.updatedState

        // Strike 2
        val result2 = ChatbotSecurityGate.evaluateMessage("siapa presiden amerika", baseTime + 3000L, state)
        assertTrue(result2 is SecurityEvaluationResult.RejectedOutOfScope)
        val out2 = result2 as SecurityEvaluationResult.RejectedOutOfScope
        assertEquals(2, out2.currentViolations)
        state = out2.updatedState

        // Strike 3 -> Suspended 24 hours (Level 1)
        val result3 = ChatbotSecurityGate.evaluateMessage("buatkan kode python", baseTime + 6000L, state)
        assertTrue(result3 is SecurityEvaluationResult.SuspendedNow)
        val suspended = result3 as SecurityEvaluationResult.SuspendedNow
        assertEquals(1, suspended.level)
        assertFalse(suspended.isPermanent)
        val expectedSuspensionUntil = (baseTime + 6000L) + ChatbotSecurityGate.SUSPENSION_24_HOURS_MS
        assertEquals(expectedSuspensionUntil, suspended.updatedState.suspensionUntil)
        assertEquals(1, suspended.updatedState.suspensionLevel)
    }

    @Test
    fun evaluateMessage_activeSuspension_blocksAnyMessage() {
        val baseTime = 1000000L
        val state = SecurityGateState(
            suspensionLevel = 1,
            suspensionUntil = baseTime + 3600000L // 1 hour remaining
        )

        val result = ChatbotSecurityGate.evaluateMessage(
            message = "Halo dok gigi saya sakit",
            currentTimeMillis = baseTime,
            state = state
        )

        assertTrue(result is SecurityEvaluationResult.Blocked)
        val blocked = result as SecurityEvaluationResult.Blocked
        assertEquals(3600000L, blocked.remainingMillis)
        assertEquals(1, blocked.level)
        assertFalse(blocked.isPermanent)
        assertTrue(blocked.formattedRemainingTime.contains("60 menit") || blocked.formattedRemainingTime.contains("1 jam"))
    }

    @Test
    fun evaluateMessage_repeatViolationAfterLevel1_escalatesToLevel2SevenDays() {
        val baseTime = 1000000000L
        // Level 1 suspension already expired
        val state = SecurityGateState(
            suspensionLevel = 1,
            suspensionUntil = baseTime - 1000L,
            violationCount = 0
        )

        // Repeat violation
        val result = ChatbotSecurityGate.evaluateMessage(
            message = "cara memperbaiki motor",
            currentTimeMillis = baseTime,
            state = state
        )

        assertTrue(result is SecurityEvaluationResult.SuspendedNow)
        val suspended = result as SecurityEvaluationResult.SuspendedNow
        assertEquals(2, suspended.level)
        assertFalse(suspended.isPermanent)
        val expectedUntil = baseTime + ChatbotSecurityGate.SUSPENSION_7_DAYS_MS
        assertEquals(expectedUntil, suspended.updatedState.suspensionUntil)
        assertEquals(2, suspended.updatedState.suspensionLevel)
    }

    @Test
    fun evaluateMessage_repeatViolationAfterLevel2_escalatesToPermanentBlock() {
        val baseTime = 1000000000L
        // Level 2 suspension expired
        val state = SecurityGateState(
            suspensionLevel = 2,
            suspensionUntil = baseTime - 1000L,
            violationCount = 0
        )

        val result = ChatbotSecurityGate.evaluateMessage(
            message = "jadwal bioskop",
            currentTimeMillis = baseTime,
            state = state
        )

        assertTrue(result is SecurityEvaluationResult.SuspendedNow)
        val suspended = result as SecurityEvaluationResult.SuspendedNow
        assertEquals(3, suspended.level)
        assertTrue(suspended.isPermanent)
        assertEquals(Long.MAX_VALUE, suspended.updatedState.suspensionUntil)
    }
}
