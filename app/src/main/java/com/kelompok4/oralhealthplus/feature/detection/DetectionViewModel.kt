package com.kelompok4.oralhealthplus.feature.detection

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetectionUiState {
    data object Idle : DetectionUiState
    data object Loading : DetectionUiState
    data class Rejected(val confidence: Float) : DetectionUiState
    data class Success(val hasil: DetectionResult, val waktuInferensiMs: Long) : DetectionUiState
    data class Error(val pesan: String) : DetectionUiState
}

/**
 * `simpanRiwayat` disambungkan ke HistoryRepository milik Anggota 2, mis.
 *   DetectionViewModel(classifier) { historyRepository.simpanHasil(it) }
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
                val hasil = TriageEngine.evaluate(prediksi.kelas, prediksi.confidence)
                if (hasil.ditolak) {
                    _state.value = DetectionUiState.Rejected(hasil.confidence)
                } else {
                    simpanRiwayat(hasil)
                    _state.value = DetectionUiState.Success(hasil, prediksi.waktuInferensiMs)
                }
            } catch (e: Exception) {
                _state.value = DetectionUiState.Error(e.message ?: "Gagal menganalisis foto")
            }
        }
    }

    /** "Try Again" / "Change Photo" / Finish: buang foto dan kembali ke layar Oral Check kosong. */
    fun ulangiFoto() {
        if (_state.value is DetectionUiState.Loading) return
        _foto.value = null
        _state.value = DetectionUiState.Idle
    }

    override fun onCleared() { classifier.close() }

    class Factory(
        private val classifier: Classifier,
        private val simpanRiwayat: suspend (DetectionResult) -> Unit
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetectionViewModel(classifier, simpanRiwayat) as T
    }
}
