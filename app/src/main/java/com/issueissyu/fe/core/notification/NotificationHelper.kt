package com.issueissyu.fe.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.issueissyu.fe.MainActivity
import com.issueissyu.fe.R

enum class PushType(
    val serverCode: String,
    val channelId: String,
    val channelName: String,
    private val fixedNotificationId: Int? = null,
) {
    PIN_LIKED("PIN_LIKED", "channel_like", "내 핀 좋아요", 1001),
    PIN_EVENT("PIN_EVENT", "channel_event", "이벤트"),
    PIN_POPULAR("PIN_POPULAR", "channel_popular", "인기 게시글"),
    PIN_STORE_AD("PIN_STORE_AD", "channel_store_ad", "가게 홍보"),
    ;

    fun resolveDestination(pinId: String?, communityId: String?): PushDestination? = when (this) {
        PIN_LIKED -> pinId?.takeIf { it.isNotBlank() }?.let { PushDestination.PinDetail(it) }
        PIN_EVENT, PIN_STORE_AD -> communityId?.toLongOrNull()?.let { PushDestination.CommunityDetail(it) }
        PIN_POPULAR -> null
    }

    fun notificationId(): Int =
        fixedNotificationId ?: (System.currentTimeMillis() and 0x7FFFFFFF).toInt()

    companion object {
        fun fromServer(value: String?): PushType? =
            entries.find { it.serverCode.equals(value, ignoreCase = true) }
    }
}

object NotificationHelper {

    const val EXTRA_TYPE = "type"
    const val EXTRA_PIN_ID = "pinId"
    const val EXTRA_COMMUNITY_ID = "communityId"
    const val EXTRA_ALARM_ID = "alarmId"

    fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        PushType.entries.forEach { type ->
            manager.createNotificationChannel(
                NotificationChannel(type.channelId, type.channelName, NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }

    fun show(
        context: Context,
        type: String?,
        pinId: String?,
        communityId: String?,
        title: String,
        body: String,
        alarmId: String? = null,
    ) {
        val pushType = PushType.fromServer(type) ?: return

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_TYPE, pushType.serverCode)
            putExtra(EXTRA_PIN_ID, pinId)
            putExtra(EXTRA_COMMUNITY_ID, communityId)
            putExtra(EXTRA_ALARM_ID, alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, pushType.channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_report) // 나중에 아이콘 교체
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        NotificationManagerCompat.from(context).notify(pushType.notificationId(), notification)
    }
}

sealed interface PushDestination {
    data class PinDetail(val pinId: String) : PushDestination
    data class CommunityDetail(val communityId: Long) : PushDestination
}

fun Intent?.parsePushDestination(): PushDestination? {
    if (this == null) return null
    val pushType = PushType.fromServer(
        getStringExtra(NotificationHelper.EXTRA_TYPE) ?: getStringExtra("type")
    ) ?: return null

    return pushType.resolveDestination(readPinId(), readCommunityId())
}

fun Intent?.readPushAlarmId(): Long? {
    if (this == null) return null
    return (
        getStringExtra(NotificationHelper.EXTRA_ALARM_ID)
            ?: getStringExtra("alarmId")
        )?.toLongOrNull()
}

fun Intent.clearPushExtras() {
    removeExtra(NotificationHelper.EXTRA_TYPE)
    removeExtra(NotificationHelper.EXTRA_PIN_ID)
    removeExtra(NotificationHelper.EXTRA_COMMUNITY_ID)
    removeExtra(NotificationHelper.EXTRA_ALARM_ID)
}

private fun Intent.readPinId(): String? = (
    getStringExtra(NotificationHelper.EXTRA_PIN_ID)
        ?: getStringExtra("pinId")
    )?.takeIf { it.isNotBlank() }

private fun Intent.readCommunityId(): String? =
    getStringExtra(NotificationHelper.EXTRA_COMMUNITY_ID)
        ?: getStringExtra("communityId")
