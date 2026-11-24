package com.kelompok4.oralhealthplus.feature.onboarding

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.kelompok4.oralhealthplus.R

/**
 * Satu halaman onboarding (Figma Tutorial Screen 1–3).
 * [gambar] = ilustrasi dokter dari Figma, [animasi] = animasi Lottie (dipakai jika pakaiLottie = true).
 */
data class OnboardingPage(
    @DrawableRes val gambar: Int,
    @RawRes val animasi: Int,
    val judul: String,
    val deskripsi: String,
)

object OnboardingContent {
    val halaman = listOf(
        OnboardingPage(
            R.drawable.onboarding_1, // dokter membawa suntikan
            R.raw.onboarding_1,
            "Welcome!",
            "Welcome to Oral Disease App, This app is designed to help you maintain your oral health easily. Let's get started!",
        ),
        OnboardingPage(
            R.drawable.onboarding_2, // dokter memegang clipboard
            R.raw.onboarding_2,
            "Get to Know the Features",
            "Take a photo of your mouth for a quick oral health check. Get daily tips to maintain your oral hygiene.",
        ),
        OnboardingPage(
            R.drawable.onboarding_3, // dokter dengan sabun dan perisai
            R.raw.onboarding_3,
            "Enjoy The App!",
            "Ready to take care of your oral health? Start by choosing the feature you want to use. Feel free to explore the app further!",
        ),
    )
}

/** Logika tombol dipisah dari UI supaya bisa diuji dengan unit test biasa. */
object OnboardingLogic {
    fun halamanTerakhir(indeks: Int, total: Int): Boolean = indeks >= total - 1

    fun labelTombol(indeks: Int, total: Int): String = if (halamanTerakhir(indeks, total)) "Done" else "Next"

    fun halamanBerikutnya(indeks: Int, total: Int): Int = (indeks + 1).coerceAtMost(total - 1)
}

/**
 * Menyimpan apakah onboarding sudah pernah dilihat, supaya hanya muncul sekali (saat pertama install).
 * Dipakai Anggota 3 di NavHost untuk menentukan startDestination.
 */
class OnboardingPrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("onboarding", Context.MODE_PRIVATE)

    var sudahDilihat: Boolean
        get() = prefs.getBoolean(KEY_SUDAH_DILIHAT, false)
        set(value) = prefs.edit().putBoolean(KEY_SUDAH_DILIHAT, value).apply()

    private companion object {
        const val KEY_SUDAH_DILIHAT = "sudah_dilihat"
    }
}
