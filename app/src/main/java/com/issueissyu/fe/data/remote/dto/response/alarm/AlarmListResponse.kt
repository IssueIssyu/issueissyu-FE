package com.issueissyu.fe.data.remote.dto.response.alarm

import android.util.Log
import com.issueissyu.fe.core.time.formatAlarmTimeAgo
import com.issueissyu.fe.domain.model.notification.Notification
import com.issueissyu.fe.domain.model.notification.NotificationPage
import com.issueissyu.fe.domain.model.notification.NotificationType

data class AlarmListResponse(
    val alarms: List<Alarm>,
    val pageInfo: PageInfo,
)

data class Alarm(
    val alarmId: Long,
    val isConfirmed: Boolean,
    val alarmType: String,
    val alarmTitle: String,
    val alarmBody: String,
    val pinId: Long,
    val communityId: Long?,
    val createdAt: String,
    val timeAgo: String,
)

data class PageInfo(
    val hasNext: Boolean,
    val nextCursor: String? = null,
)

fun AlarmListResponse.toNotificationPage(): NotificationPage {
    return NotificationPage(
        items = alarms.mapNotNull { it.toNotification() },
        hasNext = pageInfo.hasNext,
        nextCursor = pageInfo.nextCursor?.takeIf { it.isNotBlank() },
    )
}

fun Alarm.toNotification(): Notification? {
    val type = NotificationType.fromServer(alarmType)
    if (type == null) {
        Log.w(TAG, "Unknown alarmType=$alarmType alarmId=$alarmId — item skipped")
        return null
    }
    return Notification(
        alarmId = alarmId,
        type = type,
        title = alarmTitle,
        body = alarmBody,
        timeAgo = formatAlarmTimeAgo(timeAgo),
        isUnread = !isConfirmed,
        pinId = pinId,
        communityId = communityId,
    )
}

private const val TAG = "AlarmListResponse"
