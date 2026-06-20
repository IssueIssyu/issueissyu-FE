package com.issueissyu.fe.ui.navigation

import androidx.lifecycle.ViewModel
import com.issueissyu.fe.core.auth.AuthSessionState
import com.issueissyu.fe.core.auth.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class AppSessionViewModel @Inject constructor(
    sessionManager: SessionManager,
) : ViewModel() {
    val authState: StateFlow<AuthSessionState> = sessionManager.authState
    val sessionExpiredMessages: SharedFlow<String> = sessionManager.sessionExpiredMessages
}
