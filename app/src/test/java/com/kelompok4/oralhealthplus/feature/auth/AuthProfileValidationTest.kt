package com.kelompok4.oralhealthplus.feature.auth

import com.kelompok4.oralhealthplus.data.repository.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthProfileValidationTest {

    @Test
    fun normalizeWhatsappNumber_blankIsValidOptional() {
        assertEquals("", AuthRepository.normalizeWhatsappNumber(""))
        assertEquals("", AuthRepository.normalizeWhatsappNumber("   "))
    }

    @Test
    fun normalizeWhatsappNumber_convertsLocalIndonesianZeroPrefixTo62() {
        assertEquals("6281234567890", AuthRepository.normalizeWhatsappNumber("081234567890"))
        assertEquals("6281234567890", AuthRepository.normalizeWhatsappNumber("0812-3456-7890"))
    }

    @Test
    fun normalizeWhatsappNumber_preservesInternationalFormat() {
        assertEquals("6281234567890", AuthRepository.normalizeWhatsappNumber("+62 812 3456 7890"))
        assertEquals("6281234567890", AuthRepository.normalizeWhatsappNumber("6281234567890"))
    }

    @Test
    fun normalizeWhatsappNumber_rejectsTooShortTooLongOrNonDigits() {
        assertNull(AuthRepository.normalizeWhatsappNumber("08123"))
        assertNull(AuthRepository.normalizeWhatsappNumber("081234567890123456"))
        assertNull(AuthRepository.normalizeWhatsappNumber("0812abc67890"))
    }
}
