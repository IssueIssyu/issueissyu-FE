package com.issueissyu.fe.data.remote.dto.response.community

data class CommunityFeedResponse(
    val region: String?,
    val content: List<CommunityFeedItemResponse>?,
    val items: List<CommunityFeedItemResponse>? = null,
    val nextCursor: String?,
    val hasNext: Boolean?,
    val storePromotions: List<CommunityFeedItemResponse>? = null,
    val hotPreviews: List<CommunityFeedItemResponse>? = null,
    val recentNews: CommunityFeedPageResponse? = null,
)

data class CommunityFeedPageResponse(
    val region: String?,
    val content: List<CommunityFeedItemResponse>?,
    val nextCursor: String?,
    val hasNext: Boolean?,
)
