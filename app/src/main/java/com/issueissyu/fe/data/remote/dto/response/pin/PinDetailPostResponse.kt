package com.issueissyu.fe.data.remote.dto.response.pin

data class PinDetailPostResponse(
    val pinId: Long,
    val pinType: String,
    val pinTitle: String,

    val likeCount: Long,
    val isLike: Boolean,

    val pinUserId: String?,
    val pinUserProfile: String?,
    val pinUserNickname: String?,

    val mainPinImageUrl: String?,

    val discount: String?,
    val storeImageUrl: String?
)