package com.issueissyu.fe.core.notification

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.domain.repository.AlarmRepository
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenSyncManager @Inject constructor(
    @ApplicationContext context: Context,
    private val tokenManager: TokenManager,
    private val alarmRepository: Lazy<AlarmRepository>,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    private var syncGeneration = 0

    fun syncCurrentTokenIfNeeded() {
        if (!tokenManager.hasTokens()) return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            syncToken(token)
        }
    }

    fun syncToken(fcmToken: String) {
        scope.launch { syncTokenInternal(fcmToken) }
    }

    fun cancelPendingSync() {
        syncGeneration++
        prefs.edit().remove(KEY_LAST_SYNCED).apply()
    }

    private suspend fun syncTokenInternal(fcmToken: String) {
        val generation = syncGeneration
        if (!tokenManager.hasTokens() || fcmToken.isBlank()) return
        if (prefs.getString(KEY_LAST_SYNCED, null) == fcmToken) return

        repeat(MAX_ATTEMPTS) { attempt ->
            if (generation != syncGeneration) return
            if (!tokenManager.hasTokens() || fcmToken.isBlank()) return

            val result = alarmRepository.get().storePushToken(fcmToken)
            if (result.isSuccess) {
                prefs.edit().putString(KEY_LAST_SYNCED, fcmToken).apply()
                return
            }
            Log.w(
                TAG,
                "FCM token sync failed attempt=${attempt + 1}/$MAX_ATTEMPTS",
                result.exceptionOrNull(),
            )
            if (attempt < MAX_ATTEMPTS - 1) {
                delay(RETRY_DELAY_MS)
            }
        }
    }

    companion object {
        private const val TAG = "FcmTokenSyncManager"
        private const val PREFS_NAME = "fcm_token_sync"
        private const val KEY_LAST_SYNCED = "last_synced_fcm_token"
        private const val MAX_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 2_000L
    }
}
