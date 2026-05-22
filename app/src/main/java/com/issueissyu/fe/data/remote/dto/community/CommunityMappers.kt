package com.issueissyu.fe.data.remote.dto.community

import com.issueissyu.fe.data.remote.dto.response.community.CommunityDetailResponse
import com.issueissyu.fe.data.remote.dto.response.community.CommunityFeedItemResponse
import com.issueissyu.fe.data.remote.dto.response.community.CommunityFeedResponse
import com.issueissyu.fe.domain.model.community.CommunityDetail
import com.issueissyu.fe.domain.model.community.CommunityFeed
import com.issueissyu.fe.domain.model.community.CommunityFeedItem
import com.issueissyu.fe.domain.model.community.CommunityItemKind

private const val DEFAULT_COMMUNITY_TITLE = "제목 없음"

@Suppress("unused") // TODO: 실제 CommunityApi 연결 시 사용 예정
fun CommunityFeedResponse.toCommunityFeed(): CommunityFeed {
    return CommunityFeed(
        items = this.content?.map { it.toCommunityFeedItem() } ?: emptyList(),
        region = this.region ?: "",
        nextCursor = this.nextCursor,
        hasNext = this.hasNext ?: false
    )
}

fun CommunityDetailResponse.toCommunityDetail(): CommunityDetail {
    return CommunityDetail(
        communityId = communityId ?: 0L,
        pinId = pinId,
        kind = kind.toCommunityItemKind(),
        title = title ?: DEFAULT_COMMUNITY_TITLE,
        content = this.content ?: "",
        imageUrls = imageUrls.orEmpty().ifEmpty { listOfNotNull(thumbnailUrl) },
        writerNickname = writerNickname,
        writerProfileUrl = writerProfileUrl,
        address = detailAddress,
        viewCount = viewCount ?: 0,
        likeCount = likeCount?.toInt() ?: 0,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isReported = this.isReported ?: false,
        isPetitioned = false,
        isProblemSolver = this.isProblemSolver ?: false,
        isMine = this.isMine ?: false,
        reliabilityScore = null,
        reliabilityReason = null,
        issueStatusText = issuePinState.toIssueStatusText(),
        discount = this.discount,
        eventStartTime = this.eventStartTime,
        eventEndTime = this.eventEndTime,
        petitionCount = 0,
        petitionTargetCount = null,
        isPetitionedByMe = false
    )
}

@Suppress("unused") // TODO: 실제 CommunityApi 연결 시 사용 예정
fun CommunityFeedItemResponse.toCommunityFeedItem(): CommunityFeedItem {
    return CommunityFeedItem(
        communityId = this.communityId ?: 0L,
        pinId = this.pinId,
        kind = this.kind.toCommunityItemKind(),
        title = this.title ?: this.pinTitle ?: DEFAULT_COMMUNITY_TITLE,
        content = this.content,
        thumbnailUrl = this.pinImageUrl ?: this.thumbnailUrl ?: this.storeImageUrl,
        writerNickname = this.pinUserNickname,
        writerProfileUrl = this.pinUserProfile,
        address = this.pinDetailAddress ?: this.address,
        viewCount = this.viewCount ?: 0,
        likeCount = this.likeCount ?: 0,
        eventStartTime = this.eventStartTime,
        eventEndTime = this.eventEndTime,
        discount = this.discount,
        isHot = false
    )
}

private fun String?.toCommunityItemKind(): CommunityItemKind {
    return when (this?.trim()?.uppercase()) {
        "ISSUE" -> CommunityItemKind.ISSUE
        "COMMUNICATION" -> CommunityItemKind.COMMUNICATION
        "STORE", "SHOP" -> CommunityItemKind.STORE
        "FESTIVAL" -> CommunityItemKind.FESTIVAL
        "POLICY" -> CommunityItemKind.POLICY
        "CONTEST" -> CommunityItemKind.CONTEST
        "CARDNEWS", "CARD_NEWS" -> CommunityItemKind.CARDNEWS
        else -> CommunityItemKind.UNKNOWN
    }
}

private fun String?.toIssueStatusText(): String? {
    return when (this?.trim()?.uppercase()) {
        "BEFORE_RESOLUTION", "BEFORE", "READY" -> "해결 전"
        "IN_PROGRESS", "PROGRESS", "RESOLVING" -> "진행중"
        "RESOLVED", "DONE" -> "해결 완료"
        null, "" -> null
        else -> this
    }
}
