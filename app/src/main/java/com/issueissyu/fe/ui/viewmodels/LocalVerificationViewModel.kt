package com.issueissyu.fe.ui.viewmodels

import android.location.Location
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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private val locationFlow = MutableSharedFlow<Pair<Double, Double>>(extraBufferCapacity = 1)

    private var lastLat: Double? = null
    private var lastLng: Double? = null
    private var lastPreviewErrorMessage: String? = null

    init {
        observeLocation()
    }

    data class UiState(
        val currentAddress: String = "",
        val latitude: Double? = null,
        val longitude: Double? = null,
        val isLoading: Boolean = false,
        val isAddressResolved: Boolean = false,
    )

    sealed interface UiEvent {
        data object NavigateNext : UiEvent
        data class ShowError(val message: String) : UiEvent
    }

    private fun observeLocation() {
        locationFlow
            .debounce(400)
            .onEach { (lat, lng) -> fetchAddressPreview(lat, lng) }
            .launchIn(viewModelScope)
    }

    fun onMapMoved(lat: Double, lng: Double) {
        val previousLat = lastLat
        val previousLng = lastLng
        if (previousLat != null && previousLng != null) {
            val distance = distanceBetween(previousLat, previousLng, lat, lng)
            if (distance < 30) return
        }

        lastLat = lat
        lastLng = lng

        _uiState.update {
            it.copy(
                currentAddress = "주소를 확인하는 중이에요...",
                latitude = lat,
                longitude = lng,
                isAddressResolved = false,
            )
        }

        locationFlow.tryEmit(lat to lng)
    }

    private suspend fun fetchAddressPreview(lat: Double, lng: Double) {
        locationRepository.resolveAddressPreview(lat, lng).fold(
            onSuccess = { address ->
                lastPreviewErrorMessage = null
                if (address.isNotBlank()) {
                    _uiState.update {
                        it.copy(
                            currentAddress = address,
                            isAddressResolved = true,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            currentAddress = "지도를 움직여 동네 주소를 확인해 주세요",
                            isAddressResolved = hasValidCoordinates(it),
                        )
                    }
                }
            },
            onFailure = {
                val message = it.message ?: "주소 미리보기에 실패했습니다"
                if (lastPreviewErrorMessage != message) {
                    lastPreviewErrorMessage = message
                    _event.emit(UiEvent.ShowError(message))
                }
                _uiState.update {
                    it.copy(
                        currentAddress = "위치는 선택되었습니다. 확인을 눌러 동네를 등록해 주세요.",
                        isAddressResolved = hasValidCoordinates(it),
                    )
                }
            },
        )
    }

    private fun hasValidCoordinates(state: UiState): Boolean =
        state.latitude != null && state.longitude != null

    fun registerLocation() {
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
                onSuccess = { address ->
                    if (address.isNotBlank()) {
                        _uiState.update { it.copy(currentAddress = address) }
                    }

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

    private fun distanceBetween(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double,
    ): Double {
        val result = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, result)
        return result[0].toDouble()
    }
}
