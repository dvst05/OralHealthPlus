package com.kelompok4.oralhealthplus.feature.detection

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

/**
 * SATU rute NavHost untuk seluruh fungsi 1 (Oral Check -> Analyze -> Result / Rejected).
 * Anggota 3 cukup memasang: composable("deteksi") { DetectionRoute(vm, ...) }.
 *
 * Galeri memakai Photo Picker sistem (tanpa izin storage). Kamera memakai TakePicturePreview
 * (tanpa FileProvider, hasilnya Bitmap kecil; cukup untuk model 224x224).
 * JANGAN deklarasikan <uses-permission CAMERA> di manifest: kontrak ini hanya jalan tanpa
 * izin itu, kalau dideklarasikan aplikasi harus meminta izin runtime lebih dulu.
 */
@Composable
fun DetectionRoute(
    vm: DetectionViewModel,
    onBack: () -> Unit,
    onLihatEdukasi: (kelas: String) -> Unit,
    onTanyaChatbot: () -> Unit,
    onCariKlinik: () -> Unit,
    onSelesai: () -> Unit,
    rekomendasi: List<EduItem> = emptyList(),
    onEduItemClick: (EduItem) -> Unit = {}
) {
    val state by vm.state.collectAsState()
    val foto by vm.foto.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pesanGalat by remember { mutableStateOf<String?>(null) }

    val galeri = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            runCatching { muatBitmap(context, uri) }
                .onSuccess { pesanGalat = null; vm.pilihFoto(it) }
                .onFailure { pesanGalat = "Couldn't open that photo. Try another one." }
        }
    }
    val kamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) { pesanGalat = null; vm.pilihFoto(bmp) }
    }

    // Back dari Result/Rejected/Error = ulang dari awal (bukan keluar fitur)
    BackHandler(
        enabled = state is DetectionUiState.Success || state is DetectionUiState.Rejected ||
            state is DetectionUiState.Error
    ) { vm.ulangiFoto() }

    when (val s = state) {
        DetectionUiState.Idle -> OralCheckScreen(
            foto = foto, pesanGalat = pesanGalat, onBack = onBack,
            onTakePhoto = { kamera.launch(null) },
            onGallery = { galeri.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onChangePhoto = vm::ulangiFoto,
            onAnalyze = vm::analyze
        )
        DetectionUiState.Loading -> LoadingContent()
        is DetectionUiState.Rejected -> RejectedContent(s.confidence, onTryAgain = vm::ulangiFoto, onBack = vm::ulangiFoto)
        is DetectionUiState.Error -> ErrorContent(s.pesan, onTryAgain = vm::ulangiFoto, onBack = vm::ulangiFoto)
        is DetectionUiState.Success -> DetectionResultScreen(
            hasil = s.hasil, foto = foto, rekomendasi = rekomendasi,
            onTryAgain = vm::ulangiFoto,
            onFinish = { vm.ulangiFoto(); onSelesai() },
            onSeeAll = { onLihatEdukasi(s.hasil.kelas) },
            onItemClick = onEduItemClick,
            onTanyaChatbot = onTanyaChatbot,
            onCariKlinik = onCariKlinik
        )
    }
}
