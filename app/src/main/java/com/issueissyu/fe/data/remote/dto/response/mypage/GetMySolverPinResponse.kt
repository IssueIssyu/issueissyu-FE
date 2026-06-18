package com.issueissyu.fe.data.remote.dto.response.mypage

data class GetMySolverPinResponse(
    val pins: List<SolverPinInfo>,
    val pageInfo: PageInfo,
)

data class SolverPinInfo(
    val pinId: Long,
    val pinTitle: String,
    val pinDetailAddress: String,
    val issuePinState: String,
    val createdAt: String,
)
