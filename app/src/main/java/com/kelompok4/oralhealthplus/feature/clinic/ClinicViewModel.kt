package com.kelompok4.oralhealthplus.feature.clinic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClinicUiState(
    val isLoading: Boolean = false,
    val clinics: List<DentalClinic> = emptyList(),
    val selectedRadiusKm: Int = 5,
    val userLat: Double? = null,
    val userLon: Double? = null,
    val errorMessage: String? = null,
    val hasLocationPermission: Boolean = false
)

class ClinicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ClinicRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ClinicUiState())
    val uiState: StateFlow<ClinicUiState> = _uiState.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
        if (granted) {
            loadNearbyClinics()
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Izin lokasi diperlukan untuk menemukan klinik gigi di sekitar Anda."
                )
            }
        }
    }

    fun updateRadius(radiusKm: Int) {
        _uiState.update { it.copy(selectedRadiusKm = radiusKm) }
        if (_uiState.value.hasLocationPermission) {
            loadNearbyClinics()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun loadNearbyClinics(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (forceRefresh) repository.clearCache()
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val location = repository.getCurrentLocation()
            if (location == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Tidak dapat membaca GPS. Pastikan GPS/Lokasi perangkat aktif."
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(userLat = location.latitude, userLon = location.longitude)
            }

            val radiusMeters = _uiState.value.selectedRadiusKm * 1000
            val result = repository.fetchNearbyClinics(
                userLat = location.latitude,
                userLon = location.longitude,
                radiusMeters = radiusMeters
            )

            result.fold(
                onSuccess = { list ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            clinics = list,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message ?: "Terjadi kesalahan saat mencari klinik."
                        )
                    }
                }
            )
        }
    }
}