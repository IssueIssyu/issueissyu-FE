package com.issueissyu.fe.core.notification

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.domain.repository.AlarmRepository
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenSyncManager @Inject constructor(
    @ApplicationContext context: Context,
    private val tokenManager: TokenManager,
    // dagger.Lazy 주입: AlarmRepository → OkHttp(TokenAuthenticator) → SessionManager →
    // FcmTokenSyncManager 순환을 그래프 생성 시점이 아닌 사용 시점으로 미뤄 끊는다.
    private val alarmRepository: Lazy<AlarmRepository>,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun syncCurrentTokenIfNeeded() {
        if (!tokenManager.hasTokens()) return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            scope.launch { syncToken(token) }
        }
    }

    fun syncToken(fcmToken: String) {
        scope.launch { syncTokenInternal(fcmToken) }
    }

    fun clearLastSynced() {
        prefs.edit().remove(KEY_LAST_SYNCED).apply()
    }

    private suspend fun syncTokenInternal(fcmToken: String) {
        if (!tokenManager.hasTokens() || fcmToken.isBlank()) return
        if (prefs.getString(KEY_LAST_SYNCED, null) == fcmToken) return

        alarmRepository.get().storePushToken(fcmToken).onSuccess {
            prefs.edit().putString(KEY_LAST_SYNCED, fcmToken).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "fcm_token_sync"
        private const val KEY_LAST_SYNCED = "last_synced_fcm_token"
    }
}
