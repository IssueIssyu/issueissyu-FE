package com.issueissyu.fe.data.remote.dto.request.pin

import com.issueissyu.fe.domain.model.pin.UpdatePinEditRequest
import com.issueissyu.fe.domain.model.pin.hasImageChanges

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

fun UpdatePinEditRequest.toIssuePinEditRequest(): IssuePinEditRequest {
    if (!hasImageChanges()) {
        return IssuePinEditRequest(pinTitle = title, pinContent = description)
    }
    val mainNewUri = mainNewImageUri?.takeIf { newImageUris.contains(it) } ?: newImageUris.firstOrNull()
    val pinImages = newImageUris.takeIf { it.isNotEmpty() }?.map { uri ->
        PinImageItemRequest(isMain = uri == mainNewUri)
    }
    val pinImageUrls = when {
        existingImages.isNotEmpty() -> existingImages.map { image ->
            IssuePinEditExistingImageRequest(pinImageUrl = image.imageUrl, isMain = image.isMain)
        }
        baselineExistingImages.isNotEmpty() -> emptyList()
        else -> null
    }
    return IssuePinEditRequest(
        pinTitle = title,
        pinContent = description,
        pinImageUrls = pinImageUrls,
        pinImages = pinImages,
    )
}
