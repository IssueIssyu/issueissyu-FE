package com.issueissyu.fe.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.local.OnboardingSessionStore
import com.issueissyu.fe.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
    private val authRepository: AuthRepository,
    private val onboardingSessionStore: OnboardingSessionStore,
) : ViewModel() {

    data class UiState(
        val showConfirmDialog: Boolean = false,
        val isExiting: Boolean = false,
    )

    sealed interface Effect {
        data object NavigateToLogin : Effect
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
            authRepository.logout()
            onboardingSessionStore.clearPendingProfile()
            _effects.emit(Effect.NavigateToLogin)
        }
    }
}
