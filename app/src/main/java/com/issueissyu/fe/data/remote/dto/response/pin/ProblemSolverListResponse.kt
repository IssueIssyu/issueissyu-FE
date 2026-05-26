package com.issueissyu.fe.data.remote.dto.response.pin

data class ProblemSolverListResponse(
    val isGoNow: Boolean,
    val problemSolvers: List<ProblemSolverItemResponse> = emptyList(),
)

data class ProblemSolverItemResponse(
    val problemSolverId: Long,
    val problemSolveState: String,
    val problemSolverImageUrl: String?,
    val nickname: String,
    val createdAt: String,
    val profileUrl: String,
    val checkAction: String?
)