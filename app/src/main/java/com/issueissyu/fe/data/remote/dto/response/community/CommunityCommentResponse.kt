package com.issueissyu.fe.data.remote.dto.response.community

data class CommunityCommentResponse(
    val commentId: Long? = null,
    val nickname: String? = null,
    val profileImageUrl: String? = null,
    val commentContent: String? = null,
    val edited: Boolean? = null,
    val createdAt: String? = null,
    val mine: Boolean? = null,
)
