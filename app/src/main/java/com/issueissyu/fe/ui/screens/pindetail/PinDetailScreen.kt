package com.issueissyu.fe.ui.screens.pindetail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.data.sample.PinSamples
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.toPostSympathyContent
import com.issueissyu.fe.ui.components.EmojiReactionBottomSheet
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Orange
import com.issueissyu.fe.ui.theme.Title

internal const val PIN_DETAIL_REFRESH_KEY = "pin_detail_refresh"

@Composable
fun PinDetailScreen(
    pinId: String,
    onBackClick: () -> Unit,
    onReportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    savedStateHandle: SavedStateHandle? = null,
    viewModel: PinDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val emojiPickerUiState by viewModel.emojiPickerUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(pinId) {
        viewModel.loadPin(pinId)
    }

    LaunchedEffect(savedStateHandle, pinId) {
        val handle = savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow(PIN_DETAIL_REFRESH_KEY, false).collect { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.loadPin(pinId)
                handle[PIN_DETAIL_REFRESH_KEY] = false
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PinDetailEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        IssueissyuTopAppBar(
            onBackClick = onBackClick,
            titleText = "",
        )

        when {
            uiState.isLoading -> {
                CenteredPlaceholder(
                    modifier = Modifier.weight(1f),
                    text = "핀 정보를 불러오는 중...",
                )
            }

            uiState.errorMessage != null -> {
                CenteredPlaceholder(
                    modifier = Modifier.weight(1f),
                    text = uiState.errorMessage.orEmpty(),
                )
            }

            uiState.pin != null -> {
                Box(modifier = Modifier.weight(1f)) {
                    PinDetailContent(
                        pin = uiState.pin!!,
                        uiState = uiState,
                        onSelectTab = viewModel::selectTab,
                        onSympathyClick = viewModel::toggleSympathy,
                        onEmojiClick = viewModel::openEmojiPicker,
                        onEmojiChipClick = viewModel::toggleEmojiFromList,
                        onCommentSubmit = viewModel::submitComment,
                        onCommentEdit = viewModel::startEditComment,
                        onCommentEditCancel = viewModel::cancelEditComment,
                        onCommentDelete = viewModel::deleteComment,
                        onReportClick = onReportClick,
                        onEditClick = { /* TODO: 핀 수정 화면 */ },
                        onDeleteClick = { deletePinId ->
                            viewModel.deletePin(deletePinId, onSuccess = onBackClick)
                        },
                        onCommunityClick = { /* TODO: 커뮤니티 상세 */ },
                        modifier = Modifier.fillMaxSize(),
                    )

                    if (emojiPickerUiState.isVisible) {
                        EmojiReactionBottomSheet(
                            candidates = emojiPickerUiState.candidates,
                            selectedEmojiId = emojiPickerUiState.pickedEmojiId,
                            isLoading = emojiPickerUiState.isLoading,
                            isSubmitting = emojiPickerUiState.isSubmitting,
                            errorMessage = emojiPickerUiState.errorMessage,
                            onDismiss = {
                                if (!emojiPickerUiState.isSubmitting) {
                                    viewModel.closeEmojiPicker()
                                }
                            },
                            onEmojiClick = viewModel::pickEmojiInPicker,
                            onApplyClick = viewModel::submitPickedEmoji,
                            allowApplyWithoutSelection = true,
                        )
                    }
                }
            }

            else -> {
                CenteredPlaceholder(
                    modifier = Modifier.weight(1f),
                    text = "핀 정보를 불러올 수 없습니다.",
                )
            }
        }
    }
}

@Composable
private fun PinDetailContent(
    pin: Pin,
    uiState: PinDetailUiState,
    onSelectTab: (PinDetailTab) -> Unit,
    onSympathyClick: () -> Unit,
    onEmojiClick: () -> Unit,
    onEmojiChipClick: (Long) -> Unit,
    onCommentSubmit: (String) -> Unit,
    onCommentEdit: (Long) -> Unit,
    onCommentEditCancel: () -> Unit,
    onCommentDelete: (Long) -> Unit,
    onReportClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onCommunityClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = buildList {
        add(PinDetailTab.HOME)
        add(PinDetailTab.POST)
        if (pin.detail is IssuePinDetail) add(PinDetailTab.RESOLUTION)
    }
    val effectiveTab = tabs.getOrElse(tabs.indexOf(uiState.selectedTab).coerceAtLeast(0)) {
        PinDetailTab.HOME
    }

    Column(modifier = modifier.fillMaxSize()) {
        PinDetailTabBar(
            tabs = tabs,
            selectedTab = effectiveTab,
            onSelectTab = onSelectTab,
        )

        when (effectiveTab) {
            PinDetailTab.HOME -> PinHomeTab(
                pin = pin,
                onReportClick = onReportClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onCommunityClick = onCommunityClick,
                isDeleting = uiState.isDeleting,
                modifier = Modifier.fillMaxSize(),
            )

            PinDetailTab.POST -> {
                val postSympathy = uiState.postSympathy ?: pin.toPostSympathyContent()
                PinPostTab(
                    sympathy = postSympathy,
                    postEmojis = uiState.postEmojis,
                    comments = uiState.postComments,
                    isCommentsLoading = uiState.isCommentsLoading,
                    isCommentSubmitting = uiState.isCommentSubmitting,
                    commentInputRevision = uiState.commentInputRevision,
                    editingCommentId = uiState.editingCommentId,
                    onSympathyClick = onSympathyClick,
                    onEmojiClick = onEmojiClick,
                    onEmojiChipClick = onEmojiChipClick,
                    onCommentSubmit = onCommentSubmit,
                    onCommentEdit = onCommentEdit,
                    onCommentEditCancel = onCommentEditCancel,
                    onCommentDelete = onCommentDelete,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            PinDetailTab.RESOLUTION -> {
                val issueDetail = pin.detail as? IssuePinDetail
                if (issueDetail != null) {
                    PinResolutionTab(
                        pin = pin,
                        issueDetail = issueDetail,
                        currentUserId = "user1_id",
                        onGoNowClick = { /* TODO */ },
                        onPetitionClick = { /* TODO */ },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    CenteredPlaceholder(
                        modifier = Modifier.fillMaxSize(),
                        text = "해결하기는 이슈 핀에서만 사용할 수 있습니다.",
                    )
                }
            }
        }
    }
}

@Composable
private fun PinDetailTabBar(
    tabs: List<PinDetailTab>,
    selectedTab: PinDetailTab,
    onSelectTab: (PinDetailTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEach { tab ->
                PinDetailTabItem(
                    label = tab.label(),
                    selected = selectedTab == tab,
                    onClick = { onSelectTab(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        HorizontalDivider(color = Gray_3, thickness = 1.dp)
    }
}

@Composable
private fun PinDetailTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textStyle = if (selected) {
        IssueTypo.Regular15.copy(color = Title, fontWeight = FontWeight.Bold)
    } else {
        IssueTypo.Regular15.copy(color = Gray_5)
    }

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = label, style = textStyle)
        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth(0.5f)
                .background(if (selected) Orange else Color.Transparent),
        )
    }
}

@Composable
private fun CenteredPlaceholder(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text)
    }
}

private fun PinDetailTab.label(): String = when (this) {
    PinDetailTab.HOME -> "홈"
    PinDetailTab.POST -> "포스트"
    PinDetailTab.RESOLUTION -> "해결하기"
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PinDetailScreenPreview() {
    IssueissyuTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            IssueissyuTopAppBar(onBackClick = {}, titleText = "")
            PinHomeTab(
                pin = PinSamples.findById(PinSamples.IssueInProgressPinId),
                onReportClick = {},
                onEditClick = {},
                onDeleteClick = {},
                onCommunityClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
