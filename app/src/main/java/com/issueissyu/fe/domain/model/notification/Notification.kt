package com.issueissyu.fe.domain.model.notification

data class Notification(
    val id: String,
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
