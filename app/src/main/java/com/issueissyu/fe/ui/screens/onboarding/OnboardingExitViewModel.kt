package com.issueissyu.fe.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.usecase.onboarding.ExitOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingExitViewModel @Inject constructor(
    private val exitOnboardingUseCase: ExitOnboardingUseCase,
) : ViewModel() {

    data class UiState(
        val showConfirmDialog: Boolean = false,
        val isExiting: Boolean = false,
    )

    sealed interface Effect {
        data object NavigateToLogin : Effect
        data class ShowError(val message: String) : Effect
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    fun requestSwitchAccount() {
        if (_uiState.value.isExiting) return
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    fun dismissConfirmDialog() {
        if (_uiState.value.isExiting) return
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    fun confirmSwitchAccount() {
        if (_uiState.value.isExiting) return
        viewModelScope.launch {
            _uiState.update { it.copy(showConfirmDialog = false, isExiting = true) }
            exitOnboardingUseCase()
                .onSuccess { _effects.emit(Effect.NavigateToLogin) }
                .onFailure { error ->
                    _uiState.update { it.copy(isExiting = false) }
                    _effects.emit(
                        Effect.ShowError(
                            error.message ?: LOGOUT_ERROR_MESSAGE,
                        ),
                    )
                }
        }
    }

    fun onNavigateToLoginDispatched() {
        viewModelScope.launch {
            delay(NAVIGATION_RESET_DELAY_MS)
            if (_uiState.value.isExiting) {
                _uiState.update { it.copy(isExiting = false) }
            }
        }
    }

    companion object {
        private const val NAVIGATION_RESET_DELAY_MS = 500L
        private const val LOGOUT_ERROR_MESSAGE = "로그아웃에 실패했습니다."
    }
}
