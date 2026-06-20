package com.issueissyu.fe.data.remote.dto.response.auth

data class OnboardingResponse(
    val uuid: String,
    val socialType: String,
    val userCustomCollectionId: Int,
    val customCollectionId: Int,
    val customCollectionName: String,
    val customCollectionUrl: String,
)