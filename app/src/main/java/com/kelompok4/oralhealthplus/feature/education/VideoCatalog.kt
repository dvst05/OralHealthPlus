package com.kelompok4.oralhealthplus.feature.education

/**
 * Daftar video edukasi YouTube. Isinya diambil dari VideoData pada repository asli OralHealth+,
 * deskripsinya dipersingkat. Video tidak punya tanggal terbit, jadi bonus kebaruannya selalu 0.
 */
object VideoCatalog {

    private fun video(id: String, judul: String, deskripsi: String) = EducationContent(
        judul = judul,
        deskripsi = deskripsi,
        url = "https://www.youtube.com/watch?v=$id",
        tipe = ContentType.VIDEO,
        sumber = "YouTube",
        gambarUrl = "https://img.youtube.com/vi/$id/hqdefault.jpg",
        publishedAtMillis = null,
    )

    val semua: List<EducationContent> = listOf(
        video(
            "DdVTN0bU7gI", "Cara Menyikat Gigi yang Benar",
            "Panduan cara menyikat gigi yang baik dan benar untuk menjaga kebersihan mulut.",
        ),
        video(
            "WWsnzBZyhIc", "Cara Menghilangkan Bau Mulut!",
            "Bau mulut di dunia kedokteran disebut halitosis. Halitosis bisa disebabkan masalah di dalam rongga mulut atau penyakit lain.",
        ),
        video(
            "TYwOfWZVnz0", "Cara Membersihkan Lidah, Kebiasaan Baik untuk Cegah Bau Mulut",
            "Lidah yang kotor dapat menjadi sarang bakteri dan membuat napas menjadi bau.",
        ),
        video(
            "5J89gCDt_rk", "The Perfect Oral Health Care Routine (3 easy steps)",
            "The best order for brushing, flossing, and tongue scraping, plus antiseptic and anti-cavity mouthwashes.",
        ),
        video(
            "6r3FW_LDj8U", "Setup Your Oral Hygiene Routine",
            "Everything you need to know about caring for your teeth at home with simple daily tips.",
        ),
        video(
            "dp23sQ6MNvs", "8 Ways To Keep Your Teeth Healthy",
            "Regular brushing and cleaning between the teeth help prevent dental disease and bad breath. The most common dental diseases are tooth decay (cavities, dental caries) and gum disease, including gingivitis and periodontitis.",
        ),
        video(
            "BapR9J86ZZw", "How to Brush Your Teeth Animation",
            "An animation that teaches the proper technique to brush your teeth.",
        ),
    )
}
