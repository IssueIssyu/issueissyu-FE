package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.TermsAgreementResult
import com.issueissyu.fe.domain.model.notification.NotificationType
import com.issueissyu.fe.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmSettingsUiState(
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val pinLike: Boolean = false,
    val event: Boolean = false,
    val popularPost: Boolean = false,
    val storePromo: Boolean = false,
)

@HiltViewModel
class AlarmSettingsViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmSettingsUiState())
    val uiState: StateFlow<AlarmSettingsUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val event = _event.asSharedFlow()

    sealed interface UiEvent {
        data class ShowError(val message: String) : UiEvent
    }

    init {
        loadAlarmSettings()
    }

    fun loadAlarmSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            alarmRepository.getAlarmToggleState().fold(
                onSuccess = { state ->
                    _uiState.update { it.applyAlarmState(state).copy(isLoading = false, errorMessage = null) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message?.takeIf { message -> message.isNotBlank() }
                                ?: "알림 설정을 불러오지 못했습니다.",
                        )
                    }
                },
            )
        }
    }

    fun updatePinLike(enabled: Boolean) = updateAlarmToggle(NotificationType.LIKE, enabled)

    fun updateEvent(enabled: Boolean) = updateAlarmToggle(NotificationType.EVENT, enabled)

    fun updatePopularPost(enabled: Boolean) = updateAlarmToggle(NotificationType.HOT, enabled)

    fun updateStorePromo(enabled: Boolean) = updateAlarmToggle(NotificationType.STORE, enabled)

    private fun updateAlarmToggle(
        alarmType: NotificationType,
        enabled: Boolean,
    ) {
        if (_uiState.value.isUpdating) return

        val previousState = _uiState.value
        _uiState.update {
            it.copy(isUpdating = true).updateToggle(alarmType, enabled)
        }

        viewModelScope.launch {
            alarmRepository.updateAlarmToggle(alarmType).fold(
                onSuccess = { updated ->
                    _uiState.update {
                        it.copy(isUpdating = false).updateToggle(alarmType, updated)
                    }
                },
                onFailure = { error ->
                    _uiState.update { previousState.copy(isUpdating = false) }
                    _event.emit(
                        UiEvent.ShowError(
                            error.message?.takeIf { it.isNotBlank() }
                                ?: "알림 설정 변경에 실패했습니다.",
                        )
                    )
                },
            )
        }
    }

    private fun AlarmSettingsUiState.applyAlarmState(state: TermsAgreementResult): AlarmSettingsUiState {
        return copy(
            pinLike = state.likeAlarmActive,
            event = state.eventAlarmActive,
            popularPost = state.hotAlarmActive,
            storePromo = state.storeAlarmActive
        )
    }

    private fun AlarmSettingsUiState.updateToggle(
        alarmType: NotificationType,
        enabled: Boolean,
    ): AlarmSettingsUiState {
        return when (alarmType) {
            NotificationType.LIKE -> copy(pinLike = enabled)
            NotificationType.EVENT -> copy(event = enabled)
            NotificationType.HOT -> copy(popularPost = enabled)
            NotificationType.STORE -> copy(storePromo = enabled)
        }
    }
}
