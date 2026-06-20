package com.issueissyu.fe.domain.model.pin

data class PetitionStatus(
    val pinId: Long,
    val petitionCount: Int,
    val isPetitioned: Boolean,
    val targetPetition: Int,
)
