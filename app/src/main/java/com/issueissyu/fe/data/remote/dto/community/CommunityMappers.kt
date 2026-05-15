package com.issueissyu.fe.data.remote.dto.community

import com.issueissyu.fe.domain.model.community.CommunityFeed
import com.issueissyu.fe.domain.model.community.CommunityFeedItem
import com.issueissyu.fe.domain.model.community.CommunityItemKind

fun CommunityFeedResponse.toCommunityFeed(): CommunityFeed {
    return CommunityFeed(
        items = this.content?.map { it.toCommunityFeedItem() } ?: emptyList(),
        region = this.region ?: "",
        nextCursor = this.nextCursor,
        hasNext = this.hasNext ?: false
    )
}

fun CommunityFeedItemResponse.toCommunityFeedItem(): CommunityFeedItem {
    return CommunityFeedItem(
        communityId = this.communityId ?: 0L, // TODO: communityId null 처리 정책 확정 필요
        pinId = this.pinId,
        kind = this.kind.toCommunityItemKind(),
        title = this.title ?: this.pinTitle ?: "제목 없음",
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
        isHot = false // TODO: isHot 필드 서버 응답 확정 시 처리 필요 (현재 false로 fallback)
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
