package com.issueissyu.fe.data.remote.dto.response.pin

data class PinSolveResponse (
    val isPetitioned: Boolean,
    val userProblemSolverId: Long?,
    val userProblemSolveState: String?
)