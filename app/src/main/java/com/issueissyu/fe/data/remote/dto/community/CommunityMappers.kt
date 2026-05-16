package com.issueissyu.fe.data.remote.dto.community

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

@Suppress("unused") // TODO: 실제 CommunityApi 연결 시 사용 예정
fun CommunityDetailResponse.toCommunityDetail(): CommunityDetail {
    val item = this.item
    return CommunityDetail(
        communityId = item?.communityId ?: 0L,
        pinId = item?.pinId,
        kind = item?.kind.toCommunityItemKind(),
        title = item?.title ?: item?.pinTitle ?: DEFAULT_COMMUNITY_TITLE,
        content = this.content ?: "",
        imageUrls = if (!this.pinImageUrls.isNullOrEmpty()) {
            this.pinImageUrls
        } else {
            listOfNotNull(item?.pinImageUrl ?: item?.thumbnailUrl ?: item?.storeImageUrl)
        },
        writerNickname = item?.pinUserNickname,
        writerProfileUrl = item?.pinUserProfile,
        address = item?.pinDetailAddress ?: item?.address,
        viewCount = item?.viewCount ?: 0,
        likeCount = item?.likeCount ?: 0,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isReported = this.isReported ?: false,
        isPetitioned = this.isPetitioned ?: false,
        isProblemSolver = this.isProblemSolver ?: false,
        isMine = this.isMine ?: false,
        reliabilityScore = this.reliabilityScore,
        reliabilityReason = this.reliabilityReason,
        discount = this.discount ?: item?.discount,
        eventStartTime = this.eventStartTime ?: item?.eventStartTime,
        eventEndTime = this.eventEndTime ?: item?.eventEndTime,
        petitionCount = this.petitionCount ?: 0,
        petitionTargetCount = this.petitionTargetCount,
        isPetitionedByMe = this.isPetitioned ?: false
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
