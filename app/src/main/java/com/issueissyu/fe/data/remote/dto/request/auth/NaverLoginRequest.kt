package com.issueissyu.fe.data.remote.dto.request.auth

data class NaverLoginRequest(
    val accessToken: String,
    val refreshToken: String,
)
