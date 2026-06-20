package com.issueissyu.fe.data.remote.dto.response.pin

data class PinLikeResponse(
    val pinId: Long = 0,
    val pinLikeCount: Int = 0,
    val isLike: Boolean = false,
)