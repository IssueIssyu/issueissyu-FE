package com.issueissyu.fe.data.remote.dto.pin

import com.issueissyu.fe.data.remote.dto.response.pin.PinCommentDto
import com.issueissyu.fe.data.remote.dto.response.pin.PinDetailHomeResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinDetailPostResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojiDto
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojisResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinLikeResponse
import com.issueissyu.fe.domain.model.pin.CommunicationPinDetail
import com.issueissyu.fe.domain.model.pin.FestivalPinDetail
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinComment
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.domain.model.pin.PinDetail
import com.issueissyu.fe.domain.model.pin.PinEmoji
import com.issueissyu.fe.domain.model.pin.PinEmojiCandidate
import com.issueissyu.fe.domain.model.pin.PinEmojiReaction
import com.issueissyu.fe.domain.model.pin.PinEmojis
import com.issueissyu.fe.domain.model.pin.PinImageRef
import com.issueissyu.fe.domain.model.pin.PinLike
import com.issueissyu.fe.domain.model.pin.PinPostSympathyContent
import com.issueissyu.fe.domain.model.pin.PinUser
import com.issueissyu.fe.domain.model.pin.ResolutionStatus
import com.issueissyu.fe.domain.model.pin.ShopPinDetail

data class PinDetailHomeImages(
    val attachments: List<PinImageRef>,
    val displayUrls: List<String>
)
fun PinCommentDto.toPinComment(): PinComment {
    return PinComment(
        commentId = commentId,
        nickname = nickname?.takeIf { it.isNotBlank() } ?: "알 수 없음",
        profileImageUrl = profileImageUrl?.takeIf { it.isNotBlank() },
        content = commentContent,
        edited = edited,
        createdAt = createdAt,
        isMine = mine,
    )
}

fun PinDetailPostResponse.toPostSympathyContent(): PinPostSympathyContent {
    val writer = pinUserId?.takeIf { it.isNotBlank() }?.let { id ->
        PinUser(
            id = id,
            name = pinUserNickname?.takeIf { it.isNotBlank() } ?: "알 수 없음",
            imageUrl = pinUserProfile,
        )
    }
    return PinPostSympathyContent(
        pinId = pinId,
        pinType = pinType.toPinCategoryOrNull() ?: PinCategory.ISSUE,
        pinTitle = pinTitle,
        sympathyCount = likeCount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
        isSympathizedByMe = isLike,
        writer = writer,
        discount = discount?.takeIf { it.isNotBlank() },
        mainPinImageUrl = mainPinImageUrl?.takeIf { it.isNotBlank() },
        storeImageUrl = storeImageUrl?.takeIf { it.isNotBlank() },
    )
}

fun PinDetailHomeResponse.toPin(
    coordinate: PinCoordinate = PinCoordinate(latitude = 0.0, longitude = 0.0),
): Pin {
    val author = toPinUserOrNull()
    val images = toPinImages()
    return Pin(
        id = pinId.toString(),
        title = pinTitle,
        description = pinContent,
        coordinate = coordinate,
        address = pinDetailAddress,
        locationName = null,
        imageUrls = images.displayUrls,
        imageAttachments = images.attachments,
        viewCount = viewCount,
        sympathyCount = likeCount,
        isSympathizedByMe = isLike,
        communityPostId = communityId?.toString(),
        author = author,
        isMine = isMine,
        isReported = isReported,
        isUpdated = isUpdated,
        createdAt = createdAt,
        updatedAt = updatedAt,
        detail = toPinDetail(author),
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

fun PinEmojis.toEmojiReactions(): List<PinEmojiReaction> {
    return emojis.map { emoji ->
        PinEmojiReaction(
            emojiId = emoji.emojiId.toString(),
            count = emoji.count,
            reactedByMe = emoji.emojiId == selectedEmojiId,
            emojiImageUrl = emoji.emojiImageUrl.takeIf { it.isNotBlank() },
        )
    }
}

fun PinEmojiDto.toPinEmojiCandidate(): PinEmojiCandidate? {
    if (emojiId == 0L || emojiId > Int.MAX_VALUE) return null
    return PinEmojiCandidate(
        emojiId = emojiId.toInt(),
        emojiImageUrl = emojiImageUrl,
        isDefault = isDefault,
        isOwned = owned,
        productId = productId,
    )
}

private fun PinDetailHomeResponse.toPinDetail(author: PinUser?): PinDetail {
    val category = pinType.toPinCategory()
    val writer = author ?: unknownWriter()
    return when (category) {
        PinCategory.ISSUE -> IssuePinDetail(
            writer = writer,
            resolutionStatus = issuePinState.toResolutionStatus() ?: ResolutionStatus.BEFORE_RESOLUTION,
        )
        PinCategory.COMMUNICATION -> CommunicationPinDetail(
            writer = writer,
        )
        PinCategory.SHOP -> ShopPinDetail(
            keywords = DETAIL_HOME_NO_KEYWORDS,
            currentNews = discount,
        )
        PinCategory.FESTIVAL -> FestivalPinDetail(
            keywords = DETAIL_HOME_NO_KEYWORDS,
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

private val DETAIL_HOME_NO_KEYWORDS: List<String> = emptyList()

private fun PinDetailHomeResponse.toPinImages(): PinDetailHomeImages {
    val sorted = pinImageUrls.sortedByDescending { it.isMain }
    val attachments = sorted.map {
        PinImageRef(
            pinImageId = it.pinImageId,
            imageUrl = it.pinImageUrl,
            isMain = it.isMain,
        )
    }
    val urlsFromPin = attachments.mapNotNull { ref -> ref.imageUrl.takeIf { it.isNotBlank() } }
    val storeImage = storeImageUrl?.takeIf { it.isNotBlank() }
    val allUrls = if (storeImage == null) urlsFromPin else (urlsFromPin + storeImage).distinct()
    return PinDetailHomeImages(attachments = attachments, displayUrls = allUrls)
}

private fun String.toPinCategory(): PinCategory {
    return toPinCategoryOrNull()
        ?: throw IllegalArgumentException("Unknown pin type: $this")
}

private fun String.toPinCategoryOrNull(): PinCategory? {
    return when (trim().uppercase()) {
        "ISSUE" -> PinCategory.ISSUE
        "COMMUNICATION" -> PinCategory.COMMUNICATION
        "STORE", "SHOP" -> PinCategory.SHOP
        "FESTIVAL" -> PinCategory.FESTIVAL
        else -> null
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
    if (emojiId == 0L || emojiImageUrl.isBlank()) return null
    return PinEmoji(
        emojiId = emojiId,
        emojiImageUrl = emojiImageUrl,
        count = count,
        isDefault = isDefault,
        isOwned = owned,
        productId = productId,
    )
}

private fun unknownWriter(): PinUser = PinUser(
    id = "",
    name = "알 수 없음",
)