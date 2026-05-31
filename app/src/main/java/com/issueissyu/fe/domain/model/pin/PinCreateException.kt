package com.issueissyu.fe.domain.model.pin

class PinCreateException(
    message: String,
    val isImageRelated: Boolean = false,
    val serverCode: String? = null,
) : Exception(message)
