package com.issueissyu.fe.domain.model.community

data class CommunityComment(
    val commentId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val content: String,
    val isEdited: Boolean,
    val createdAt: String?,
    val isMine: Boolean,
)
