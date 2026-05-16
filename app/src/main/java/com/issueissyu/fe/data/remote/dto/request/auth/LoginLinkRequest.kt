package com.issueissyu.fe.data.remote.dto.request.auth

data class LoginLinkRequest(
    val phone: String,
    val socialType: String,
)
