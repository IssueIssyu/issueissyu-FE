package com.issueissyu.fe.data.remote.dto.response.community

data class CommunityFeedResponse(
    val region: String?,
    val content: List<CommunityFeedItemResponse>?,
    val nextCursor: String?,
    val hasNext: Boolean?
)
