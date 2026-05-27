package com.issueissyu.fe.data.remote.dto.response.pin

data class PinCommentDto(
    val commentId: Long,
    val nickname: String?,
    val profileImageUrl: String?,
    val commentContent: String,
    val edited: Boolean,
    val createdAt: String,
    val mine: Boolean,
)
