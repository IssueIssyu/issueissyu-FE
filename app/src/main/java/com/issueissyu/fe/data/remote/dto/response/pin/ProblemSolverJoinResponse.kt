package com.issueissyu.fe.data.remote.dto.response.pin

data class ProblemSolverJoinResponse (
    val pinId: Long,
    val problemSolverId: Long,
    val problemSolveState: String
)