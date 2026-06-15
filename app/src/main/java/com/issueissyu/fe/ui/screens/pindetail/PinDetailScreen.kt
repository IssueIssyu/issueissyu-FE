package com.issueissyu.fe.ui.screens.pindetail

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.issueissyu.fe.data.sample.PinSamples
import com.issueissyu.fe.core.extensions.findActivity
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinImageRef
import com.issueissyu.fe.domain.model.pin.PinEditRateLimitQuota
import com.issueissyu.fe.domain.model.pin.toPostSympathyContent
import com.issueissyu.fe.core.media.rememberPhotoSourcePicker
import com.issueissyu.fe.core.constants.PinImageUploadConstraints
import com.issueissyu.fe.ui.components.EmojiReactionBottomSheet
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.components.Dialog as IssueDialog
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Orange
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
internal const val PIN_DETAIL_REFRESH_KEY = "pin_detail_refresh"

@Composable
fun PinDetailScreen(
    pinId: String,
    onBackClick: () -> Unit,
    onReportClick: (String) -> Unit,
    onCommunityClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    savedStateHandle: SavedStateHandle? = null,
    viewModel: PinDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val emojiPickerUiState by viewModel.emojiPickerUiState.collectAsStateWithLifecycle()
    val currentUserId = viewModel.currentUserId.orEmpty()
    val context = LocalContext.current
    val activity = context.findActivity()
    var pendingResolutionProofUri by remember { mutableStateOf<String?>(null) }

    val resolutionProofPhotoPicker = rememberPhotoSourcePicker(
        currentCount = 0,
        maxCount = 1,
        onImagesPicked = { uris ->
            uris.firstOrNull()?.let { pendingResolutionProofUri = it }
        },
        cameraFileNamePrefix = "resolution-proof",
    )
    resolutionProofPhotoPicker.PhotoSourceBottomSheet()

    val homeEditPhotoCount = if (uiState.isHomeEditing) {
        uiState.homeEditExistingImages.size + uiState.homeEditNewImageUris.size
    } else {
        0
    }
    val homeEditPhotoPicker = rememberPhotoSourcePicker(
        currentCount = homeEditPhotoCount,
        maxCount = PinImageUploadConstraints.MAX_COUNT,
        onImagesPicked = viewModel::addHomeEditImageUris,
        cameraFileNamePrefix = "pin-edit",
    )
    homeEditPhotoPicker.PhotoSourceBottomSheet()

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
            .background(White),
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
                        currentUserId = currentUserId,
                        onSelectTab = viewModel::selectTab,
                        onSympathyClick = viewModel::toggleSympathy,
                        onEmojiClick = viewModel::openEmojiPicker,
                        onEmojiChipClick = viewModel::toggleEmojiFromList,
                        onCommentSubmit = viewModel::submitComment,
                        onCommentEdit = viewModel::startEditComment,
                        onCommentEditCancel = viewModel::cancelEditComment,
                        onCommentDelete = viewModel::deleteComment,
                        onReportClick = onReportClick,
                        onEditClick = { viewModel.startHomeEdit() },
                        onDeleteClick = { deletePinId ->
                            viewModel.deletePin(deletePinId, onSuccess = onBackClick)
                        },
                        onCommunityClick = { communityId ->
                            communityId.toLongOrNull()?.let(onCommunityClick)
                        },
                        onAttachProofClick = resolutionProofPhotoPicker.showSourceSheet,
                        onGoNowClick = viewModel::joinProblemSolver,
                        onPetitionClick = viewModel::joinPetition,
                        onConfirmResolverClick = viewModel::verifyProblemSolver,
                        isHomeEditing = uiState.isHomeEditing,
                        homeEditTitle = uiState.homeEditTitle,
                        homeEditDescription = uiState.homeEditDescription,
                        homeEditExistingImages = uiState.homeEditExistingImages,
                        homeEditNewImageUris = uiState.homeEditNewImageUris,
                        homeEditMainImageKey = uiState.homeEditMainImageKey,
                        isSubmittingHomeEdit = uiState.isSubmittingHomeEdit,
                        homeEditSubmitFailed = uiState.homeEditSubmitFailed,
                        onHomeEditTitleChange = viewModel::onHomeEditTitleChange,
                        onHomeEditDescriptionChange = viewModel::onHomeEditDescriptionChange,
                        onHomeEditCancel = viewModel::cancelHomeEdit,
                        onHomeEditSubmit = viewModel::requestHomeEditSubmit,
                        onHomeEditPhotoAddClick = homeEditPhotoPicker.showSourceSheet,
                        onHomeEditExistingImageRemove = viewModel::removeHomeEditExistingImage,
                        onHomeEditNewImageRemove = viewModel::removeHomeEditNewImageUri,
                        onHomeEditMainImageSelect = viewModel::setHomeEditMainImage,
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
                            onLockedEmojiClick = { emojiId -> viewModel.purchaseEmoji(activity, emojiId) },
                            onApplyClick = viewModel::submitPickedEmoji,
                            allowApplyWithoutSelection = true,
                        )
                    }

                    pendingResolutionProofUri?.let { imageUri ->
                        ResolutionProofConfirmDialog(
                            imageUri = imageUri,
                            isSubmitting = uiState.isResolutionProofSubmitting,
                            onDismiss = { pendingResolutionProofUri = null },
                            onConfirm = {
                                viewModel.submitProblemSolverPhoto(
                                    imageUri = imageUri,
                                    onSuccess = { pendingResolutionProofUri = null }
                                )
                            }
                        )
                    }

                    if (uiState.showHomeEditConfirmDialog) {
                        IssueDialog(
                            title = "이슈 핀 수정",
                            message = uiState.homeEditRateLimitQuota.toHomeEditConfirmMessage(),
                            confirmText = "수정",
                            dismissText = "취소",
                            isWarning = true,
                            onDismiss = viewModel::dismissHomeEditConfirmDialog,
                            onConfirm = viewModel::confirmHomeEditSubmit,
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
    currentUserId: String,
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
    onAttachProofClick: () -> Unit,
    onGoNowClick: () -> Unit,
    onPetitionClick: () -> Unit,
    onConfirmResolverClick: (Long) -> Unit,
    isHomeEditing: Boolean = false,
    homeEditTitle: String = "",
    homeEditDescription: String = "",
    homeEditExistingImages: List<PinImageRef> = emptyList(),
    homeEditNewImageUris: List<String> = emptyList(),
    homeEditMainImageKey: String? = null,
    isSubmittingHomeEdit: Boolean = false,
    homeEditSubmitFailed: Boolean = false,
    onHomeEditTitleChange: (String) -> Unit = {},
    onHomeEditDescriptionChange: (String) -> Unit = {},
    onHomeEditCancel: () -> Unit = {},
    onHomeEditSubmit: () -> Unit = {},
    onHomeEditPhotoAddClick: () -> Unit = {},
    onHomeEditExistingImageRemove: (String) -> Unit = {},
    onHomeEditNewImageRemove: (String) -> Unit = {},
    onHomeEditMainImageSelect: (String) -> Unit = {},
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
            enabled = !isHomeEditing,
        )

        when (effectiveTab) {
            PinDetailTab.HOME -> PinHomeTab(
                pin = pin,
                onReportClick = onReportClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onCommunityClick = onCommunityClick,
                isDeleting = uiState.isDeleting,
                isEditing = isHomeEditing,
                editTitle = homeEditTitle,
                editDescription = homeEditDescription,
                editExistingImages = homeEditExistingImages,
                editNewImageUris = homeEditNewImageUris,
                editMainImageKey = homeEditMainImageKey,
                isSubmittingEdit = isSubmittingHomeEdit,
                showEditCancel = !homeEditSubmitFailed,
                onEditTitleChange = onHomeEditTitleChange,
                onEditDescriptionChange = onHomeEditDescriptionChange,
                onEditCancelClick = onHomeEditCancel,
                onEditSubmitClick = onHomeEditSubmit,
                onEditPhotoAddClick = onHomeEditPhotoAddClick,
                onEditExistingImageRemove = onHomeEditExistingImageRemove,
                onEditNewImageRemove = onHomeEditNewImageRemove,
                onEditMainImageSelect = onHomeEditMainImageSelect,
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
                        currentUserId = currentUserId,
                        onGoNowClick = { onGoNowClick() },
                        onPetitionClick = { onPetitionClick() },
                        onConfirmResolverClick = onConfirmResolverClick,
                        onAttachProofClick = onAttachProofClick,
                        reliabilityScore = uiState.reliabilityScore,
                        reliabilityReason = uiState.reliabilityReason,
                        reliabilityStatus = uiState.reliabilityStatus,
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
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = remember(tabs, selectedTab) {
        tabs.indexOf(selectedTab).coerceAtLeast(0)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEach { tab ->
                PinDetailTabItem(
                    label = tab.label(),
                    selected = selectedTab == tab,
                    onClick = { if (enabled) onSelectTab(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Gray_3),
            )
            val tabWidth = maxWidth / tabs.size
            val indicatorWidth = tabWidth * 0.5f
            val targetOffset = tabWidth * selectedIndex + (tabWidth - indicatorWidth) / 2
            val indicatorOffset by animateDpAsState(
                targetValue = targetOffset,
                label = "pinDetailTabIndicator",
            )
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(indicatorWidth)
                    .fillMaxHeight()
                    .background(Orange),
            )
        }
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

    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = textStyle)
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

@Composable
private fun ResolutionProofConfirmDialog(
    imageUri: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(White)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "사진 첨부 확인",
                    style = IssueTypo.Bold18.copy(color = BrandColor)
                )
                AsyncImage(
                    model = imageUri,
                    contentDescription = "촬영한 인증 사진",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Gray_3, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                )
                Text(
                    text = "이 사진으로 현장 인증을 진행할까요?",
                    style = IssueTypo.Regular15.copy(color = Gray_5)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gray_3,
                            contentColor = Gray_5,
                            disabledContainerColor = Gray_3,
                            disabledContentColor = Gray_5
                        )
                    ) {
                        Text(
                            text = "다시 찍기",
                            style = IssueTypo.Bold18.copy(color = Gray_5)
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandColor,
                            contentColor = White,
                            disabledContainerColor = BrandColor.copy(alpha = 0.5f),
                            disabledContentColor = White
                        )
                    ) {
                        Text(
                            text = if (isSubmitting) "저장 중..." else "첨부 확인",
                            style = IssueTypo.Bold18.copy(color = White)
                        )
                    }
                }
            }
        }
    }
}

private fun PinDetailTab.label(): String = when (this) {
    PinDetailTab.HOME -> "홈"
    PinDetailTab.POST -> "포스트"
    PinDetailTab.RESOLUTION -> "해결하기"
}

private fun PinEditRateLimitQuota?.toHomeEditConfirmMessage(): String {
    if (this == null) {
        return buildString {
            appendLine("이슈 핀은 하루에 제한된 횟수만 수정할 수 있습니다.")
            appendLine()
            append("이대로 수정하시겠습니까?")
        }
    }
    return buildString {
        appendLine("하루 최대 ${dailyLimit}회까지 수정할 수 있습니다.")
        appendLine("오늘 남은 수정 횟수는 ${remainingCount}회입니다.")
        appendLine()
        append("이대로 수정하시겠습니까?")
    }
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
