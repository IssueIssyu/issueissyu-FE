package com.issueissyu.fe.data.remote.dto.request

data class PhoneVerifyRequest(
    val phone: String,
    val code: String,
    val isAvailableNickname: Boolean,
)
