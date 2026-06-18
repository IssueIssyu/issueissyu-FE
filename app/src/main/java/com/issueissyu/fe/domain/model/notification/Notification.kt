package com.issueissyu.fe.domain.model.notification

data class Notification(
    val alarmId: Long,
    val type: NotificationType,
    val title: String,
    val body: String,
    val timeAgo: String,
    val isUnread: Boolean,
    val pinId: Long,
    val communityId: Long? = null,
)

data class NotificationPage(
    val items: List<Notification>,
    val hasNext: Boolean,
    val nextCursor: String?,
)

/** 푸시 탭 시 confirm에 사용. FCM *AlarmId와 목록 alarmId 불일치 시 목록 매칭에 쓴다. */
data class PushAlarmContext(
    val fcmAlarmId: Long?,
    val pushType: String?,
    val pinId: Long?,
    val communityId: Long?,
)
