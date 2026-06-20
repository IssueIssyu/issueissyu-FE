package com.issueissyu.fe.data.remote.dto.response.pin

data class PinDetailHomeResponse(
    val pinId: Long,
    val pinType: String,
    val pinTitle: String,
    val pinContent: String,
    val issuePinState: String?,
    val pinDetailAddress: String,
    val likeCount: Int,
    val isLike: Boolean,
    val pinUserId: String?,
    val pinUserProfile: String?,
    val pinUserNickname: String?,
    val pinImageUrls: List<PinImageResponse> = emptyList(),
    val discount: String?,
    val storeImageUrl: String?,
    val isUpdated: Boolean,
    val createdAt: String,
    val updatedAt: String?,
    val viewCount: Int,
    val isReported: Boolean,
    val isMine: Boolean,
    val communityId: Long?,
    val rateLimitQuota: RateLimitQuotaResponse? = null,
)

data class PinImageResponse(
    val pinImageId: Long = 0,
    val pinImageUrl: String = "",
    val isMain: Boolean = false,
)
