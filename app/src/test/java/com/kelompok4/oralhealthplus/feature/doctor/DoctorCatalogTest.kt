package com.kelompok4.oralhealthplus.feature.doctor

import com.kelompok4.oralhealthplus.data.local.entity.DetectionHistoryEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoctorCatalogTest {

    @Test
    fun doctorsList_isNotEmptyAndHasValidWhatsAppNumbers() {
        assertTrue(DoctorCatalog.doctors.size >= 4)
        DoctorCatalog.doctors.forEach { doctor ->
            assertTrue(doctor.name.isNotBlank())
            assertTrue(doctor.whatsappNumber.startsWith("62"))
            assertTrue(doctor.whatsappNumber.all { it.isDigit() })
        }
    }

    @Test
    fun getSortedDoctors_prioritizesMatchingSpecialistForScanCondition() {
        val sortedForGingivitis = DoctorCatalog.getSortedDoctors("Gingivitis")
        assertTrue(DoctorCatalog.isRecommendedFor(sortedForGingivitis.first(), "Gingivitis"))

        val sortedForCaries = DoctorCatalog.getSortedDoctors("Data_Caries")
        assertTrue(DoctorCatalog.isRecommendedFor(sortedForCaries.first(), "Data_Caries"))
    }

    @Test
    fun buildWhatsAppUrl_containsDoctorNumberAndEncodedScanContext() {
        val doctor = DoctorCatalog.doctors.first()
        val detection = DetectionHistoryEntity(
            id = 10,
            username = "yusuf",
            kelas = "Data_Caries",
            confidence = 0.91f,
            urgensi = "SEGERA_KE_DOKTER",
            deskripsi = "Karies gigi",
            catatanKeluhan = "Nyeri berdenyut"
        )

        val url = DoctorCatalog.buildWhatsAppUrl(
            doctor = doctor,
            latestDetection = detection,
            severeContext = "Pipi bengkak"
        )

        assertTrue(url.startsWith("https://wa.me/${doctor.whatsappNumber}?text="))
        assertTrue(url.contains("OralHealth"))
        assertTrue(url.contains("Caries"))
        assertTrue(url.contains("Pipi+bengkak") || url.contains("Pipi%20bengkak"))
        assertFalse(url.contains(" "))
    }
}
