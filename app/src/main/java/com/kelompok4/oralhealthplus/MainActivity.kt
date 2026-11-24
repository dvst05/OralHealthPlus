package com.kelompok4.oralhealthplus

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.kelompok4.oralhealthplus.data.local.AppDatabase
import com.kelompok4.oralhealthplus.data.local.SessionManager
import com.kelompok4.oralhealthplus.data.repository.AuthRepository
import com.kelompok4.oralhealthplus.data.repository.HistoryRepository
import com.kelompok4.oralhealthplus.feature.auth.AuthViewModel
import com.kelompok4.oralhealthplus.feature.chatbot.AccountRoute
import com.kelompok4.oralhealthplus.feature.chatbot.ChatbotRoute
import com.kelompok4.oralhealthplus.feature.chatbot.ChatbotViewModel
import com.kelompok4.oralhealthplus.feature.chatbot.HistoryReportRoute
import com.kelompok4.oralhealthplus.feature.history.HistoryViewModel
import com.kelompok4.oralhealthplus.ui.theme.OralHealthPlusTheme
import com.kelompok4.oralhealthplus.feature.clinic.ClinicRoute

enum class AppTab {
    CHATBOT,
    CLINIC,
    HISTORY,
    ACCOUNT
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

    var currentTab by remember { mutableStateOf(AppTab.CHATBOT) }

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
                AppTab.CHATBOT -> {
                    ChatbotRoute(vm = chatbotViewModel)
                }
                AppTab.CLINIC -> {
                    ClinicRoute()
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
}

