package com.kelompok4.oralhealthplus.feature.auth

/**
 * Aturan kuota akses untuk pengguna Tamu (belum login).
 * - Tamu hanya boleh melakukan 1x scan foto dan 1x analisis.
 * - Tamu hanya boleh mengajukan maksimal 3 pertanyaan di Chatbot.
 * - Pengguna yang sudah login memiliki akses tidak terbatas.
 */
object GuestAccessPolicy {
    const val MAX_GUEST_SCANS = 1
    const val MAX_GUEST_ANALYSES = 1
    const val MAX_GUEST_CHAT_QUESTIONS = 3

    const val MSG_SCAN_LIMIT_REACHED =
        "Kuota 1x scan foto tamu sudah terpakai. Silakan lanjutkan analisis foto ini atau masuk/daftar akun untuk scan ulang."
    const val MSG_ANALYSIS_LIMIT_REACHED =
        "Anda telah menggunakan 1x kuota scan dan analisis gratis untuk tamu. Silakan masuk atau daftar akun untuk analisis lebih lanjut."
    const val MSG_CHAT_LIMIT_REACHED =
        "Batas 3 pertanyaan tamu di Chatbot telah tercapai. Silakan masuk atau daftar akun gratis untuk melanjutkan konsultasi."
}

data class GuestQuotaState(
    val isLoggedIn: Boolean,
    val scanCount: Int = 0,
    val analysisCount: Int = 0,
    val chatQuestionCount: Int = 0
) {
    val canScanPhoto: Boolean
        get() = isLoggedIn || (scanCount < GuestAccessPolicy.MAX_GUEST_SCANS && analysisCount < GuestAccessPolicy.MAX_GUEST_ANALYSES)

    val canAnalyzePhoto: Boolean
        get() = isLoggedIn || analysisCount < GuestAccessPolicy.MAX_GUEST_ANALYSES

    val canStartNewScanFlow: Boolean
        get() = isLoggedIn || analysisCount < GuestAccessPolicy.MAX_GUEST_ANALYSES

    val canAskChatbot: Boolean
        get() = isLoggedIn || chatQuestionCount < GuestAccessPolicy.MAX_GUEST_CHAT_QUESTIONS

    val remainingGuestChats: Int
        get() = if (isLoggedIn) Int.MAX_VALUE else (GuestAccessPolicy.MAX_GUEST_CHAT_QUESTIONS - chatQuestionCount).coerceAtLeast(0)

    val remainingGuestAnalyses: Int
        get() = if (isLoggedIn) Int.MAX_VALUE else (GuestAccessPolicy.MAX_GUEST_ANALYSES - analysisCount).coerceAtLeast(0)
}
