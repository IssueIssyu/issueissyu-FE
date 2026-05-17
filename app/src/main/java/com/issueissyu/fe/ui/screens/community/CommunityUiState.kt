package com.issueissyu.fe.ui.screens.community

import com.issueissyu.fe.domain.model.community.CommunityFeedItem
import com.issueissyu.fe.domain.model.community.CommunityTab

data class CommunityUiState(
    val selectedTab: CommunityTab = CommunityTab.HOT, // 피그마 기준 HOT이 첫 번째
    val region: String = "마포구", // TODO: 실제 사용자 지역 연동 필요
    val feedItems: List<CommunityFeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val nextCursor: String? = null,
    val hasNext: Boolean = false,
    val error: String? = null
)
