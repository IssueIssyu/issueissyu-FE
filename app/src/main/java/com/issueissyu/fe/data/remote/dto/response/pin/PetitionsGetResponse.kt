package com.issueissyu.fe.data.remote.dto.response.pin

data class PetitionsGetResponse (
    val pinId: Long,
    val petitionCount: Int,
    val isPetitioned: Boolean,
    val targetPetition: Long
)