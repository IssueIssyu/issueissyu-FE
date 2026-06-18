package com.issueissyu.fe.domain.model.mypage

import com.issueissyu.fe.domain.model.pin.ResolutionStatus

data class MySolverPinPage(
    val items: List<MySolverPin>,
    val hasNext: Boolean,
    val nextCursor: String?,
)

data class MySolverPin(
    val pinId: Long,
    val title: String,
    val address: String,
    val createdAt: String,
    val resolutionStatus: ResolutionStatus,
)
