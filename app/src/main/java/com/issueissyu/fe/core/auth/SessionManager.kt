package com.issueissyu.fe.core.auth

import com.issueissyu.fe.data.local.TokenManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthSessionState {
    data object Authenticated : AuthSessionState
    data object Unauthenticated : AuthSessionState
}

@Singleton
class SessionManager @Inject constructor(
    private val tokenManager: TokenManager,
) {
    private val _authState = MutableStateFlow<AuthSessionState>(
        if (tokenManager.hasTokens()) {
            AuthSessionState.Authenticated
        } else {
            AuthSessionState.Unauthenticated
        }
    )
    val authState = _authState.asStateFlow()

    private val _sessionExpiredMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val sessionExpiredMessages = _sessionExpiredMessages.asSharedFlow()

    fun markAuthenticated() {
        _authState.value = AuthSessionState.Authenticated
    }

    fun clearSession() {
        tokenManager.clearTokens()
        _authState.value = AuthSessionState.Unauthenticated
    }

    fun expireSession(message: String = DEFAULT_SESSION_EXPIRED_MESSAGE) {
        val shouldNotify = _authState.value != AuthSessionState.Unauthenticated || tokenManager.hasTokens()
        tokenManager.clearTokens()
        _authState.value = AuthSessionState.Unauthenticated
        if (shouldNotify) {
            _sessionExpiredMessages.tryEmit(message)
        }
    }

    companion object {
        const val DEFAULT_SESSION_EXPIRED_MESSAGE = "세션이 만료되었습니다. 다시 로그인해 주세요."
    }
}
