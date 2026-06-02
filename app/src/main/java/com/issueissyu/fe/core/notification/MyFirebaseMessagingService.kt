package com.issueissyu.fe.core.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.issueissyu.fe.domain.repository.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "NEW 토큰: $token")
        CoroutineScope(Dispatchers.IO).launch {
            alarmRepository.storePushToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val type = message.data["type"]
        val targetId = message.data["targetId"]
        val title = message.data["title"] ?: message.notification?.title ?: "이슈이슈 알림"
        val body = message.data["body"] ?: message.notification?.body ?: ""

        NotificationHelper.show(this, type, targetId, title, body)
    }
}