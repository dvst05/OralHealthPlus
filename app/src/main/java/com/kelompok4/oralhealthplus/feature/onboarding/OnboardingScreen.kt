package com.kelompok4.oralhealthplus.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kelompok4.oralhealthplus.ui.components.GradientButton
import com.kelompok4.oralhealthplus.ui.theme.OralHealthPlusTheme
import kotlinx.coroutines.launch

/**
 * Onboarding 3 halaman (Figma Tutorial Screen 1–3).
 * Bisa digeser (swipe) atau lewat tombol Next. Tombol "Done" di halaman terakhir memanggil [onSelesai].
 *
 * Contoh di NavHost (Anggota 3):
 *   composable("onboarding") {
 *       OnboardingScreen(onSelesai = {
 *           OnboardingPrefs(context).sudahDilihat = true
 *           navController.navigate("login") { popUpTo("onboarding") { inclusive = true } }
 *       })
 *   }
 */
@Composable
fun OnboardingScreen(
    onSelesai: () -> Unit,
    halaman: List<OnboardingPage> = OnboardingContent.halaman,
    pakaiLottie: Boolean = false, // false = ilustrasi Figma, true = animasi Lottie
) {
    val pagerState = rememberPagerState(pageCount = { halaman.size })
    val scope = rememberCoroutineScope()
    val indeks = pagerState.currentPage

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { posisi ->
            HalamanOnboarding(halaman[posisi], pakaiLottie)
        }

        IndikatorHalaman(total = halaman.size, aktif = indeks)
        Spacer(Modifier.height(20.dp))

        GradientButton(
            text = OnboardingLogic.labelTombol(indeks, halaman.size),
            onClick = {
                if (OnboardingLogic.halamanTerakhir(indeks, halaman.size)) {
                    onSelesai()
                } else {
                    scope.launch { pagerState.animateScrollToPage(OnboardingLogic.halamanBerikutnya(indeks, halaman.size)) }
                }
            },
            modifier = Modifier.width(276.dp).height(48.dp),
        )
    }
}

@Composable
private fun HalamanOnboarding(page: OnboardingPage, pakaiLottie: Boolean) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val ukuran = Modifier.size(width = 276.dp, height = 251.dp) // kotak gambar di Figma
        if (pakaiLottie) {
            // Di Preview Android Studio animasi Lottie tidak tampil; jalankan di emulator untuk melihatnya.
            val komposisi by rememberLottieComposition(LottieCompositionSpec.RawRes(page.animasi))
            LottieAnimation(composition = komposisi, iterations = LottieConstants.IterateForever, modifier = ukuran)
        } else {
            Image(painterResource(page.gambar), contentDescription = null, contentScale = ContentScale.Fit, modifier = ukuran)
        }
        Spacer(Modifier.height(40.dp))
        Text(
            page.judul,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            page.deskripsi,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), // Figma: Poppins Light 12
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(318.dp),
        )
    }
}

@Composable
private fun IndikatorHalaman(total: Int, aktif: Int) {
    Row(
        Modifier.semantics { contentDescription = "Halaman ${aktif + 1} dari $total" },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            val lebar by animateDpAsState(if (i == aktif) 24.dp else 8.dp, label = "lebarTitik")
            val warna by animateColorAsState(
                if (i == aktif) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                label = "warnaTitik",
            )
            Box(Modifier.height(8.dp).width(lebar).clip(CircleShape).background(warna))
        }
    }
}

@Preview(name = "Onboarding", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun PreviewOnboarding() = OralHealthPlusTheme {
    Box(Modifier.fillMaxWidth()) { OnboardingScreen(onSelesai = {}) }
}
