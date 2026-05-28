package com.issueissyu.fe.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.local.OnboardingSessionStore
import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.domain.repository.AuthRepository
import com.issueissyu.fe.domain.repository.LocationRepository
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
class LocalVerificationViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val authRepository: AuthRepository,
    private val onboardingSessionStore: OnboardingSessionStore,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val event = _event.asSharedFlow()

    data class UiState(
        val addressText: String = "",
        val latitude: Double? = null,
        val longitude: Double? = null,
        val isLoading: Boolean = false,
        val isLocationReady: Boolean = false,
    )

    sealed interface UiEvent {
        data object NavigateNext : UiEvent
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
                                "현재 위치 주소를 확인할 수 없어도 인증은 가능합니다."
                            },
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            addressText = "현재 위치 주소를 확인할 수 없어도 인증은 가능합니다.",
                        )
                    }
                },
            )
        }
    }

    fun registerLocation() {
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

            locationRepository.certifyUserLocation(latitude, longitude).fold(
                onSuccess = {
                    val pending = onboardingSessionStore.getPendingProfile()
                    if (pending == null) {
                        _uiState.update { it.copy(isLoading = false) }
                        _event.emit(
                            UiEvent.ShowError("프로필 정보가 없습니다. 본인인증 단계부터 다시 진행해주세요."),
                        )
                        return@fold
                    }

                    authRepository.onboarding(
                        nickname = pending.first,
                        email = pending.second,
                        phone = pending.third,
                    ).fold(
                        onSuccess = {
                            onboardingSessionStore.clearPendingProfile()
                            tokenManager.clearNewUserFlag()
                            _uiState.update { it.copy(isLoading = false) }
                            _event.emit(UiEvent.NavigateNext)
                        },
                        onFailure = { e ->
                            _uiState.update { it.copy(isLoading = false) }
                            _event.emit(
                                UiEvent.ShowError(e.message ?: "온보딩 완료 처리에 실패했습니다"),
                            )
                        },
                    )
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    _event.emit(UiEvent.ShowError(e.message ?: "동네 등록에 실패했습니다"))
                },
            )
        }
    }
}
