package com.issueissyu.fe.data.remote.dto.response.pin

data class PetitionsJoinResponse (
    val pinId: Long,
    val petitionCount: Int,
    val isPetitioned: Boolean
)