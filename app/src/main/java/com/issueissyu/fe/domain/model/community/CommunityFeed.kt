package com.issueissyu.fe.domain.model.community

data class CommunityFeed(
    val items: List<CommunityFeedItem>,
    val storePromotions: List<CommunityFeedItem> = emptyList(),
    val hotPreviews: List<CommunityFeedItem> = emptyList(),
    val recentNews: List<CommunityFeedItem> = items,
    val region: String,
    val nextCursor: String?,
    val hasNext: Boolean
)
