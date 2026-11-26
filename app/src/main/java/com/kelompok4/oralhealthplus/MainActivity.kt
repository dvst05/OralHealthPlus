package com.kelompok4.oralhealthplus

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import com.kelompok4.oralhealthplus.data.local.AppDatabase
import com.kelompok4.oralhealthplus.data.local.SessionManager
import com.kelompok4.oralhealthplus.data.repository.AuthRepository
import com.kelompok4.oralhealthplus.data.repository.HistoryRepository
import com.kelompok4.oralhealthplus.feature.auth.AuthViewModel
import com.kelompok4.oralhealthplus.feature.chatbot.AccountRoute
import com.kelompok4.oralhealthplus.feature.chatbot.ChatbotRoute
import com.kelompok4.oralhealthplus.feature.chatbot.ChatbotViewModel
import com.kelompok4.oralhealthplus.feature.chatbot.HistoryReportRoute
import com.kelompok4.oralhealthplus.feature.education.EducationRoute
import com.kelompok4.oralhealthplus.feature.history.HistoryViewModel
import com.kelompok4.oralhealthplus.feature.home.HomeScreen
import com.kelompok4.oralhealthplus.feature.onboarding.OnboardingPrefs
import com.kelompok4.oralhealthplus.feature.onboarding.OnboardingScreen
import com.kelompok4.oralhealthplus.ui.theme.OralHealthPlusTheme
import com.kelompok4.oralhealthplus.feature.clinic.ClinicRoute
import com.kelompok4.oralhealthplus.feature.clinic.ClinicViewModel

enum class AppTab {
    HOME,
    CHATBOT,
    CLINIC,
    HISTORY,
    ACCOUNT
}

/** Layar penuh tanpa bottom bar (dibuka di atas tab). */
sealed interface LayarPenuh {
    data object Onboarding : LayarPenuh
    data class Edukasi(val tabAwal: Int) : LayarPenuh // 0 = Artikel, 1 = Video
}

private val NavPrimaryBlue = Color(0xFF4A628A)
private val NavBorderHairline = Color(0xFFE2E8F0)
private val NavTextMuted = Color(0xFF475569)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        setContent {
            OralHealthPlusTheme {
                MainAppHost()
            }
        }
    }
}

@Composable
fun MainAppHost() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val sessionManager = remember { SessionManager(context) }
    val onboardingPrefs = remember { OnboardingPrefs(context) }

    val authRepo = remember { AuthRepository(db.userDao(), sessionManager) }
    val historyRepo = remember { HistoryRepository(db.detectionHistoryDao(), sessionManager) }

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(authRepo))
    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(historyRepo))
    val chatbotViewModel: ChatbotViewModel = viewModel(
        factory = ChatbotViewModel.Factory(
            historyRepository = historyRepo,
            chatMessageDao = db.chatMessageDao(),
            sessionManager = sessionManager
        )
    )

    val clinicViewModel: ClinicViewModel = viewModel()

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            clinicViewModel.onPermissionResult(true)
        }
    }

    var currentTab by remember { mutableStateOf(AppTab.HOME) }

    // Onboarding hanya muncul saat aplikasi pertama kali dibuka
    var layarPenuh by remember {
        mutableStateOf<LayarPenuh?>(if (onboardingPrefs.sudahDilihat) null else LayarPenuh.Onboarding)
    }

    // Tombol back HP menutup layar penuh (kecuali Onboarding) dan kembali ke tab
    BackHandler(enabled = layarPenuh is LayarPenuh.Edukasi) { layarPenuh = null }

    // ---------- Layar penuh (tanpa bottom bar) ----------
    when (val layar = layarPenuh) {
        LayarPenuh.Onboarding -> {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
                OnboardingScreen(onSelesai = {
                    onboardingPrefs.sudahDilihat = true
                    layarPenuh = null
                    currentTab = AppTab.HOME
                })
            }
            return
        }
        is LayarPenuh.Edukasi -> {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
                EducationRoute(
                    ambilLabelTerakhir = { historyRepo.getLatestDetection()?.kelas },
                    tabAwal = layar.tabAwal,
                    onBack = { layarPenuh = null },
                    // TODO: arahkan ke layar Deteksi setelah DetectionRoute (Anggota 2) disambungkan
                    onCekMulut = { layarPenuh = null; currentTab = AppTab.HOME },
                )
            }
            return
        }
        null -> Unit
    }

    // ---------- Tab dengan bottom bar ----------
    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavPrimaryBlue,
        selectedTextColor = NavPrimaryBlue,
        indicatorColor = NavPrimaryBlue.copy(alpha = 0.12f),
        unselectedIconColor = NavTextMuted,
        unselectedTextColor = NavTextMuted
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column {
                HorizontalDivider(color = NavBorderHairline)
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.HOME,
                        onClick = { currentTab = AppTab.HOME },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.CHATBOT,
                        onClick = { currentTab = AppTab.CHATBOT },
                        icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chatbot") },
                        label = { Text("Chatbot") },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.CLINIC,
                        onClick = { currentTab = AppTab.CLINIC },
                        icon = { Icon(Icons.Filled.LocalHospital, contentDescription = "Klinik") },
                        label = { Text("Klinik") },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.HISTORY,
                        onClick = { currentTab = AppTab.HISTORY },
                        icon = { Icon(Icons.Filled.Assessment, contentDescription = "Riwayat") },
                        label = { Text("Riwayat") },
                        colors = navItemColors
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.ACCOUNT,
                        onClick = { currentTab = AppTab.ACCOUNT },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Akun") },
                        label = { Text("Akun") },
                        colors = navItemColors
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> {
                    HomeScreen(
                        namaPengguna = sessionManager.getDisplayName(),
                        // TODO: arahkan ke layar Deteksi setelah DetectionRoute (Anggota 2) disambungkan
                        onScan = { },
                        onClinic = { currentTab = AppTab.CLINIC },
                        onVideos = { layarPenuh = LayarPenuh.Edukasi(tabAwal = 1) },
                        onArticles = { layarPenuh = LayarPenuh.Edukasi(tabAwal = 0) },
                        onReport = { currentTab = AppTab.HISTORY },
                        onChatbot = { currentTab = AppTab.CHATBOT },
                        onPengaturan = { currentTab = AppTab.ACCOUNT },
                        onToothpaste = null, // kartu pasta gigi disembunyikan sampai kelompok memutuskan
                    )
                }
                AppTab.CHATBOT -> {
                    ChatbotRoute(vm = chatbotViewModel)
                }
                AppTab.CLINIC -> {
                    ClinicRoute(vm = clinicViewModel)
                }
                AppTab.HISTORY -> {
                    HistoryReportRoute(
                        vm = historyViewModel,
                        onConsultChatbot = { currentTab = AppTab.CHATBOT }
                    )
                }
                AppTab.ACCOUNT -> {
                    AccountRoute(
                        vm = authViewModel,
                        onOpenChatbot = { currentTab = AppTab.CHATBOT },
                        onOpenHistory = { currentTab = AppTab.HISTORY }
                    )
                }
            }
        }
    }
}git checkout main