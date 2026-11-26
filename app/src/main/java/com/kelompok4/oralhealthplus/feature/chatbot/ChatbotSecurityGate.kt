package com.kelompok4.oralhealthplus.feature.chatbot

import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * State persistensi keamanan Chatbot per pengguna.
 */
data class SecurityGateState(
    val lastMessageTimestamp: Long = 0L,
    val violationCount: Int = 0,
    val suspensionLevel: Int = 0,
    val suspensionUntil: Long = 0L
)

/**
 * Hasil evaluasi pre-check sebelum pesan diteruskan ke Google Gemini API.
 */
sealed class SecurityEvaluationResult {
    /**
     * Pesan valid dan diizinkan dikirim ke Gemini.
     */
    data class Allowed(val updatedState: SecurityGateState) : SecurityEvaluationResult()

    /**
     * Ditolak karena pengiriman pesan terlalu cepat (< 2 detik cooldown).
     */
    data class RejectedSpam(val waitSeconds: Long, val message: String) : SecurityEvaluationResult()

    /**
     * Ditolak karena topik di luar kesehatan gigi dan mulut (belum mencapai 3 pelanggaran).
     */
    data class RejectedOutOfScope(
        val warningMessage: String,
        val currentViolations: Int,
        val maxViolations: Int,
        val updatedState: SecurityGateState
    ) : SecurityEvaluationResult()

    /**
     * Baru saja terkena pemblokiran karena mencapai kuota pelanggaran.
     */
    data class SuspendedNow(
        val level: Int,
        val isPermanent: Boolean,
        val remainingMillis: Long,
        val formattedRemainingTime: String,
        val message: String,
        val updatedState: SecurityGateState
    ) : SecurityEvaluationResult()

    /**
     * Pengguna masih dalam masa pemblokiran aktif.
     */
    data class Blocked(
        val level: Int,
        val isPermanent: Boolean,
        val remainingMillis: Long,
        val formattedRemainingTime: String,
        val message: String
    ) : SecurityEvaluationResult()
}

/**
 * Gate keamanan cerdas untuk Chatbot OralHealth+:
 * 1. Domain Relevance Gate (Zero-Token Pre-check).
 * 2. Anti-Spam Rate Limiting (Cooldown 2 detik antar pesan).
 * 3. Graduated Suspension Policy:
 *    - Strike 1 & 2: Warning
 *    - Strike 3: Suspensi Level 1 (24 Jam)
 *    - Pelanggaran ulang pasca-Level 1: Suspensi Level 2 (7 Hari)
 *    - Pelanggaran ulang pasca-Level 2: Suspensi Level 3 (Permanen)
 */
object ChatbotSecurityGate {
    const val COOLDOWN_MS = 2000L
    const val MAX_WARNING_STRIKES = 3
    const val SUSPENSION_24_HOURS_MS = 24L * 60L * 60L * 1000L
    const val SUSPENSION_7_DAYS_MS = 7L * 24L * 60L * 60L * 1000L

    private val GREETINGS_AND_COURTESY = listOf(
        "halo", "hallo", "hai", "hi", "hey", "hei", "p", "permisi", "tes", "test",
        "assalamualaikum", "assalamu'alaikum", "selamat", "pagi", "siang", "sore", "malam",
        "makasih", "terima", "kasih", "terimakasih", "banyak", "tq", "thank", "thanks", "oke", "ok",
        "baik", "siap", "dok", "dokter"
    )

    private val ORAL_HEALTH_KEYWORDS = listOf(
        // Organ mulut
        "gigi", "gusi", "mulut", "lidah", "bibir", "rahang", "langit-langit", "tenggorokan",
        "pipi", "enamel", "pulpa", "dentin", "gusi berdarah", "saliva", "air liur",

        // Kondisi & Gejala
        "sakit", "nyeri", "ngilu", "linu", "senut", "berdenyut", "bengkak", "nanah", "abses",
        "sariawan", "radang", "karies", "lubang", "bolong", "karang", "kalkulus", "plak",
        "kuning", "hitam", "bercak", "putih", "bau mulut", "halitosis", "goyang", "longgar",
        "patah", "gompal", "retak", "tanggal", "copot", "lepas", "tumbuh", "impaksi", "bungsu",
        "berdarah", "darah", "sensitif", "hipodontia", "tartar", "karang gigi",

        // Perawatan & Tindakan
        "sikat", "gosok", "odol", "pasta", "floss", "benang", "kumur", "mouthwash",
        "tambal", "tambalan", "behel", "kawat", "aligner", "veneer", "crown", "scaling",
        "cabut", "pencabutan", "bedah", "implan", "akar", "saluran akar", "kuret",
        "bleaching", "fluoride", "siwak", "oral", "dentist", "odontektomi", "periksa", "kontrol",

        // Merek & Produk dari katalog
        "pepsodent", "sensodyne", "formula", "oral-b", "colgate", "ciptadent", "close up"
    )

    /**
     * Memeriksa apakah pesan masih berada dalam domain kesehatan gigi & mulut atau sapaan percakapan.
     */
    fun isDomainRelevant(rawMessage: String): Boolean {
        val normalized = rawMessage.trim().lowercase(Locale.ROOT)
        if (normalized.isEmpty()) return false

        // 1. Cek apakah sapaan pendek/kesopanan
        val words = normalized.split(Regex("[\\s,!?.]+")).filter { it.isNotBlank() }
        if (words.size <= 4 && words.all { GREETINGS_AND_COURTESY.contains(it) }) {
            return true
        }

        // 2. Cek apakah ada kata kunci kesehatan gigi dan mulut
        return ORAL_HEALTH_KEYWORDS.any { keyword ->
            if (keyword.contains(" ")) {
                normalized.contains(keyword)
            } else {
                words.contains(keyword) || normalized.contains(keyword)
            }
        }
    }

    /**
     * Evaluasi menyeluruh pesan pengguna sebelum diteruskan ke Gemini API.
     */
    fun evaluateMessage(
        message: String,
        currentTimeMillis: Long,
        state: SecurityGateState
    ): SecurityEvaluationResult {
        // 1. Cek apakah sedang dalam pemblokiran aktif
        if (state.suspensionUntil > currentTimeMillis) {
            val remaining = state.suspensionUntil - currentTimeMillis
            val isPermanent = state.suspensionUntil == Long.MAX_VALUE
            return SecurityEvaluationResult.Blocked(
                level = state.suspensionLevel,
                isPermanent = isPermanent,
                remainingMillis = remaining,
                formattedRemainingTime = formatRemainingTime(remaining, isPermanent),
                message = buildBlockedMessage(state.suspensionLevel, remaining, isPermanent)
            )
        }

        // 2. Cek Rate Limiting Anti-Spam
        val timeSinceLastMessage = currentTimeMillis - state.lastMessageTimestamp
        if (state.lastMessageTimestamp > 0L && timeSinceLastMessage < COOLDOWN_MS) {
            val remainingCooldownMs = COOLDOWN_MS - timeSinceLastMessage
            val waitSec = ((remainingCooldownMs + 999L) / 1000L).coerceAtLeast(1L)
            return SecurityEvaluationResult.RejectedSpam(
                waitSeconds = waitSec,
                message = "Mohon tunggu $waitSec detik sebelum mengirim pesan berikutnya."
            )
        }

        // 3. Cek Relevansi Domain Gigi & Mulut
        val isRelevant = isDomainRelevant(message)
        if (!isRelevant) {
            // Pelanggaran baru terdeteksi
            val nextLevel = when {
                state.suspensionLevel >= 2 -> 3 // Permanent ban
                state.suspensionLevel == 1 -> 2 // 7 Days ban
                else -> 1                       // First 24 Hours ban (after 3 strikes)
            }

            if (state.suspensionLevel > 0) {
                // Pengguna pernah disuspen sebelumnya, langsung kena hukuman berat pada pelanggaran berikutnya!
                val newSuspensionUntil = if (nextLevel == 3) Long.MAX_VALUE else currentTimeMillis + SUSPENSION_7_DAYS_MS
                val remaining = if (nextLevel == 3) Long.MAX_VALUE else SUSPENSION_7_DAYS_MS
                val isPermanent = nextLevel == 3
                val updated = state.copy(
                    lastMessageTimestamp = currentTimeMillis,
                    violationCount = 0,
                    suspensionLevel = nextLevel,
                    suspensionUntil = newSuspensionUntil
                )
                return SecurityEvaluationResult.SuspendedNow(
                    level = nextLevel,
                    isPermanent = isPermanent,
                    remainingMillis = remaining,
                    formattedRemainingTime = formatRemainingTime(remaining, isPermanent),
                    message = buildBlockedMessage(nextLevel, remaining, isPermanent),
                    updatedState = updated
                )
            }

            // Pelanggaran awal (belum pernah disuspen): hitung sampai 3 strikes
            val newViolations = state.violationCount + 1
            if (newViolations >= MAX_WARNING_STRIKES) {
                // Suspensi Level 1 (24 Jam)
                val newSuspensionUntil = currentTimeMillis + SUSPENSION_24_HOURS_MS
                val updated = state.copy(
                    lastMessageTimestamp = currentTimeMillis,
                    violationCount = 0,
                    suspensionLevel = 1,
                    suspensionUntil = newSuspensionUntil
                )
                return SecurityEvaluationResult.SuspendedNow(
                    level = 1,
                    isPermanent = false,
                    remainingMillis = SUSPENSION_24_HOURS_MS,
                    formattedRemainingTime = formatRemainingTime(SUSPENSION_24_HOURS_MS, false),
                    message = buildBlockedMessage(1, SUSPENSION_24_HOURS_MS, false),
                    updatedState = updated
                )
            } else {
                val remainingWarnings = MAX_WARNING_STRIKES - newViolations
                val updated = state.copy(
                    lastMessageTimestamp = currentTimeMillis,
                    violationCount = newViolations
                )
                return SecurityEvaluationResult.RejectedOutOfScope(
                    warningMessage = "Pesan Anda tidak terkait dengan topik kesehatan gigi dan mulut. Chatbot ini khusus melayani konsultasi gigi. Sisa kesempatan peringatan: $remainingWarnings kali sebelum akses diblokir.",
                    currentViolations = newViolations,
                    maxViolations = MAX_WARNING_STRIKES,
                    updatedState = updated
                )
            }
        }

        // 4. Pesan valid & relevan -> izinkan dan reset violation count sementara
        val updated = state.copy(
            lastMessageTimestamp = currentTimeMillis,
            violationCount = 0
        )
        return SecurityEvaluationResult.Allowed(updatedState = updated)
    }

    fun formatRemainingTime(remainingMillis: Long, isPermanent: Boolean): String {
        if (isPermanent || remainingMillis == Long.MAX_VALUE) {
            return "Permanen"
        }
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60

        return when {
            hours >= 24 -> {
                val days = hours / 24
                val remHours = hours % 24
                if (remHours > 0) "$days hari $remHours jam" else "$days hari"
            }
            hours > 0 -> "$hours jam $minutes menit"
            minutes > 0 -> "$minutes menit $seconds detik"
            else -> "$seconds detik"
        }
    }

    private fun buildBlockedMessage(level: Int, remainingMillis: Long, isPermanent: Boolean): String {
        val timeStr = formatRemainingTime(remainingMillis, isPermanent)
        return when (level) {
            1 -> "Akses Chatbot ditangguhkan sementara selama $timeStr karena 3 kali mengirim pertanyaan di luar topik kesehatan gigi & mulut."
            2 -> "Akses Chatbot ditangguhkan selama $timeStr (Hukuman Berat Tingkat 2) karena kembali melanggar aturan topik setelah penangguhan sebelumnya."
            else -> "Akses Chatbot telah diblokir secara permanen pada perangkat ini karena pelanggaran berulang terhadap aturan penggunaan aplikasi."
        }
    }
}
