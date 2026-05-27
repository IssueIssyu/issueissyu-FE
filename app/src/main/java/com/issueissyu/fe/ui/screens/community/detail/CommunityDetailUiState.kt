package com.issueissyu.fe.ui.screens.community.detail

import com.issueissyu.fe.domain.model.community.CommunityComment
import com.issueissyu.fe.domain.model.community.CommunityDetail
import com.issueissyu.fe.domain.model.issue.IssueReliabilityStatus
import com.issueissyu.fe.domain.model.pin.PinEmojiCandidate
import com.issueissyu.fe.domain.model.pin.PinEmojiReaction

data class CommunityDetailUiState(
    val isLoading: Boolean = false,
    val detail: CommunityDetail? = null,
    val comments: List<CommunityComment> = emptyList(),
    val emojiReactions: List<PinEmojiReaction> = emptyList(),
    val emojiPicker: CommunityEmojiPickerUiState = CommunityEmojiPickerUiState(),
    val isCommentLoading: Boolean = false,
    val isCommentSubmitting: Boolean = false,
    val deletingCommentIds: Set<Long> = emptySet(),
    val isCommunityLikeSubmitting: Boolean = false,
    val isCommunityDeleting: Boolean = false,
    val reliabilityStatus: IssueReliabilityStatus = IssueReliabilityStatus.PENDING,
    val errorMessage: String? = null
)

data class CommunityEmojiPickerUiState(
    val targetPinId: Long? = null,
    val candidates: List<PinEmojiCandidate> = emptyList(),
    val selectedEmojiId: Long? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val isVisible: Boolean
        get() = targetPinId != null
}
