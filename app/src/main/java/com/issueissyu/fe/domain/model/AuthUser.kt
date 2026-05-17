package com.issueissyu.fe.domain.model

data class AuthUser(
    val uuid: String,
    val userName: String,
    val isNew: Boolean
)