package com.kelompok4.oralhealthplus.feature.doctor

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kelompok4.oralhealthplus.data.local.entity.DetectionHistoryEntity
import java.net.URLEncoder
import java.util.Locale

data class DentalDoctor(
    val id: String,
    val name: String,
    val specialization: String,
    val clinicName: String,
    val experienceYears: Int,
    val rating: Double,
    val schedule: String,
    val isAvailableNow: Boolean,
    /** Format internasional tanpa '+' (misal: 6281234567890) untuk deep-link WhatsApp wa.me */
    val whatsappNumber: String,
    val recommendedConditions: Set<String> = emptySet()
)

/**
 * Katalog daftar dummy dokter gigi untuk fitur konsultasi langsung via WhatsApp
 * sesuai spesifikasi dokumen UTP OralHealth+ Kelompok 4.
 *
 * Catatan: Nomor WhatsApp di bawah menggunakan nomor dummy Indonesia (62812345678xx).
 * Anda dapat mengganti `DEFAULT_DUMMY_WHATSAPP` atau nomor masing-masing dokter di sini.
 */
object DoctorCatalog {

    const val DEFAULT_DUMMY_WHATSAPP = "6281234567890"

    val doctors: List<DentalDoctor> = listOf(
        DentalDoctor(
            id = "drg_nadya",
            name = "drg. Nadya Prameswari, Sp.KG",
            specialization = "Spesialis Konservasi Gigi (Tambal & Saluran Akar)",
            clinicName = "Brawijaya Dental Care, Malang",
            experienceYears = 9,
            rating = 4.9,
            schedule = "Senin–Sabtu, 09.00–20.00",
            isAvailableNow = true,
            whatsappNumber = DEFAULT_DUMMY_WHATSAPP,
            recommendedConditions = setOf("caries", "toothdiscoloration")
        ),
        DentalDoctor(
            id = "drg_reza",
            name = "drg. Reza Mahendra, Sp.Perio",
            specialization = "Spesialis Periodonsia (Gusi & Karang Gigi)",
            clinicName = "RSGM Universitas Brawijaya",
            experienceYears = 11,
            rating = 4.8,
            schedule = "Senin–Jumat, 08.00–17.00",
            isAvailableNow = true,
            whatsappNumber = "6281234567891",
            recommendedConditions = setOf("gingivitis", "calculus")
        ),
        DentalDoctor(
            id = "drg_dimas",
            name = "drg. Dimas Pratama, Sp.BM",
            specialization = "Spesialis Bedah Mulut & Kasus Darurat",
            clinicName = "Klinik Gigi Ijen Medika, Malang",
            experienceYears = 12,
            rating = 4.9,
            schedule = "Senin–Minggu, 08.00–21.00",
            isAvailableNow = true,
            whatsappNumber = "6281234567892",
            recommendedConditions = setOf("caries", "mouthulcer", "hypodontia")
        ),
        DentalDoctor(
            id = "drg_clara",
            name = "drg. Clara Kusuma",
            specialization = "Dokter Gigi Umum & Perawatan Estetika",
            clinicName = "Dental Studio Soekarno-Hatta",
            experienceYears = 6,
            rating = 4.7,
            schedule = "Selasa–Minggu, 10.00–20.00",
            isAvailableNow = true,
            whatsappNumber = "6281234567893",
            recommendedConditions = setOf("calculus", "toothdiscoloration", "mouthulcer")
        ),
        DentalDoctor(
            id = "drg_siti",
            name = "drg. Siti Rahmawati, Sp.Ort",
            specialization = "Spesialis Ortodonsia (Susunan Gigi & Rahang)",
            clinicName = "Klinik Senyum Sehat Dinoyo",
            experienceYears = 8,
            rating = 4.8,
            schedule = "Senin–Sabtu, 13.00–20.00",
            isAvailableNow = false,
            whatsappNumber = "6281234567894",
            recommendedConditions = setOf("hypodontia")
        )
    )

    fun normalizeConditionKey(kelas: String?): String =
        kelas.orEmpty().lowercase(Locale.ROOT).replace(Regex("[^a-z]"), "")

    fun isRecommendedFor(doctor: DentalDoctor, kelas: String?): Boolean {
        val key = normalizeConditionKey(kelas)
        if (key.isEmpty()) return false
        return doctor.recommendedConditions.any { key.contains(it) }
    }

    /**
     * Mengurutkan daftar dokter agar spesialis yang cocok dengan hasil scan terakhir tampil di atas,
     * diikuti dokter yang sedang praktik saat ini dan rating tertinggi.
     */
    fun getSortedDoctors(latestCondition: String? = null): List<DentalDoctor> {
        return doctors.sortedWith(
            compareByDescending<DentalDoctor> { isRecommendedFor(it, latestCondition) }
                .thenByDescending { it.isAvailableNow }
                .thenByDescending { it.rating }
        )
    }

    fun buildWhatsAppMessage(
        doctor: DentalDoctor,
        latestDetection: DetectionHistoryEntity? = null,
        severeContext: String? = null
    ): String {
        val sb = StringBuilder()
        sb.append("Halo ${doctor.name}, saya pengguna aplikasi OralHealth+. ")
        if (latestDetection != null) {
            val kondisi = latestDetection.kelas.replace("_", " ")
            val confidencePct = String.format(Locale.US, "%.0f%%", latestDetection.confidence * 100f)
            val urgensiLabel = when (latestDetection.urgensi.uppercase(Locale.ROOT)) {
                "SEGERA_KE_DOKTER" -> "Segera ke Dokter"
                "KONTROL_RUTIN" -> "Kontrol Rutin"
                else -> "Edukasi Mandiri"
            }
            sb.append("Saya baru saja melakukan skrining mandiri dengan hasil dugaan $kondisi (keyakinan $confidencePct, urgensi: $urgensiLabel). ")
            if (latestDetection.catatanKeluhan.isNotBlank()) {
                sb.append("Keluhan tambahan: ${latestDetection.catatanKeluhan}. ")
            }
        }
        if (!severeContext.isNullOrBlank()) {
            sb.append("Catatan gejala berat: $severeContext ")
        }
        sb.append("Apakah saya bisa berkonsultasi atau membuat jadwal pemeriksaan?")
        return sb.toString().trim()
    }

    fun buildWhatsAppUrl(
        doctor: DentalDoctor,
        latestDetection: DetectionHistoryEntity? = null,
        severeContext: String? = null
    ): String {
        val cleanNumber = doctor.whatsappNumber.filter { it.isDigit() }
        val message = buildWhatsAppMessage(doctor, latestDetection, severeContext)
        val encoded = URLEncoder.encode(message, "UTF-8")
        return "https://wa.me/$cleanNumber?text=$encoded"
    }

    fun openDoctorWhatsApp(
        context: Context,
        doctor: DentalDoctor,
        latestDetection: DetectionHistoryEntity? = null,
        severeContext: String? = null
    ) {
        val url = buildWhatsAppUrl(doctor, latestDetection, severeContext)
        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(waIntent)
        }.onFailure {
            val cleanNumber = doctor.whatsappNumber.filter { it.isDigit() }
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+$cleanNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(dialIntent) }
        }
    }
}
