package com.kelompok4.oralhealthplus

import com.kelompok4.oralhealthplus.feature.detection.TriageEngine
import com.kelompok4.oralhealthplus.feature.detection.Urgensi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * Taruh di app/src/test/java/com/oralhealth/ml/
 * Gradle: testImplementation("junit:junit:4.13.2")
 * Jika isi tabel di TriageEngine diubah, ubah juga nilai harapan di sini.
 */
class TriageEngineTest {

    private val sedang = 0.70f   // 0.60 <= p < 0.80
    private val tinggi = 0.90f   // p >= 0.80

    private fun cek(kelas: String, p: Float, harapan: Urgensi) {
        val r = TriageEngine.evaluate(kelas, p)
        assertTrue("$kelas @ $p seharusnya tidak ditolak", !r.ditolak)
        assertEquals("$kelas @ $p", harapan, r.urgensi)
    }

    // ---- semua sel decision table (6 kelas x 2 tingkat) ----
    @Test fun calculus() { cek("Calculus", sedang, Urgensi.KONTROL_RUTIN); cek("Calculus", tinggi, Urgensi.KONTROL_RUTIN) }
    @Test fun caries() { cek("Caries", sedang, Urgensi.KONTROL_RUTIN); cek("Caries", tinggi, Urgensi.SEGERA_KE_DOKTER) }
    @Test fun gingivitis() { cek("Gingivitis", sedang, Urgensi.EDUKASI_MANDIRI); cek("Gingivitis", tinggi, Urgensi.KONTROL_RUTIN) }
    @Test fun mouthUlcer() { cek("Mouth Ulcer", sedang, Urgensi.EDUKASI_MANDIRI); cek("Mouth Ulcer", tinggi, Urgensi.KONTROL_RUTIN) }
    @Test fun discoloration() { cek("Tooth Discoloration", sedang, Urgensi.EDUKASI_MANDIRI); cek("Tooth Discoloration", tinggi, Urgensi.EDUKASI_MANDIRI) }
    @Test fun hypodontia() { cek("hypodontia", sedang, Urgensi.KONTROL_RUTIN); cek("hypodontia", tinggi, Urgensi.KONTROL_RUTIN) }

    // ---- nilai batas ----
    @Test fun tepatDiAmbangYakin_diterima() = cek("Caries", 0.60f, Urgensi.KONTROL_RUTIN)

    @Test fun sedikitDiBawahAmbangYakin_ditolak() {
        val r = TriageEngine.evaluate("Caries", 0.5999f)
        assertTrue(r.ditolak); assertNull(r.urgensi)
    }

    @Test fun tepatDiAmbangTinggi_naikTingkat() = cek("Caries", 0.80f, Urgensi.SEGERA_KE_DOKTER)
    @Test fun sedikitDiBawahAmbangTinggi_masihSedang() = cek("Caries", 0.7999f, Urgensi.KONTROL_RUTIN)
    @Test fun confidenceSatu_valid() = cek("Caries", 1.0f, Urgensi.SEGERA_KE_DOKTER)

    @Test fun confidenceNol_atauNaN_ditolak() {
        assertTrue(TriageEngine.evaluate("Caries", 0f).ditolak)
        assertTrue(TriageEngine.evaluate("Caries", Float.NaN).ditolak)
    }

    // ---- penamaan kelas ----
    @Test fun namaKelasVariasi_tetapCocok() {
        cek("Data caries", tinggi, Urgensi.SEGERA_KE_DOKTER)
        cek("MOUTH_ULCER", sedang, Urgensi.EDUKASI_MANDIRI)
    }

    @Test fun kelasTidakDikenal_defaultKontrolRutin() = cek("penyakit_baru", tinggi, Urgensi.KONTROL_RUTIN)
}