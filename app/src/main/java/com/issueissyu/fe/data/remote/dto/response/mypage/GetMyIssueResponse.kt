package com.issueissyu.fe.data.remote.dto.response.mypage

data class GetMyIssueResponse(
    val pins: List<PinInfo>,
    val pageInfo: PageInfo,
)

data class PinInfo(
    val pinId: Long,
    val pinType: String,
    val pinTitle: String,
    val pinDetailAddress: String,
    val issuePinState: String?,
    val createdAt: String,
)

data class PageInfo(
    val hasNext: Boolean,
    val nextCursor: String?,
)
