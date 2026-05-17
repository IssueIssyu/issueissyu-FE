package com.issueissyu.fe.data.remote.dto.response.auth

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: UserDto,
    val socialType: String,
    val isNew: Boolean
)