package com.issueissyu.fe.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    data object Login : SplashDestination
    data object Main : SplashDestination
    data object Onboarding : SplashDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            delay(800)
            _destination.value = when {
                !tokenManager.hasTokens() -> SplashDestination.Login
                tokenManager.getIsNewUser() -> SplashDestination.Onboarding
                else -> SplashDestination.Main
            }
        }
    }
}
