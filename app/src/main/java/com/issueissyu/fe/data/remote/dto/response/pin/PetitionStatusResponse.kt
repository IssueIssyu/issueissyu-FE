package com.issueissyu.fe.data.remote.dto.response.pin

data class PetitionStatusResponse(
    val pinId: Long = 0L,
    val petitionCount: Int = 0,
    val isPetitioned: Boolean = false,
    val targetPetition: Int = 0,
)
