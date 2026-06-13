package com.issueissyu.fe.data.remote.dto.response.map

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
    val communityId: Long? = null,
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
    val type = NotificationType.fromServer(alarmType) ?: return null
    return Notification(
        id = alarmId.toString(),
        type = type,
        title = alarmTitle,
        body = alarmBody,
        time = timeAgo,
        isUnread = !isConfirmed,
        createdAtEpochMillis = 0L,
        targetId = pinId.toString(),
        communityId = communityId,
    )
}
