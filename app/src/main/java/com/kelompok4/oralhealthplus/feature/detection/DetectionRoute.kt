package com.kelompok4.oralhealthplus.feature.detection

import android.Manifest
import android.content.ActivityNotFoundException
import android.net.Uri
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

/**
 * SATU rute NavHost untuk seluruh fungsi 1 (Oral Check -> Analyze -> Result / Rejected).
 * Anggota 3 cukup memasang: composable("deteksi") { DetectionRoute(vm, ...) }.
 *
 * Galeri memakai Photo Picker sistem (tanpa izin storage). Kamera memakai TakePicture +
 * FileProvider supaya fotonya beresolusi penuh (TakePicturePreview hanya memberi thumbnail).
 * Izin CAMERA tidak dibutuhkan; tetapi jika modul lain mendeklarasikannya di manifest,
 * Android mewajibkan izin runtime sebelum membuka kamera, dan rute ini sudah memintanya.
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
                .onFailure {
                    if (it is CancellationException) throw it
                    pesanGalat = "Couldn't open that photo. Try another one."
                }
        }
    }
    // rememberSaveable: aplikasi bisa dimatikan sistem saat kamera terbuka
    var pathFotoKamera by rememberSaveable { mutableStateOf<String?>(null) }

    val kamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { berhasil ->
        val file = pathFotoKamera?.let(::File)
        pathFotoKamera = null
        if (file == null) return@rememberLauncherForActivityResult
        if (!berhasil) {
            file.delete()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            try {
                vm.pilihFoto(muatBitmap(context, Uri.fromFile(file)))
                pesanGalat = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                pesanGalat = "Couldn't read the camera photo. Please try again."
            } finally {
                file.delete()   // foto mulut tidak disimpan di perangkat setelah dibaca
            }
        }
    }

    fun bukaKamera() {
        try {
            val file = buatFileFotoKamera(context)
            pathFotoKamera = file.absolutePath
            kamera.launch(uriFotoKamera(context, file))
        } catch (e: ActivityNotFoundException) {
            pesanGalat = "No camera app found. Please use Gallery instead."
        } catch (e: Exception) {
            pesanGalat = "Couldn't open the camera. Please use Gallery instead."
        }
    }

    val izinKamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { diberi ->
        if (diberi) bukaKamera()
        else pesanGalat = "Camera permission is needed to take a photo. You can use Gallery instead."
    }

    fun ambilFoto() {
        if (perluIzinKamera(context)) izinKamera.launch(Manifest.permission.CAMERA) else bukaKamera()
    }

    // Back dari Result/Rejected/Error = ulang dari awal (bukan keluar fitur)
    BackHandler(
        enabled = state is DetectionUiState.Success || state is DetectionUiState.Rejected ||
            state is DetectionUiState.Error
    ) { vm.ulangiFoto() }

    when (val s = state) {
        DetectionUiState.Idle -> OralCheckScreen(
            foto = foto, pesanGalat = pesanGalat, onBack = onBack,
            onTakePhoto = { ambilFoto() },
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
            onCariKlinik = onCariKlinik,
            riwayatTersimpan = s.riwayatTersimpan
        )
    }
}
