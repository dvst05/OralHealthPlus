package com.kelompok4.oralhealthplus.feature.detection

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetectionUiState {
    data object Idle : DetectionUiState
    data object Loading : DetectionUiState
    data class Rejected(val confidence: Float) : DetectionUiState
    data class Success(
        val hasil: DetectionResult,
        val waktuInferensiMs: Long,
        val riwayatTersimpan: Boolean = true
    ) : DetectionUiState
    data class Error(val pesan: String) : DetectionUiState
}

/**
 * `simpanRiwayat` disambungkan ke HistoryRepository milik Anggota 2:
 *   DetectionViewModel.Factory(context) { historyRepository.simpanHasil(it.keModelBersama()) }
 * Hanya hasil yang LOLOS (tidak ditolak) yang disimpan, sesuai flowchart fungsi 1.
 */
class DetectionViewModel(
    private val classifier: Classifier,
    private val simpanRiwayat: suspend (DetectionResult) -> Unit
) : ViewModel() {

    private val _state = MutableStateFlow<DetectionUiState>(DetectionUiState.Idle)
    val state: StateFlow<DetectionUiState> = _state.asStateFlow()   // bertahan saat rotate

    private val _foto = MutableStateFlow<Bitmap?>(null)
    val foto: StateFlow<Bitmap?> = _foto.asStateFlow()              // foto terpilih (kamera/galeri)

    fun pilihFoto(bitmap: Bitmap) {
        if (_state.value is DetectionUiState.Loading) return
        _foto.value = bitmap
        _state.value = DetectionUiState.Idle
    }

    /** Tombol "Analyze": menganalisis foto yang sudah dipilih. */
    fun analyze() { _foto.value?.let(::analyze) }

    fun analyze(bitmap: Bitmap) {
        if (_state.value is DetectionUiState.Loading) return        // cegah analisis ganda
        viewModelScope.launch {
            _state.value = DetectionUiState.Loading
            try {
                val prediksi = classifier.classify(bitmap)           // Dispatchers.Default
                // Waktu inferensi dicatat di Logcat untuk tabel uji Bab IV
                Log.d(TAG, "${prediksi.kelas} ${prediksi.confidence} dalam ${prediksi.waktuInferensiMs} ms")
                val hasil = TriageEngine.evaluate(prediksi.kelas, prediksi.confidence)
                if (hasil.ditolak) {
                    _state.value = DetectionUiState.Rejected(hasil.confidence)
                } else {
                    val tersimpan = cobaSimpan(hasil)
                    _state.value = DetectionUiState.Success(hasil, prediksi.waktuInferensiMs, tersimpan)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                gagal(e)
            } catch (e: LinkageError) {                             // library native gagal dimuat
                gagal(e)
            }
        }
    }

    /** Gagal menyimpan riwayat tidak boleh menghilangkan hasil analisis. */
    private suspend fun cobaSimpan(hasil: DetectionResult): Boolean = try {
        simpanRiwayat(hasil)
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Gagal menyimpan riwayat", e)
        false
    }

    private fun gagal(e: Throwable) {
        Log.e(TAG, "Analisis foto gagal", e)                        // detail teknis cukup di Logcat
        _state.value = DetectionUiState.Error("We couldn't analyze this photo. Please try again.")
    }

    /** "Try Again" / "Change Photo" / Finish: buang foto dan kembali ke layar Oral Check kosong. */
    fun ulangiFoto() {
        if (_state.value is DetectionUiState.Loading) return
        _foto.value = null
        _state.value = DetectionUiState.Idle
    }

    override fun onCleared() { classifier.close() }

    /**
     * Classifier dibuat di dalam create(), jadi hanya ada satu per ViewModel
     * (tidak ada interpreter baru yang bocor setiap layar diputar).
     */
    class Factory(
        context: Context,
        private val simpanRiwayat: suspend (DetectionResult) -> Unit
    ) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetectionViewModel(Classifier(appContext), simpanRiwayat) as T
    }

    private companion object {
        const val TAG = "Deteksi"
    }
}
