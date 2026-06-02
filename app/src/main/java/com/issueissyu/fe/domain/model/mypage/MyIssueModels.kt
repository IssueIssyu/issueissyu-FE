package com.issueissyu.fe.domain.model.mypage

import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.ResolutionStatus

data class MyIssuePage(
    val items: List<MyIssuePin>,
    val hasNext: Boolean,
    val nextCursor: String?,
)

data class MyIssuePin(
    val pinId: Long,
    val pinType: PinCategory,
    val title: String,
    val address: String,
    val createdAt: String,
    val resolutionStatus: ResolutionStatus?,
)
