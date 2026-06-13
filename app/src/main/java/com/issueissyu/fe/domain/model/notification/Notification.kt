package com.issueissyu.fe.domain.model.notification

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val time: String,
    val isUnread: Boolean,
    val createdAtEpochMillis: Long,
    val targetId: String? = null,
    val communityId: Long? = null,
)

data class NotificationPage(
    val items: List<Notification>,
    val hasNext: Boolean,
    val nextCursor: String?,
)
