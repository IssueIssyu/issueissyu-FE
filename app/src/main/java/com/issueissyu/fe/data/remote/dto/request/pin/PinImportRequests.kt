package com.issueissyu.fe.data.remote.dto.request.pin

data class CommunicationPinImportRequest(
    val lat: Double,
    val lng: Double,
    val pinImageUrls: List<PinImageItemRequest>,
    val pinTitle: String,
    val pinContent: String,
)

data class PinImageItemRequest(
    val pinImageUrl: String,
    val isMain: Boolean,
)
