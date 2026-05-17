package com.issueissyu.fe.data.remote.dto.response.auth

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)