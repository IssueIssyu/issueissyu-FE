package com.issueissyu.fe.data.remote.dto.pin

import com.issueissyu.fe.data.remote.dto.response.pin.PinCommentDto
import com.issueissyu.fe.data.remote.dto.response.pin.PinDetailHomeResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinDetailPostResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojiDto
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojisResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinSolveResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PetitionsGetResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PetitionsJoinResponse
import com.issueissyu.fe.data.remote.dto.response.pin.ProblemSolverJoinResponse
import com.issueissyu.fe.data.remote.dto.response.pin.ProblemSolverItemResponse
import com.issueissyu.fe.data.remote.dto.response.pin.ProblemSolverListResponse
import com.issueissyu.fe.data.remote.dto.response.pin.ProblemSolverPhotoResponse
import com.issueissyu.fe.data.remote.dto.response.pin.ProblemSolverVerificationResponse
import com.issueissyu.fe.core.text.decodePinContentNewlines
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
import com.issueissyu.fe.domain.model.pin.PinSolveInfo
import com.issueissyu.fe.domain.model.pin.PinUser
import com.issueissyu.fe.domain.model.pin.PetitionJoinInfo
import com.issueissyu.fe.domain.model.pin.PetitionStatusInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverJoinInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverParticipantInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverPhotoInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverVerificationInfo
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

fun PinDetailPostResponse.toPostSympathyContentOrNull(): PinPostSympathyContent? {
    val category = pinType.toPinCategoryOrNull() ?: return null
    val writer = pinUserNickname?.takeIf { it.isNotBlank() }?.let { name ->
        PinUser(
            id = pinUserId?.takeIf { it.isNotBlank() }.orEmpty(),
            name = name,
            imageUrl = pinUserProfile?.takeIf { it.isNotBlank() },
        )
    }
    return PinPostSympathyContent(
        pinId = pinId,
        pinType = category,
        pinTitle = pinTitle,
        sympathyCount = likeCount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
        isSympathizedByMe = isLike,
        writer = writer,
        discount = discount?.takeIf { it.isNotBlank() },
        storeImageUrl = storeImageUrl?.takeIf { it.isNotBlank() },
        mainPinImageUrl = mainPinImageUrl?.takeIf { it.isNotBlank() },
    )
}

fun PinDetailHomeResponse.toPinOrNull(
    coordinate: PinCoordinate = PinCoordinate(latitude = 0.0, longitude = 0.0),
): Pin? {
    val category = pinType.toPinCategoryOrNull() ?: return null
    val author = toPinUserOrNull(category)
    val images = toPinImages()
    return Pin(
        id = pinId.toString(),
        title = pinTitle,
        description = pinContent.decodePinContentNewlines(),
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
        storeImageUrl = storeImageUrl?.takeIf { it.isNotBlank() },
        mainPinImageUrl = pinImageUrls
            .firstOrNull { it.isMain }
            ?.pinImageUrl
            ?.takeIf { it.isNotBlank() }
            ?: pinImageUrls.firstOrNull()?.pinImageUrl?.takeIf { it.isNotBlank() },
        isMine = isMine,
        isReported = isReported,
        isUpdated = isUpdated,
        createdAt = createdAt,
        updatedAt = updatedAt,
        detail = toPinDetail(category, author),
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
//해결하기
fun PinSolveResponse.toPinSolveInfo(): PinSolveInfo {
    return PinSolveInfo(
        isPetitioned = isPetitioned ?: false,
        userProblemSolverId = userProblemSolverId,
        userProblemSolveState = userProblemSolveState,
    )
}

fun PetitionsGetResponse.toPetitionStatusInfo(): PetitionStatusInfo {
    return PetitionStatusInfo(
        pinId = pinId,
        petitionCount = petitionCount,
        isPetitioned = isPetitioned,
        targetPetitionCount = targetPetition.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
    )
}

fun PetitionsJoinResponse.toPetitionJoinInfo(): PetitionJoinInfo {
    return PetitionJoinInfo(
        pinId = pinId,
        petitionCount = petitionCount,
        isPetitioned = isPetitioned,
    )
}

fun ProblemSolverListResponse.toProblemSolverInfo(): ProblemSolverInfo {
    return ProblemSolverInfo(
        isGoNow = isGoNow,
        problemSolvers = problemSolvers.map { it.toProblemSolverParticipantInfo() },
    )
}

fun ProblemSolverItemResponse.toProblemSolverParticipantInfo(): ProblemSolverParticipantInfo {
    return ProblemSolverParticipantInfo(
        problemSolverId = problemSolverId,
        problemSolveState = problemSolveState,
        problemSolverImageUrl = problemSolverImageUrl?.takeIf { it.isNotBlank() },
        nickname = nickname,
        createdAt = createdAt,
        profileUrl = profileUrl?.takeIf { it.isNotBlank() },
        checkAction = checkAction?.takeIf { it.isNotBlank() },
    )
}

fun ProblemSolverJoinResponse.toProblemSolverJoinInfo(): ProblemSolverJoinInfo {
    return ProblemSolverJoinInfo(
        pinId = pinId,
        problemSolverId = problemSolverId.toLong(),
        problemSolveState = problemSolveState,
    )
}

fun ProblemSolverPhotoResponse.toProblemSolverPhotoInfo(): ProblemSolverPhotoInfo {
    return ProblemSolverPhotoInfo(
        photoId = photoId,
        photoUrl = photoUrl,
        problemSolveState = problemSolveState,
    )
}

fun ProblemSolverVerificationResponse.toProblemSolverVerificationInfo(): ProblemSolverVerificationInfo {
    return ProblemSolverVerificationInfo(
        problemSolveState = problemSolveState,
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
    if (emojiId == 0L) return null
    return PinEmojiCandidate(
        emojiId = emojiId,
        emojiImageUrl = emojiImageUrl,
        isDefault = isDefault,
        isOwned = owned,
        productId = productId,
    )
}

private fun PinDetailHomeResponse.toPinDetail(
    category: PinCategory,
    author: PinUser?,
): PinDetail {
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
        PinCategory.FESTIVAL -> FestivalPinDetail(keywords = DETAIL_HOME_NO_KEYWORDS)
    }
}

private fun PinDetailHomeResponse.toPinUserOrNull(category: PinCategory): PinUser? {
    when (category) {
        PinCategory.ISSUE, PinCategory.COMMUNICATION, PinCategory.FESTIVAL -> Unit
        else -> return null
    }
    val name = pinUserNickname?.takeIf { it.isNotBlank() } ?: return null
    return PinUser(
        id = pinUserId?.takeIf { it.isNotBlank() }.orEmpty(),
        name = name,
        imageUrl = pinUserProfile?.takeIf { it.isNotBlank() },
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