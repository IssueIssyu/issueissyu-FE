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
        val pinId = message.data["pinId"]
        val communityId = message.data["communityId"]
        val alarmId = message.readAlarmId()
        val title = message.data["title"] ?: message.notification?.title ?: "이슈이슈 알림"
        val body = message.data["body"] ?: message.notification?.body ?: ""

        NotificationHelper.show(
            context = this,
            type = type,
            pinId = pinId,
            communityId = communityId,
            title = title,
            body = body,
            alarmId = alarmId,
        )
    }
}

private fun RemoteMessage.readAlarmId(): String? {
    val knownAlarmId = listOf(
        "alarmId",
        "likeAlarmId",
        "eventAlarmId",
        "storeAlarmId",
        "hotAlarmId",
    ).firstNotNullOfOrNull { key ->
        data[key]?.takeIf { it.isNotBlank() }
    }
    if (knownAlarmId != null) return knownAlarmId
    return data.entries
        .firstOrNull { (key, value) ->
            key.endsWith("AlarmId", ignoreCase = true) && value.isNotBlank()
        }
        ?.value
}