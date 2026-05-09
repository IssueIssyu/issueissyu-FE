package com.issueissyu.fe.core.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "NEW 토큰: $token")
        // TODO: 백엔드에 토큰 업데이트
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