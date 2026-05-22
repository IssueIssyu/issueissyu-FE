package com.issueissyu.fe.ui.screens.community

import com.issueissyu.fe.domain.model.community.CommunityFeedItem
import com.issueissyu.fe.domain.model.community.CommunityTab
import com.issueissyu.fe.domain.model.LocationRegionGroup

data class CommunityUiState(
    val selectedTab: CommunityTab = CommunityTab.ALL,
    val region: String = "",
    val isRegionSelectedByUser: Boolean = false,
    val regionGroups: List<LocationRegionGroup> = emptyList(),
    val isRegionLoading: Boolean = false,
    val regionError: String? = null,
    val feedItems: List<CommunityFeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val nextCursor: String? = null,
    val hasNext: Boolean = false,
    val error: String? = null
)
