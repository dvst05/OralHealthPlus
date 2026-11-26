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
import com.kelompok4.oralhealthplus.feature.auth.GuestAccessPolicy
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
    onHubungiDokter: () -> Unit = {},
    isGuest: Boolean = false,
    remainingGuestAnalyses: Int = 1,
    canScanPhoto: Boolean = true,
    canAnalyzePhoto: Boolean = true,
    onRecordGuestScan: () -> Unit = {},
    onRecordGuestAnalysis: () -> Unit = {},
    onRequireLogin: (reason: String) -> Unit = {},
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
                .onSuccess {
                    pesanGalat = null
                    vm.pilihFoto(it)
                    onRecordGuestScan()
                }
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
                onRecordGuestScan()
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
        if (!canScanPhoto) {
            onRequireLogin(GuestAccessPolicy.MSG_ANALYSIS_LIMIT_REACHED)
            return
        }
        if (perluIzinKamera(context)) izinKamera.launch(Manifest.permission.CAMERA) else bukaKamera()
    }

    fun bukaGaleri() {
        if (!canScanPhoto) {
            onRequireLogin(GuestAccessPolicy.MSG_ANALYSIS_LIMIT_REACHED)
            return
        }
        galeri.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // Back dari Result/Rejected/Error = ulang dari awal (atau keluar jika kuota tamu habis)
    BackHandler(
        enabled = state is DetectionUiState.Success || state is DetectionUiState.Rejected ||
            state is DetectionUiState.Error
    ) {
        vm.ulangiFoto()
        if (isGuest && !canAnalyzePhoto) onSelesai()
    }

    when (val s = state) {
        DetectionUiState.Idle -> OralCheckScreen(
            foto = foto,
            pesanGalat = pesanGalat,
            onBack = onBack,
            onTakePhoto = { ambilFoto() },
            onGallery = { bukaGaleri() },
            onChangePhoto = {
                if (isGuest && !canScanPhoto) {
                    onRequireLogin(GuestAccessPolicy.MSG_SCAN_LIMIT_REACHED)
                } else {
                    vm.ulangiFoto()
                }
            },
            onAnalyze = {
                if (!canAnalyzePhoto) {
                    onRequireLogin(GuestAccessPolicy.MSG_ANALYSIS_LIMIT_REACHED)
                } else {
                    onRecordGuestAnalysis()
                    vm.analyze()
                }
            },
            isGuest = isGuest,
            remainingGuestAnalyses = remainingGuestAnalyses
        )
        DetectionUiState.Loading -> LoadingContent()
        is DetectionUiState.Rejected -> RejectedContent(
            confidence = s.confidence,
            onTryAgain = {
                if (isGuest && !canAnalyzePhoto) {
                    vm.ulangiFoto()
                    onRequireLogin(GuestAccessPolicy.MSG_ANALYSIS_LIMIT_REACHED)
                } else {
                    vm.ulangiFoto()
                }
            },
            onBack = {
                vm.ulangiFoto()
                if (isGuest && !canAnalyzePhoto) onSelesai()
            }
        )
        is DetectionUiState.Error -> ErrorContent(
            pesan = s.pesan,
            onTryAgain = {
                if (isGuest && !canAnalyzePhoto) {
                    vm.ulangiFoto()
                    onRequireLogin(GuestAccessPolicy.MSG_ANALYSIS_LIMIT_REACHED)
                } else {
                    vm.ulangiFoto()
                }
            },
            onBack = {
                vm.ulangiFoto()
                if (isGuest && !canAnalyzePhoto) onSelesai()
            }
        )
        is DetectionUiState.Success -> DetectionResultScreen(
            hasil = s.hasil, foto = foto, rekomendasi = rekomendasi,
            onTryAgain = {
                if (isGuest && !canAnalyzePhoto) {
                    vm.ulangiFoto()
                    onRequireLogin(GuestAccessPolicy.MSG_ANALYSIS_LIMIT_REACHED)
                } else {
                    vm.ulangiFoto()
                }
            },
            onFinish = { vm.ulangiFoto(); onSelesai() },
            onSeeAll = { onLihatEdukasi(s.hasil.kelas) },
            onItemClick = onEduItemClick,
            onTanyaChatbot = {
                vm.ulangiFoto()
                onTanyaChatbot()
            },
            onCariKlinik = {
                vm.ulangiFoto()
                onCariKlinik()
            },
            onHubungiDokter = {
                vm.ulangiFoto()
                onHubungiDokter()
            },
            riwayatTersimpan = s.riwayatTersimpan
        )
    }
}
