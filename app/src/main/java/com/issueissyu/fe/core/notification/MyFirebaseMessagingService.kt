package com.issueissyu.fe.core.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenSyncManager: FcmTokenSyncManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        fcmTokenSyncManager.syncToken(token)
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