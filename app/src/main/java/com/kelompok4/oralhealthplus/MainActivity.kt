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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import com.kelompok4.oralhealthplus.data.local.AppDatabase
import com.kelompok4.oralhealthplus.data.local.SessionManager
import com.kelompok4.oralhealthplus.data.repository.AuthRepository
import com.kelompok4.oralhealthplus.data.repository.HistoryRepository
import com.kelompok4.oralhealthplus.feature.auth.AuthViewModel
import com.kelompok4.oralhealthplus.feature.auth.GuestAccessPolicy
import com.kelompok4.oralhealthplus.feature.auth.GuestQuotaState
import com.kelompok4.oralhealthplus.feature.chatbot.AccountRoute
import com.kelompok4.oralhealthplus.feature.chatbot.ChatbotRoute
import com.kelompok4.oralhealthplus.feature.chatbot.ChatbotViewModel
import com.kelompok4.oralhealthplus.feature.chatbot.HistoryReportRoute
import com.kelompok4.oralhealthplus.feature.detection.DetectionRoute
import com.kelompok4.oralhealthplus.feature.detection.DetectionViewModel
import com.kelompok4.oralhealthplus.feature.education.EducationRoute
import com.kelompok4.oralhealthplus.feature.education.EducationViewModel
import com.kelompok4.oralhealthplus.feature.encyclopedia.EncyclopediaDetailScreen
import com.kelompok4.oralhealthplus.feature.encyclopedia.EncyclopediaEntry
import com.kelompok4.oralhealthplus.feature.encyclopedia.EncyclopediaListScreen
import com.kelompok4.oralhealthplus.feature.encyclopedia.EncyclopediaRepository
import com.kelompok4.oralhealthplus.feature.history.HistoryViewModel
import com.kelompok4.oralhealthplus.feature.home.HomeScreen
import com.kelompok4.oralhealthplus.feature.onboarding.OnboardingPrefs
import com.kelompok4.oralhealthplus.feature.onboarding.OnboardingScreen
import com.kelompok4.oralhealthplus.ui.theme.OralHealthPlusTheme
import com.kelompok4.oralhealthplus.feature.clinic.ClinicRoute
import com.kelompok4.oralhealthplus.feature.clinic.ClinicSection
import com.kelompok4.oralhealthplus.feature.clinic.ClinicViewModel
import com.kelompok4.oralhealthplus.ui.theme.BluePrimary
import com.kelompok4.oralhealthplus.ui.theme.SurfaceBlue
import com.kelompok4.oralhealthplus.ui.components.GradientFilledButton

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
    data object Ensiklopedia : LayarPenuh
}

private val NavPrimaryBlue = BluePrimary
private val NavBorderHairline = SurfaceBlue
private val NavTextMuted = BluePrimary

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
    val detectionViewModel: DetectionViewModel = viewModel(
        factory = DetectionViewModel.Factory(context) { historyRepo.simpanHasil(it) }
    )

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

    val loggedInUsername by sessionManager.loggedInUsernameFlow.collectAsState()
    val displayName by sessionManager.displayNameFlow.collectAsState()
    val profilePhotoPath by sessionManager.profilePhotoPathFlow.collectAsState()
    val guestScanCount by sessionManager.guestScanCountFlow.collectAsState()
    val guestAnalysisCount by sessionManager.guestAnalysisCountFlow.collectAsState()
    val guestChatCount by sessionManager.guestChatCountFlow.collectAsState()
    val latestDetection by chatbotViewModel.latestDetection.collectAsState()
    val chatbotUiState by chatbotViewModel.uiState.collectAsState()
    val detectionFoto by detectionViewModel.foto.collectAsState()

    val quotaState = GuestQuotaState(
        isLoggedIn = loggedInUsername != null,
        scanCount = guestScanCount,
        analysisCount = guestAnalysisCount,
        chatQuestionCount = guestChatCount
    )

    // rememberSaveable: posisi tidak hilang saat layar diputar atau sepulang dari kamera
    var currentTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    var clinicSection by rememberSaveable { mutableStateOf(ClinicSection.CLINICS) }
    var diDeteksi by rememberSaveable { mutableStateOf(false) } // Fungsi 1, layar penuh
    var loginGateReason by remember { mutableStateOf<String?>(null) }

    // Onboarding hanya muncul saat aplikasi pertama kali dibuka
    var layarPenuh by remember {
        mutableStateOf<LayarPenuh?>(if (onboardingPrefs.sudahDilihat) null else LayarPenuh.Onboarding)
    }

    fun openScanFlow() {
        if (!quotaState.canStartNewScanFlow && detectionFoto == null) {
            loginGateReason = GuestAccessPolicy.MSG_ANALYSIS_LIMIT_REACHED
        } else {
            layarPenuh = null
            diDeteksi = true
        }
    }

    fun openLoginFromGate() {
        loginGateReason = null
        diDeteksi = false
        layarPenuh = null
        currentTab = AppTab.ACCOUNT
    }

    fun openDoctorsSection() {
        diDeteksi = false
        layarPenuh = null
        clinicSection = ClinicSection.DOCTORS
        currentTab = AppTab.CLINIC
    }

    fun openClinicsSection() {
        diDeteksi = false
        layarPenuh = null
        clinicSection = ClinicSection.CLINICS
        currentTab = AppTab.CLINIC
    }

    // Tombol back HP menutup layar penuh (kecuali Onboarding) dan kembali ke tab
    BackHandler(enabled = layarPenuh is LayarPenuh.Edukasi) { layarPenuh = null }

    Box(Modifier.fillMaxSize()) {
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
            }
            is LayarPenuh.Edukasi -> {
                val ambilLabel: suspend () -> String? = { historyRepo.getLatestDetection()?.kelas }
                // Instance yang sama dengan milik EducationRoute; muat ulang agar hasil deteksi terbaru terpakai
                val educationViewModel: EducationViewModel =
                    viewModel(factory = EducationViewModel.factory(context, ambilLabel))
                LaunchedEffect(Unit) { educationViewModel.muat() }
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
                    EducationRoute(
                        ambilLabelTerakhir = ambilLabel,
                        tabAwal = layar.tabAwal,
                        onBack = { layarPenuh = null },
                        onCekMulut = { openScanFlow() },
                    )
                }
            }
            LayarPenuh.Ensiklopedia -> {
                val daftarPenyakit = remember { EncyclopediaRepository.dariAssets(context).semua() }
                var selectedEntry by remember { mutableStateOf<EncyclopediaEntry?>(null) }
                BackHandler {
                    if (selectedEntry != null) {
                        selectedEntry = null
                    } else {
                        layarPenuh = null
                    }
                }
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
                    val activeEntry = selectedEntry
                    if (activeEntry != null) {
                        EncyclopediaDetailScreen(
                            entri = activeEntry,
                            onBack = { selectedEntry = null }
                        )
                    } else {
                        EncyclopediaListScreen(
                            daftar = daftarPenyakit,
                            onPilih = { selectedEntry = it },
                            onBack = { layarPenuh = null }
                        )
                    }
                }
            }
            null -> if (diDeteksi) {
                // Dipasang sebelum DetectionRoute: back dari layar hasil ditangani rute itu lebih dulu
                BackHandler { diDeteksi = false }
                Box(Modifier.fillMaxSize().background(Color.White)) {
                    DetectionRoute(
                        vm = detectionViewModel,
                        onBack = { diDeteksi = false },
                        onLihatEdukasi = { layarPenuh = LayarPenuh.Edukasi(tabAwal = 0) },
                        onTanyaChatbot = {
                            chatbotViewModel.useLatestDetectionReference()
                            diDeteksi = false
                            currentTab = AppTab.CHATBOT
                        },
                        onCariKlinik = { openClinicsSection() },
                        onHubungiDokter = { openDoctorsSection() },
                        onSelesai = { diDeteksi = false; currentTab = AppTab.HOME },
                        isGuest = !quotaState.isLoggedIn,
                        remainingGuestAnalyses = quotaState.remainingGuestAnalyses,
                        canScanPhoto = quotaState.canScanPhoto,
                        canAnalyzePhoto = quotaState.canAnalyzePhoto,
                        onRecordGuestScan = sessionManager::recordGuestScan,
                        onRecordGuestAnalysis = sessionManager::recordGuestAnalysis,
                        onRequireLogin = { reason -> loginGateReason = reason }
                    )
                }
            } else {
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
                                    namaPengguna = displayName.ifBlank { loggedInUsername ?: "Tamu" },
                                    onScan = { openScanFlow() },
                                    onClinic = { openClinicsSection() },
                                    onVideos = { layarPenuh = LayarPenuh.Edukasi(tabAwal = 1) },
                                    onArticles = { layarPenuh = LayarPenuh.Edukasi(tabAwal = 0) },
                                    onReport = { currentTab = AppTab.HISTORY },
                                    onChatbot = { currentTab = AppTab.CHATBOT },
                                    onPengaturan = { currentTab = AppTab.ACCOUNT },
                                    onToothpaste = null, // kartu pasta gigi disembunyikan sampai kelompok memutuskan
                                    onDoctors = { openDoctorsSection() },
                                    onEncyclopedia = { layarPenuh = LayarPenuh.Ensiklopedia },
                                    profilePhotoPath = profilePhotoPath
                                )
                            }
                            AppTab.CHATBOT -> {
                                ChatbotRoute(
                                    vm = chatbotViewModel,
                                    onOpenLogin = { openLoginFromGate() },
                                    onOpenScan = { openScanFlow() },
                                    onOpenDoctors = { openDoctorsSection() },
                                    onOpenClinics = { openClinicsSection() }
                                )
                            }
                            AppTab.CLINIC -> {
                                ClinicRoute(
                                    vm = clinicViewModel,
                                    initialSection = clinicSection,
                                    latestDetection = latestDetection,
                                    severeContext = if (chatbotUiState.isSevereCaseDetected) {
                                        chatbotUiState.severeCaseReason
                                    } else {
                                        null
                                    }
                                )
                            }
                            AppTab.HISTORY -> {
                                HistoryReportRoute(
                                    vm = historyViewModel,
                                    onConsultChatbot = { item ->
                                        chatbotViewModel.selectDetectionReference(item)
                                        currentTab = AppTab.CHATBOT
                                    }
                                )
                            }
                            AppTab.ACCOUNT -> {
                                AccountRoute(
                                    vm = authViewModel,
                                    onOpenChatbot = { currentTab = AppTab.CHATBOT },
                                    onOpenHistory = { currentTab = AppTab.HISTORY },
                                    onOpenOnboarding = { layarPenuh = LayarPenuh.Onboarding }
                                )
                            }
                        }
                    }
                }
            }
        }

        val gateMessage = loginGateReason
        if (gateMessage != null) {
            AlertDialog(
                onDismissRequest = { loginGateReason = null },
                title = { Text("Perlu Masuk Akun", fontWeight = FontWeight.SemiBold) },
                text = { Text(gateMessage) },
                confirmButton = {
                    GradientFilledButton(
                        onClick = { openLoginFromGate() },
                    ) {
                        Text("Masuk / Daftar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { loginGateReason = null }) {
                        Text("Nanti Saja", color = NavTextMuted)
                    }
                }
            )
        }
    }
}