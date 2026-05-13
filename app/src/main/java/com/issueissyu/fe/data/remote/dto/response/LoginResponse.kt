package com.issueissyu.fe.data.remote.dto.response

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: UserDto,
    val socialType: String,
    val isNew: Boolean
)