package com.issueissyu.fe.domain.model.mypage

data class MySolverPinPage(
    val items: List<MyIssuePin>,
    val hasNext: Boolean,
    val nextCursor: String?,
)
