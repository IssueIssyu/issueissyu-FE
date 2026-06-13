package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmSettingsUiState(
    val isLoading: Boolean = true,
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

    init {
        loadAlarmSettings()
    }

    fun loadAlarmSettings() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            alarmRepository.getAlarmToggleState().fold(
                onSuccess = { state ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            pinLike = state.likeAlarmActive,
                            event = state.eventAlarmActive,
                            popularPost = state.hotAlarmActive,
                            storePromo = state.storeAlarmActive,
                        )
                    }
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

    fun updatePinLike(enabled: Boolean) {
        _uiState.update { it.copy(pinLike = enabled) }
    }

    fun updateEvent(enabled: Boolean) {
        _uiState.update { it.copy(event = enabled) }
    }

    fun updatePopularPost(enabled: Boolean) {
        _uiState.update { it.copy(popularPost = enabled) }
    }

    fun updateStorePromo(enabled: Boolean) {
        _uiState.update { it.copy(storePromo = enabled) }
    }
}
