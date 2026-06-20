package com.issueissyu.fe.domain.model.auth

data class OnboardingProfile(
    val uuid: String,
    val socialType: String,
    val userCustomCollectionId: Int,
    val customCollectionId: Int,
    val customCollectionName: String,
    val customCollectionUrl: String,
)
