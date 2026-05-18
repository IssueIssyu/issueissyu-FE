package com.issueissyu.fe.ui.screens.community.detail

import com.issueissyu.fe.domain.model.community.CommunityDetail

data class CommunityDetailUiState(
    val isLoading: Boolean = false,
    val detail: CommunityDetail? = null,
    val errorMessage: String? = null
)
