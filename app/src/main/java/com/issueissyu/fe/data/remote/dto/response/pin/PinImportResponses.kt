package com.issueissyu.fe.data.remote.dto.response.pin

data class PinImportResponse(
    val pinId: Long = 0,
    val pinType: String? = null,
    val region: String? = null,
    val pinDetailAddress: String? = null,
    val pinImageUrls: List<PinImageWithIdResponse>? = null,
    val toneType: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class PinImageWithIdResponse(
    val pinImageId: Long = 0,
    val pinImageUrl: String = "",
    val isMain: Boolean = false,
)
