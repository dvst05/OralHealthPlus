package com.kelompok4.oralhealthplus.feature.detection

import com.kelompok4.oralhealthplus.data.model.Urgensi as UrgensiBersama
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IntegrasiRiwayatTest {

    @Test fun semuaNilaiUrgensi_samaDiKeduaModel() {
        assertEquals(Urgensi.values().map { it.name }, UrgensiBersama.values().map { it.name })
    }

    @Test fun hasilDiterima_semuaFieldTerbawa() {
        val h = DetectionResult("Caries", 0.91f, Urgensi.SEGERA_KE_DOKTER, ditolak = false).keModelBersama()
        assertEquals("Caries", h.kelas)
        assertEquals(0.91f, h.confidence, 0f)
        assertEquals(UrgensiBersama.SEGERA_KE_DOKTER, h.urgensi)
        assertEquals(false, h.ditolak)
    }

    @Test fun hasilDitolak_urgensiTetapNull() {
        val h = TriageEngine.evaluate("Caries", 0.2f).keModelBersama()
        assertTrue(h.ditolak)
        assertNull(h.urgensi)
    }
}
