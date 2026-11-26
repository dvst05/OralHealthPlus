package com.kelompok4.oralhealthplus.feature.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuestAccessPolicyTest {

    @Test
    fun initialGuestQuota_allowsOneScanOneAnalysisAndThreeChats() {
        val state = GuestQuotaState(isLoggedIn = false, scanCount = 0, analysisCount = 0, chatQuestionCount = 0)

        assertTrue(state.canScanPhoto)
        assertTrue(state.canAnalyzePhoto)
        assertTrue(state.canStartNewScanFlow)
        assertTrue(state.canAskChatbot)
        assertEquals(1, state.remainingGuestAnalyses)
        assertEquals(3, state.remainingGuestChats)
    }

    @Test
    fun guestAfterOneScan_blocksSecondScanButStillAllowsFirstAnalysis() {
        val state = GuestQuotaState(isLoggedIn = false, scanCount = 1, analysisCount = 0, chatQuestionCount = 0)

        assertFalse(state.canScanPhoto)
        assertTrue(state.canAnalyzePhoto)
        assertTrue(state.canStartNewScanFlow)
    }

    @Test
    fun guestAfterOneAnalysis_blocksFurtherScansAndAnalyses() {
        val state = GuestQuotaState(isLoggedIn = false, scanCount = 1, analysisCount = 1, chatQuestionCount = 0)

        assertFalse(state.canScanPhoto)
        assertFalse(state.canAnalyzePhoto)
        assertFalse(state.canStartNewScanFlow)
        assertEquals(0, state.remainingGuestAnalyses)
        assertTrue(state.canAskChatbot)
    }

    @Test
    fun guestAfterThreeChatQuestions_blocksFourthQuestion() {
        val twoAsked = GuestQuotaState(isLoggedIn = false, chatQuestionCount = 2)
        assertTrue(twoAsked.canAskChatbot)
        assertEquals(1, twoAsked.remainingGuestChats)

        val threeAsked = GuestQuotaState(isLoggedIn = false, chatQuestionCount = 3)
        assertFalse(threeAsked.canAskChatbot)
        assertEquals(0, threeAsked.remainingGuestChats)
    }

    @Test
    fun loggedInUser_hasUnlimitedAccessRegardlessOfPreviousGuestCounts() {
        val state = GuestQuotaState(isLoggedIn = true, scanCount = 5, analysisCount = 5, chatQuestionCount = 50)

        assertTrue(state.canScanPhoto)
        assertTrue(state.canAnalyzePhoto)
        assertTrue(state.canStartNewScanFlow)
        assertTrue(state.canAskChatbot)
        assertEquals(Int.MAX_VALUE, state.remainingGuestAnalyses)
        assertEquals(Int.MAX_VALUE, state.remainingGuestChats)
    }
}
