package com.issueissyu.fe.data.remote.dto.response.auth

data class UsernameAvailabilityDto(
    val userName: String? = null,
    val isAvailableUsername: Boolean,
)
