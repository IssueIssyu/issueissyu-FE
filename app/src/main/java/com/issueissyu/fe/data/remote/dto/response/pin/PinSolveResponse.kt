package com.issueissyu.fe.data.remote.dto.response.pin

data class PinSolveResponse(
    val isPetitioned: Boolean? = null,
    val userProblemSolverId: Long? = null,
    val userProblemSolveState: String? = null,
    val isProblemSolver: Boolean = false,
    val reliability: Int? = null,
)
