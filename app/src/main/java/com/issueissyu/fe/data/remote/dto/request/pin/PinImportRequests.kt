package com.issueissyu.fe.data.remote.dto.request.pin

data class CommunicationPinImportRequest(
    val lat: Double,
    val lng: Double,
    val pinImages: List<PinImageItemRequest>? = null,
    val pinTitle: String,
    val pinContent: String,
)

data class IssuePinImportRequest(
    val lat: Double,
    val lng: Double,
    val pinTitle: String,
    val pinContent: String,
    val pinImages: List<PinImageItemRequest>? = null,
)

data class PinImageItemRequest(
    val isMain: Boolean,
)

data class IssuePinEditRequest(
    val pinTitle: String,
    val pinContent: String,
    val pinImageUrls: List<IssuePinEditExistingImageRequest>? = null,
    val pinImages: List<PinImageItemRequest>? = null,
)

data class IssuePinEditExistingImageRequest(
    val pinImageUrl: String,
    val isMain: Boolean,
)
