package com.issueissyu.fe.domain.model.pin

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

// 요건 연결하면서 수정하시면 될듯
data class PinEmojiReaction(
    val emojiId: String,
    val count: Int,
    val reactedByMe: Boolean = false,
    val emojiImageUrl: String? = null
)

// 핀 상세 홈 - 이미지
data class PinImageRef(
    val pinImageId: Long,
    val imageUrl: String,
    val isMain: Boolean,
)

// 이모지 조회
data class PinEmojis(
    val selectedEmojiId: Long?,
    val emojis: List<PinEmoji>,
)

data class PinEmoji(
    val emojiId: Long,
    val emojiImageUrl: String,
    val count: Int,
    val isDefault: Boolean,
    val isOwned: Boolean,
    val productId: String?,
)

data class PinEmojiCandidate(
    val emojiId: Long,
    val emojiImageUrl: String,
    val isDefault: Boolean,
    val isOwned: Boolean,
    val productId: String?,
) {
    val canReact: Boolean
        get() = isDefault || isOwned
}

data class PinLike(
    val pinId: Long,
    val pinLikeCount: Int,
    val isLike: Boolean,
)

//핀 상세 포스트 - 댓글
data class PinComment(
    val commentId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val content: String,
    val edited: Boolean,
    val createdAt: String,
    val isMine: Boolean,
)

//핀 상세 포스트
data class PinDetailDisplayProfile(
    val imageUrl: String?,
    val nickname: String,
)

data class PinPostSympathyContent(
    val pinId: Long,
    val pinType: PinCategory,
    val pinTitle: String,
    val sympathyCount: Int,
    val isSympathizedByMe: Boolean,
    val writer: PinUser?,
    val discount: String? = null,
    val storeImageUrl: String? = null,
    val mainPinImageUrl: String? = null,
) {
    fun detailDisplayProfile(): PinDetailDisplayProfile = when (pinType) {
        PinCategory.ISSUE, PinCategory.COMMUNICATION, PinCategory.FESTIVAL -> PinDetailDisplayProfile(
            imageUrl = writer?.imageUrl?.takeIf { it.isNotBlank() },
            nickname = writer?.name?.takeIf { it.isNotBlank() } ?: "알 수 없음",
        )
        PinCategory.SHOP -> PinDetailDisplayProfile(
            imageUrl = storeImageUrl?.takeIf { it.isNotBlank() },
            nickname = pinTitle,
        )
    }
}

fun Pin.detailDisplayProfile(): PinDetailDisplayProfile? {
    return when (category) {
        PinCategory.ISSUE, PinCategory.COMMUNICATION -> {
            val user = author ?: (detail as? AuthoredPinDetail)?.writer ?: return null
            if (user.name.isBlank() && user.imageUrl.isNullOrBlank()) return null
            PinDetailDisplayProfile(
                imageUrl = user.imageUrl?.takeIf { it.isNotBlank() },
                nickname = user.name.takeIf { it.isNotBlank() } ?: "알 수 없음",
            )
        }
        PinCategory.FESTIVAL -> {
            val user = author ?: return null
            if (user.name.isBlank() && user.imageUrl.isNullOrBlank()) return null
            PinDetailDisplayProfile(
                imageUrl = user.imageUrl?.takeIf { it.isNotBlank() },
                nickname = user.name.takeIf { it.isNotBlank() } ?: "알 수 없음",
            )
        }
        PinCategory.SHOP -> PinDetailDisplayProfile(
            imageUrl = storeImageUrl?.takeIf { it.isNotBlank() },
            nickname = title,
        )
    }
}


fun Pin.toPostSympathyContent(): PinPostSympathyContent {
    val numericPinId = id.toLongOrNull() ?: 0L
    val writer = when (category) {
        PinCategory.ISSUE, PinCategory.COMMUNICATION -> when (val detail = detail) {
            is AuthoredPinDetail -> detail.writer
            else -> author
        }
        PinCategory.FESTIVAL -> author
        PinCategory.SHOP -> null
    }
    val storeImage = storeImageUrl?.takeIf { it.isNotBlank() }
        ?: if (category == PinCategory.SHOP) {
            imageUrls.lastOrNull()?.takeIf { it.isNotBlank() }
        } else {
            null
        }
    return PinPostSympathyContent(
        pinId = numericPinId,
        pinType = category,
        pinTitle = title,
        sympathyCount = sympathyCount,
        isSympathizedByMe = isSympathizedByMe,
        writer = writer,
        discount = (detail as? ShopPinDetail)?.currentNews?.takeIf { it.isNotBlank() },
        storeImageUrl = storeImage,
        mainPinImageUrl = mainPinImageUrl?.takeIf { it.isNotBlank() },
    )
}

/** POST API 값이 비어 있을 때 홈 데이터로 보완 (레이아웃은 홈 pinType 기준) */
fun PinPostSympathyContent.withHomeFallback(
    fallback: PinPostSympathyContent,
    layoutCategory: PinCategory,
): PinPostSympathyContent = copy(
    pinType = layoutCategory,
    pinTitle = pinTitle.ifBlank { fallback.pinTitle },
    discount = discount?.takeIf { it.isNotBlank() } ?: fallback.discount,
    storeImageUrl = storeImageUrl?.takeIf { it.isNotBlank() } ?: fallback.storeImageUrl,
    mainPinImageUrl = mainPinImageUrl?.takeIf { it.isNotBlank() } ?: fallback.mainPinImageUrl,
    writer = when (layoutCategory) {
        PinCategory.ISSUE, PinCategory.COMMUNICATION, PinCategory.FESTIVAL -> writer?.takeIf { w ->
            !w.imageUrl.isNullOrBlank() || w.name.isNotBlank()
        } ?: fallback.writer
        PinCategory.SHOP -> null
    },
)

sealed interface PinDetail {
    val category: PinCategory
}

// 새롭게 추가된 인터페이스
sealed interface AuthoredPinDetail : PinDetail {
    val writer: PinUser
}

// 시민해결사 한 명의 참여 단위.
// TODO: 서버 명세 확정 후 필드 타입(시간 포맷 등) 정합성 재검토 필요.
data class IssueResolverParticipation(
    val problemSolverId: Long? = null,
    val user: PinUser,
    val joinedAt: String,
    val proofImageUrls: List<String> = emptyList(),
    val proofSubmittedAt: String? = null,
    val isConfirmedByWriter: Boolean = false,
    val confirmedAt: String? = null
)

//해결하기
data class PinSolveInfo(
    val isPetitioned: Boolean,
    val userProblemSolverId: Long? = null,
    val userProblemSolveState: String? = null,
) {
    val isProblemSolver: Boolean
        get() = userProblemSolverId != null
}

data class ProblemSolverInfo(
    val isGoNow: Boolean,
    val problemSolvers: List<ProblemSolverParticipantInfo> = emptyList(),
)

data class ProblemSolverParticipantInfo(
    val problemSolverId: Long,
    val problemSolveState: String,
    val problemSolverImageUrl: String?,
    val nickname: String,
    val createdAt: String,
    val profileUrl: String?,
    val checkAction: String?,
)

data class ProblemSolverJoinInfo(
    val pinId: Long,
    val problemSolverId: Long,
    val problemSolveState: String,
)

data class ProblemSolverPhotoInfo(
    val photoId: Long,
    val photoUrl: String,
    val problemSolveState: String,
)

data class ProblemSolverVerificationInfo(
    val problemSolveState: String,
)

data class PetitionStatusInfo(
    val pinId: Long,
    val petitionCount: Int,
    val isPetitioned: Boolean,
    val targetPetitionCount: Int,
)

data class PetitionJoinInfo(
    val pinId: Long,
    val petitionCount: Int,
    val isPetitioned: Boolean,
)

data class IssuePinDetail(
    override val writer: PinUser,
    val resolutionStatus: ResolutionStatus = ResolutionStatus.BEFORE_RESOLUTION,
    val isProblemSolverByMe: Boolean = false,
    val myProblemSolverId: Long? = null,
    val myProblemSolveState: String? = null,
    // 지금가요를 누른 시민해결사 전체 목록.
    // TODO: 서버 응답 구조 확정 시 필요한 필드(예: 페이지네이션 등) 보강.
    val resolverParticipations: List<IssueResolverParticipation> = emptyList(),
    // 작성자가 최종 해결자로 인정한 유저 한 명. 서버의 RESOLVED 상태와 함께 신뢰.
    val resolvedBy: PinUser? = null,
    // TODO: resolverParticipations.proofImageUrls로 이전 검토 (서버 명세 확정 후 제거 후보).
    val resolutionProofImageUrls: List<String> = emptyList(),
    val resolvedAt: String? = null,
    val petitionCount: Int = 0,
    val isPetitionedByMe: Boolean = false,
    // 청원 progress bar에서 "민원 메일 조건까지 남은 수" 계산용.
    // TODO: 서버 정책값/응답 위치 확정 시 별도 config로 분리 검토.
    val petitionTargetCount: Int? = null
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
    val imageAttachments: List<PinImageRef> = emptyList(),
    val viewCount: Int = 0,
    val sympathyCount: Int = 0,
    val isSympathizedByMe: Boolean = false,
    val isMine: Boolean? = null,
    val emojiReactions: List<PinEmojiReaction> = emptyList(),
    val communityPostId: String? = null,
    val author: PinUser? = null,
    val storeImageUrl: String? = null,
    val mainPinImageUrl: String? = null,
    val isReported: Boolean = false,
    val isUpdated: Boolean = false,
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
    val imageUris: List<String> = emptyList(),
    val mainImageUri: String? = null,
    val imageUrls: List<String> = emptyList(),
    val tone: String = "없음",
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

data class UpdateIssuePinRequest(
    val title: String,
    val description: String,
    val existingImages: List<PinImageRef> = emptyList(),
    val newImageUris: List<String> = emptyList(),
    val mainNewImageUri: String? = null,
)

data class PinEditRateLimitQuota(
    val enabled: Boolean,
    val dailyLimit: Int,
    val usedCount: Int,
    val remainingCount: Int,
    val resetAt: String?,
)

data class IssuePinEditResult(
    val pin: Pin,
    val issuePinId: Long,
    val reliabilityStatus: String?,
    val imageUploadStatus: String?,
    val rateLimitQuota: PinEditRateLimitQuota?,
)

data class PinDetailHomeResult(
    val pin: Pin,
    val editRateLimitQuota: PinEditRateLimitQuota?,
)

fun Pin.canEditBy(userId: String? = null): Boolean {
    if (communityPostId != null) return false
    return isMine==true
}