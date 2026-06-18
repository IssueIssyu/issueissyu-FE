package com.issueissyu.fe.data.remote.dto.response.pin

data class IssuePinEditResponse(
    val issuePinId: Long = 0,
    val pinId: Long = 0,
    val pinType: String = "",
    val pinTitle: String = "",
    val pinContent: String = "",
    val issuePinState: String? = null,
    val pinDetailAddress: String = "",
    val pinImageUrls: List<PinImageResponse> = emptyList(),
    val likeCount: Int = 0,
    val isLike: Boolean = false,
    val pinUserId: String? = null,
    val pinUserNickname: String? = null,
    val reliabilityStatus: String? = null,
    val imageUploadStatus: String? = null,
    val isUpdated: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String? = null,
    val view: Int = 0,
    val isReported: Boolean = false,
    val isMine: Boolean = false,
    val communityId: Long? = null,
    val rateLimitQuota: RateLimitQuotaResponse? = null,
)

data class RateLimitQuotaResponse(
    val enabled: Boolean = false,
    val dailyLimit: Int = 0,
    val usedCount: Int = 0,
    val remainingCount: Int = 0,
    val resetAt: String? = null,
)
