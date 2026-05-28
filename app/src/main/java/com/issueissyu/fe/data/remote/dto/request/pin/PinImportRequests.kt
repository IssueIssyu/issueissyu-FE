package com.issueissyu.fe.data.remote.dto.request.pin

data class CommunicationPinImportRequest(
    val lat: Double,
    val lng: Double,
    val pinImages: List<PinImageItemRequest>? = null,
    val pinTitle: String,
    val pinContent: String,
)

data class PinImageItemRequest(
    val isMain: Boolean,
)
