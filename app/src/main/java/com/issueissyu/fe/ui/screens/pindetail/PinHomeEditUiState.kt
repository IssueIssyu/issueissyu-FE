package com.issueissyu.fe.ui.screens.pindetail

import com.issueissyu.fe.core.constants.PinImageUploadConstraints
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinEditRateLimitQuota
import com.issueissyu.fe.domain.model.pin.PinImageRef
import com.issueissyu.fe.domain.model.pin.UpdatePinEditRequest
import com.issueissyu.fe.domain.model.pin.homeEditImageAttachments
import com.issueissyu.fe.domain.model.pin.homeEditMainImageKey

data class PinHomeEditUiState(
    val isActive: Boolean = false,
    val title: String = "",
    val description: String = "",
    val existingImages: List<PinImageRef> = emptyList(),
    val newImageUris: List<String> = emptyList(),
    val mainImageKey: String? = null,
    val baselineExistingImages: List<PinImageRef> = emptyList(),
    val baselineMainImageKey: String? = null,
    val isSubmitting: Boolean = false,
    val isLoadingQuota: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val rateLimitQuota: PinEditRateLimitQuota? = null,
    val toneOptions: List<String> = emptyList(),
    val selectedTone: String? = null,
    val isLoadingToneOptions: Boolean = false,
    val isGeneratingAiContent: Boolean = false,
    val isLoadingAiDraftQuota: Boolean = false,
    val showAiDraftConfirmDialog: Boolean = false,
    val aiDraftRateLimitQuota: PinEditRateLimitQuota? = null,
) {
    val photoCount: Int get() = existingImages.size + newImageUris.size

    val isSubmittingHomeEdit: Boolean
        get() = isSubmitting ||
            isLoadingQuota ||
            isGeneratingAiContent ||
            isLoadingAiDraftQuota

    fun toUpdateRequest(): UpdatePinEditRequest? {
        val trimmedTitle = title.trim()
        val trimmedDescription = description.trim()
        if (trimmedTitle.isBlank() || trimmedDescription.isBlank()) return null
        val mainKey = mainImageKey
        return UpdatePinEditRequest(
            title = trimmedTitle,
            description = trimmedDescription,
            existingImages = existingImages.map { image ->
                image.copy(isMain = image.imageUrl == mainKey)
            },
            newImageUris = newImageUris,
            mainNewImageUri = newImageUris.firstOrNull { it == mainKey },
            baselineExistingImages = baselineExistingImages,
            baselineMainImageKey = baselineMainImageKey,
        )
    }

    fun cleared() = PinHomeEditUiState()

    fun openedFrom(pin: Pin): PinHomeEditUiState {
        val attachments = pin.homeEditImageAttachments()
        val mainKey = attachments.homeEditMainImageKey()
        return PinHomeEditUiState(
            isActive = true,
            title = pin.title,
            description = pin.description,
            existingImages = attachments,
            newImageUris = emptyList(),
            mainImageKey = mainKey,
            baselineExistingImages = attachments,
            baselineMainImageKey = mainKey,
        )
    }

    fun withAddedImageUris(uris: List<String>): PinHomeEditUiState {
        if (uris.isEmpty()) return this
        val maxNew = (PinImageUploadConstraints.MAX_COUNT - existingImages.size).coerceAtLeast(0)
        val merged = (newImageUris + uris).distinct().take(maxNew)
        val resolvedMainKey = mainImageKey
            ?: existingImages.homeEditMainImageKey()
            ?: merged.firstOrNull()
        return copy(newImageUris = merged, mainImageKey = resolvedMainKey)
    }

    fun withRemovedExistingImage(imageUrl: String): PinHomeEditUiState {
        val remaining = existingImages.filterNot { it.imageUrl == imageUrl }
        val resolvedMainKey = when (mainImageKey) {
            imageUrl -> remaining.homeEditMainImageKey() ?: newImageUris.firstOrNull()
            else -> mainImageKey
        }
        return copy(existingImages = remaining, mainImageKey = resolvedMainKey)
    }

    fun withRemovedNewImageUri(uri: String): PinHomeEditUiState {
        val remaining = newImageUris.filterNot { it == uri }
        val resolvedMainKey = when (mainImageKey) {
            uri -> existingImages.homeEditMainImageKey() ?: remaining.firstOrNull()
            else -> mainImageKey
        }
        return copy(newImageUris = remaining, mainImageKey = resolvedMainKey)
    }
}

data class PinHomeEditCallbacks(
    val onTitleChange: (String) -> Unit = {},
    val onDescriptionChange: (String) -> Unit = {},
    val onCancel: () -> Unit = {},
    val onSubmit: () -> Unit = {},
    val onPhotoAddClick: () -> Unit = {},
    val onExistingImageRemove: (String) -> Unit = {},
    val onNewImageRemove: (String) -> Unit = {},
    val onMainImageSelect: (String) -> Unit = {},
    val onToneChange: (String) -> Unit = {},
    val onAiDraftClick: () -> Unit = {},
)
