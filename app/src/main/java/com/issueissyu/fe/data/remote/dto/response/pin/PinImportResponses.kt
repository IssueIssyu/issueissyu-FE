package com.issueissyu.fe.data.remote.dto.response.pin

data class PinImportResponse(
    val pinId: Long = 0,
    val pinType: String? = null,
    val region: String? = null,
    val pinDetailAddress: String? = null,
    val pinImageUrls: List<PinImageResponse>? = null,
    val toneType: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
