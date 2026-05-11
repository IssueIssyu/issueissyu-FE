package com.issueissyu.fe.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class UserDto(
    val uuid: String,
    val tempUuid: String,
    val userName: String
)