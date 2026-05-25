package com.issueissyu.fe.ui.screens.community.detail

import com.issueissyu.fe.domain.model.community.CommunityComment
import com.issueissyu.fe.domain.model.community.CommunityDetail

data class CommunityDetailUiState(
    val isLoading: Boolean = false,
    val detail: CommunityDetail? = null,
    val comments: List<CommunityComment> = emptyList(),
    val isCommentLoading: Boolean = false,
    val isCommentSubmitting: Boolean = false,
    val isCommunityLikeSubmitting: Boolean = false,
    val isCommunityDeclarationSubmitting: Boolean = false,
    val errorMessage: String? = null
)
