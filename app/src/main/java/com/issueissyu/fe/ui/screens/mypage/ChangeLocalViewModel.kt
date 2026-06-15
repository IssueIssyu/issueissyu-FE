package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.repository.LocationRepository
import com.issueissyu.fe.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeLocalViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val event = _event.asSharedFlow()

    data class UiState(
        val addressText: String = "현재 위치를 불러오는 중...",
        val latitude: Double? = null,
        val longitude: Double? = null,
        val isLoading: Boolean = false,
        val isLocationReady: Boolean = false,
    )

    sealed interface UiEvent {
        data object Completed : UiEvent
        data class ShowError(val message: String) : UiEvent
    }

    fun onLocationUnavailable() {
        _uiState.update {
            it.copy(
                latitude = null,
                longitude = null,
                isLocationReady = false,
                addressText = "",
            )
        }
    }

    fun onCurrentLocationReady(lat: Double, lng: Double) {
        _uiState.update {
            it.copy(
                latitude = lat,
                longitude = lng,
                isLocationReady = true,
                addressText = "현재 위치 주소를 확인하는 중...",
            )
        }
        fetchCurrentAddressPreview(lat, lng)
    }

    private fun fetchCurrentAddressPreview(lat: Double, lng: Double) {
        viewModelScope.launch {
            locationRepository.myAddress(lat, lng).fold(
                onSuccess = { address ->
                    _uiState.update {
                        it.copy(
                            addressText = address.ifBlank {
                                "현재 위치 주소를 확인할 수 없어도 변경은 가능합니다."
                            },
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            addressText = "현재 위치 주소를 확인할 수 없어도 변경은 가능합니다.",
                        )
                    }
                },
            )
        }
    }

    fun updateLocation() {
        if (_uiState.value.isLoading) return

        val latitude = _uiState.value.latitude
        val longitude = _uiState.value.longitude

        if (latitude == null || longitude == null) {
            viewModelScope.launch {
                _event.emit(UiEvent.ShowError("위치 정보가 없습니다"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            userRepository.updateUserAddress(latitude, longitude).fold(
                onSuccess = { address ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            addressText = address.ifBlank { it.addressText },
                        )
                    }
                    _event.emit(UiEvent.Completed)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    _event.emit(UiEvent.ShowError(e.message ?: "동네 변경에 실패했습니다"))
                },
            )
        }
    }
}
