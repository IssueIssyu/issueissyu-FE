package com.issueissyu.fe.data.remote.dto.response.pin

data class PetitionSubmitResponse(
    val pinId: Long = 0L,
    val petitionCount: Int = 0,
    val isPetitioned: Boolean = false,
)
