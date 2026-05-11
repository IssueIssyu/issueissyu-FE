package com.issueissyu.fe.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class UserDto(
    val uuid: String,
    @SerializedName("temp_uuid")
    val tempUuid: String,
    @SerializedName("user_name")
    val userName: String
)