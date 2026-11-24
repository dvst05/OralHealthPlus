package com.kelompok4.oralhealthplus.feature.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingLogicTest {

    private val total = 3

    @Test
    fun halamanAwalDanTengah_tombolNext() {
        assertEquals("Next", OnboardingLogic.labelTombol(0, total))
        assertEquals("Next", OnboardingLogic.labelTombol(1, total))
    }

    @Test
    fun halamanTerakhir_tombolDone() {
        assertEquals("Done", OnboardingLogic.labelTombol(2, total))
        assertTrue(OnboardingLogic.halamanTerakhir(2, total))
        assertFalse(OnboardingLogic.halamanTerakhir(1, total))
    }

    @Test
    fun next_majuSatuHalaman_tidakMelewatiBatas() {
        assertEquals(1, OnboardingLogic.halamanBerikutnya(0, total))
        assertEquals(2, OnboardingLogic.halamanBerikutnya(1, total))
        assertEquals(2, OnboardingLogic.halamanBerikutnya(2, total))
    }
}
