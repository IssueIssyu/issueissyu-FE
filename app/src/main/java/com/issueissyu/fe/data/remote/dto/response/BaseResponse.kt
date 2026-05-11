package com.issueissyu.fe.data.remote.dto.response

data class BaseResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T?
)