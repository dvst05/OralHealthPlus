package com.kelompok4.oralhealthplus.feature.detection

import com.kelompok4.oralhealthplus.data.model.DetectionResult
import com.kelompok4.oralhealthplus.data.model.Urgensi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IntegrasiRiwayatTest {

    @Test
    fun hasilDiterima_semuaFieldModelBersamaTerbawa() {
        val h: DetectionResult = TriageEngine.evaluate("Caries", 0.91f)
        assertEquals("Caries", h.kelas)
        assertEquals(0.91f, h.confidence, 0f)
        assertEquals(Urgensi.SEGERA_KE_DOKTER, h.urgensi)
        assertFalse(h.ditolak)
    }

    @Test
    fun hasilDitolak_urgensiTetapNull() {
        val h: DetectionResult = TriageEngine.evaluate("Caries", 0.2f)
        assertTrue(h.ditolak)
        assertNull(h.urgensi)
    }
}