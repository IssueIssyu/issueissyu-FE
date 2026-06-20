package com.issueissyu.fe.domain.model.community

data class CommunityFeedItem(
    val communityId: Long,
    val pinId: Long?,
    val kind: CommunityItemKind,
    val title: String,
    val content: String?,
    val thumbnailUrl: String?,
    val writerNickname: String?,
    val writerProfileUrl: String?,
    val address: String?,
    val viewCount: Int,
    val likeCount: Int,
    val eventStartTime: String?,
    val eventEndTime: String?,
    val discount: String?,
    val isHot: Boolean
)
