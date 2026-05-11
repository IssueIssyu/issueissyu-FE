package com.issueissyu.fe.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: UserDto,
    val new: Boolean,
    val isNew: Boolean,

    @SerializedName("social_type")
    val socialType: String
)