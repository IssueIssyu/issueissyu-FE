package com.issueissyu.fe.data.remote.dto.response

data class CommunityDetailResponse(
    val item: CommunityFeedItemResponse?,
    val content: String?,
    val pinImageUrls: List<String>?,
    val createdAt: String?,
    val updatedAt: String?,
    val isReported: Boolean?,
    val isPetitioned: Boolean?,
    val isProblemSolver: Boolean?,
    val isMine: Boolean?,

    // TODO: 백엔드 상세 응답 확정 후 필드명 검증 필요
    val reliabilityScore: Int?,
    val reliabilityReason: String?,

    // TODO: STORE/FESTIVAL/CONTEST 상세 응답 필드 확정 후 검증 필요
    val discount: String?,
    val eventStartTime: String?,
    val eventEndTime: String?,

    // TODO: ISSUE 청원 정보 응답 필드 확정 후 검증 필요
    val petitionCount: Int?,
    val petitionTargetCount: Int?
)
