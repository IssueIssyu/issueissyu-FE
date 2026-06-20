package com.issueissyu.fe.domain.model.pin

data class PinSolveStatus(
    val isPetitioned: Boolean,
    val isProblemSolver: Boolean,
    val reliability: Int?,
)
