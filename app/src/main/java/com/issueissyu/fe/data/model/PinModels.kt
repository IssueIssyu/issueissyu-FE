package com.issueissyu.fe.data.model

enum class PinCategory {
    ISSUE, COMMUNICATION, SHOP, FESTIVAL
}

enum class ResolutionStatus {
    BEFORE_RESOLUTION, IN_PROGRESS, RESOLVED
}

data class PinCoordinate(
    val latitude: Double,
    val longitude: Double
)

data class PinUser(
    val id: String,
    val name: String,
    val imageUrl: String? = null
)

data class PinEmojiReaction(
    val emojiId: String,
    val count: Int,
    val reactedByMe: Boolean = false
)

sealed interface PinDetail {
    val category: PinCategory
}

// 새롭게 추가된 인터페이스
sealed interface AuthoredPinDetail : PinDetail {
    val writer: PinUser
}

data class IssuePinDetail(
    override val writer: PinUser,
    val resolutionStatus: ResolutionStatus = ResolutionStatus.BEFORE_RESOLUTION,
    val resolver: PinUser? = null,
    val resolutionProofImageUrls: List<String> = emptyList(),
    val resolvedAt: String? = null,
    val petitionCount: Int = 0,
    val isPetitionedByMe: Boolean = false
) : AuthoredPinDetail { // AuthoredPinDetail 구현
    override val category = PinCategory.ISSUE
}

data class CommunicationPinDetail(
    override val writer: PinUser
) : AuthoredPinDetail { // AuthoredPinDetail 구현
    override val category = PinCategory.COMMUNICATION
}

data class ShopPinDetail(
    val keywords: List<String> = emptyList(),
    val currentNews: String? = null
) : PinDetail {
    override val category = PinCategory.SHOP
}

data class FestivalPinDetail(
    val keywords: List<String> = emptyList(),
    val startDate: String? = null,
    val endDate: String? = null
) : PinDetail {
    override val category = PinCategory.FESTIVAL
}

data class Pin(
    val id: String,
    val title: String,
    val description: String,
    val coordinate: PinCoordinate,
    val address: String,
    val locationName: String? = null,
    val neighborhoodId: String? = null,
    val neighborhoodName: String? = null,
    val imageUrls: List<String> = emptyList(),
    val viewCount: Int = 0,
    val sympathyCount: Int = 0,
    val emojiReactions: List<PinEmojiReaction> = emptyList(),
    val communityPostId: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val detail: PinDetail
) {
    val category: PinCategory
        get() = detail.category
}

data class CreatePinRequest(
    val category: PinCategory,
    val title: String,
    val description: String,
    val coordinate: PinCoordinate,
    val address: String,
    val locationName: String? = null,
    val neighborhoodId: String? = null,
    val neighborhoodName: String? = null,
    val imageUrls: List<String> = emptyList()
)

data class UpdatePinRequest(
    val title: String,
    val description: String,
    val coordinate: PinCoordinate,
    val address: String,
    val locationName: String? = null,
    val neighborhoodId: String? = null,
    val neighborhoodName: String? = null,
    val imageUrls: List<String> = emptyList()
)

fun Pin.canEditBy(userId: String): Boolean {
    if (communityPostId != null) return false

    return (detail as? AuthoredPinDetail)?.writer?.id == userId
}