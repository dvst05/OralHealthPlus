package com.kelompok4.oralhealthplus.feature.education

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kelompok4.oralhealthplus.BuildConfig
import com.kelompok4.oralhealthplus.feature.encyclopedia.EncyclopediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/** Empat kondisi layar Rekomendasi Konten, sama dengan layar 6/7/8a/8b/8c di Figma. */
sealed interface EducationUiState {
    data object Loading : EducationUiState
    data class Sukses(
        val namaPenyakit: String?,          // null jika konten umum
        val artikel: List<ScoredContent>,
        val video: List<ScoredContent>,
        val personal: Boolean,
    ) : EducationUiState
    data class Gagal(val pesan: String) : EducationUiState
}

/**
 * @param ambilLabelTerakhir mengembalikan label hasil deteksi terakhir, atau null jika belum ada riwayat.
 *        Setelah branch Anggota 2 digabung: { historyRepository.getLatestDetection()?.kelas }
 */
class EducationViewModel(
    private val repository: EducationRepository,
    private val namaPenyakitUntuk: (String?) -> String?,
    private val ambilLabelTerakhir: suspend () -> String?,
) : ViewModel() {

    private val _state = MutableStateFlow<EducationUiState>(EducationUiState.Loading)
    val state: StateFlow<EducationUiState> = _state.asStateFlow()

    init {
        muat()
    }

    /** Dipanggil saat layar dibuka dan saat tombol "Coba Lagi" ditekan. */
    fun muat() {
        _state.value = EducationUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                val label = ambilLabelTerakhir()
                val hasil = repository.muatRekomendasi(label)
                EducationUiState.Sukses(
                    namaPenyakit = if (hasil.personal) namaPenyakitUntuk(label) else null,
                    artikel = hasil.artikel,
                    video = hasil.video,
                    personal = hasil.personal,
                )
            } catch (e: CancellationException) {
                throw e // layar ditutup saat memuat: bukan error, biarkan coroutine berhenti
            } catch (e: Exception) {
                // IOException (internet/NewsAPI) maupun data rusak tidak boleh membuat aplikasi crash
                EducationUiState.Gagal("Artikel dan video belum bisa ditampilkan. Periksa koneksi internetmu, lalu coba lagi.")
            }
        }
    }

    companion object {
        /**
         * Membuat ViewModel beserta dependensinya.
         * Jika NEWS_API_KEY di local.properties kosong, artikel diambil dari data contoh offline.
         */
        fun factory(context: Context, ambilLabelTerakhir: suspend () -> String?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appContext = context.applicationContext
                    val ensiklopedia = EncyclopediaRepository.dariAssets(appContext)
                    val kunci = BuildConfig.NEWS_API_KEY.trim()
                    val sumber: ArticleSource =
                        if (kunci.isEmpty()) OfflineArticleSource(appContext) else NewsApiArticleSource(kunci)
                    val repository = EducationRepository(sumber, ensiklopedia::kataKunciUntuk)
                    return EducationViewModel(
                        repository = repository,
                        namaPenyakitUntuk = { ensiklopedia.cari(it)?.nama },
                        ambilLabelTerakhir = ambilLabelTerakhir,
                    ) as T
                }
            }
    }
}
