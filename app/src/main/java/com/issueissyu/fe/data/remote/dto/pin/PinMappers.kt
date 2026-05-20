package com.issueissyu.fe.data.remote.dto.pin

import com.issueissyu.fe.data.remote.dto.response.pin.PinDetailHomeResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojiDto
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojisResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinImageResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinLikeResponse
import com.issueissyu.fe.domain.model.pin.CommunicationPinDetail
import com.issueissyu.fe.domain.model.pin.FestivalPinDetail
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.domain.model.pin.PinDetail
import com.issueissyu.fe.domain.model.pin.PinEmoji
import com.issueissyu.fe.domain.model.pin.PinEmojis
import com.issueissyu.fe.domain.model.pin.PinLike
import com.issueissyu.fe.domain.model.pin.PinUser
import com.issueissyu.fe.domain.model.pin.ResolutionStatus
import com.issueissyu.fe.domain.model.pin.ShopPinDetail

fun PinDetailHomeResponse.toPin(
    coordinate: PinCoordinate = PinCoordinate(latitude = 0.0, longitude = 0.0),
): Pin {
    return Pin(
        id = pinId.toString(),
        title = pinTitle,
        description = pinContent,
        coordinate = coordinate,
        address = pinDetailAddress,
        locationName = null,
        imageUrls = pinImageUrls.toImageUrls(),
        viewCount = viewCount,
        sympathyCount = likeCount,
        isSympathizedByMe = isLike,
        communityPostId = communityId?.toString(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        detail = toPinDetail(),
    )
}

fun PinLikeResponse.toPinLike(): PinLike {
    return PinLike(
        pinId = pinId,
        pinLikeCount = pinLikeCount,
        isLike = isLike,
    )
}

fun PinEmojisResponse.toPinEmojis(): PinEmojis {
    return PinEmojis(
        selectedEmojiId = selectedEmojiId,
        emojis = emojis.orEmpty().mapNotNull { it.toPinEmoji() },
    )
}

private fun PinDetailHomeResponse.toPinDetail(): PinDetail {
    val category = pinType.toPinCategory()
    val writer = toPinUserOrNull()
    return when (category) {
        PinCategory.ISSUE -> IssuePinDetail(
            writer = writer ?: unknownWriter(),
            resolutionStatus = issuePinState.toResolutionStatus()
                ?: ResolutionStatus.BEFORE_RESOLUTION,
        )

        PinCategory.COMMUNICATION -> CommunicationPinDetail(
            writer = writer ?: unknownWriter(),
        )

        PinCategory.SHOP -> ShopPinDetail(
            keywords = emptyList(),
            currentNews = discount,
        )

        PinCategory.FESTIVAL -> FestivalPinDetail(
            keywords = emptyList(),
        )
    }
}

private fun PinDetailHomeResponse.toPinUserOrNull(): PinUser? {
    val id = pinUserId?.takeIf { it.isNotBlank() } ?: return null
    return PinUser(
        id = id,
        name = pinUserNickname?.takeIf { it.isNotBlank() } ?: "알 수 없음",
        imageUrl = pinUserProfile,
    )
}

private fun List<PinImageResponse>.toImageUrls(): List<String> {
    return sortedWith(compareByDescending { it.isMain })
        .mapNotNull { image ->
            image.pinImageUrl.takeIf { it.isNotBlank() }
        }
}

private fun String.toPinCategory(): PinCategory {
    return when (trim().uppercase()) {
        "ISSUE" -> PinCategory.ISSUE
        "COMMUNICATION" -> PinCategory.COMMUNICATION
        "STORE", "SHOP" -> PinCategory.SHOP
        "FESTIVAL" -> PinCategory.FESTIVAL
        else -> throw IllegalArgumentException("Unknown pin type: $this")
    }
}

private fun String?.toResolutionStatus(): ResolutionStatus? {
    return when (this?.trim()?.uppercase()) {
        "BEFORE_RESOLUTION" -> ResolutionStatus.BEFORE_RESOLUTION
        "IN_PROGRESS" -> ResolutionStatus.IN_PROGRESS
        "RESOLVED" -> ResolutionStatus.RESOLVED
        null, "" -> null
        else -> null
    }
}

private fun PinEmojiDto.toPinEmoji(): PinEmoji? {
    if (emojiId == 0 || emojiImageUrl.isBlank()) return null
    return PinEmoji(
        emojiId = emojiId,
        emojiImageUrl = emojiImageUrl,
        count = count,
        isDefault = isDefault,
        isOwned = isOwned,
        productId = productId,
    )
}

private fun unknownWriter(): PinUser = PinUser(
    id = "",
    name = "알 수 없음",
)