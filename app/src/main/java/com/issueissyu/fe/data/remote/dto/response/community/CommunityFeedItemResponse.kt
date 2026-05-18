package com.issueissyu.fe.data.remote.dto.response.community

data class CommunityFeedItemResponse(
    val kind: String?,
    val communityId: Long?,
    val pinId: Long?,
    val title: String?,
    val pinTitle: String?,
    val content: String?,
    val discount: String?,
    val pinImageUrl: String?,
    val thumbnailUrl: String?,
    val storeImageUrl: String?,
    val pinUserNickname: String?,
    val pinUserProfile: String?,
    val pinDetailAddress: String?,
    val address: String?,
    val viewCount: Int?,
    val likeCount: Int?,
    val eventStartTime: String?,
    val eventEndTime: String?
)
