package com.issueissyu.fe.domain.model.community

data class CommunityFeed(
    val items: List<CommunityFeedItem>,
    val region: String,
    val nextCursor: String?,
    val hasNext: Boolean
)
