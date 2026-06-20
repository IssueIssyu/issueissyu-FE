package com.issueissyu.fe.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.core.auth.SessionManager
import com.issueissyu.fe.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashDestination {
    data object Loading : SplashDestination
    data class Login(val showStorageWarning: Boolean = false) : SplashDestination
    data object Main : SplashDestination
    data object Onboarding : SplashDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            delay(800)
            _destination.value = when {
                !tokenManager.hasTokens() -> SplashDestination.Login(
                    showStorageWarning = !tokenManager.isStorageAvailable,
                )
                tokenManager.getIsNewUser() -> {
                    sessionManager.markAuthenticated()
                    SplashDestination.Onboarding
                }
                else -> {
                    sessionManager.markAuthenticated()
                    SplashDestination.Main
                }
            }
        }
    }
}
