package com.issueissyu.fe.data.remote.dto.response.pin

data class CommunicationPinEditResponse(
    val pinId: Long = 0,
    val pinType: String = "",
    val pinTitle: String = "",
    val pinContent: String = "",
    val region: String? = null,
    val pinDetailAddress: String = "",
    val pinImageUrls: List<PinImageResponse> = emptyList(),
    val toneType: String? = null,
    val createdAt: String = "",
    val updatedAt: String? = null,
)