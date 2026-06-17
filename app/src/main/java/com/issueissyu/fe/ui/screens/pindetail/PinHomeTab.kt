package com.issueissyu.fe.ui.screens.pindetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.issueissyu.fe.R
import com.issueissyu.fe.core.constants.PinImageUploadConstraints
import com.issueissyu.fe.core.time.formatPinHomeCreatedAt
import com.issueissyu.fe.data.sample.PinSamples
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinImageRef
import com.issueissyu.fe.domain.model.pin.detailDisplayProfile
import com.issueissyu.fe.domain.model.pin.ResolutionStatus
import com.issueissyu.fe.domain.model.pin.ShopPinDetail
import com.issueissyu.fe.domain.model.pin.canEditBy
import com.issueissyu.fe.ui.components.CommonTextField
import com.issueissyu.fe.ui.components.DotPagerIndicator
import com.issueissyu.fe.ui.components.IssueAiDraftButton
import com.issueissyu.fe.ui.components.IssueToneSelectionSection
import com.issueissyu.fe.ui.components.ProfileImageFrame
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_1
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Orange
import com.issueissyu.fe.ui.theme.Text as TextColor
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White

@Composable
fun PinHomeTab(
    pin: Pin,
    currentUserId: String? = null,
    homeEdit: PinHomeEditUiState = PinHomeEditUiState(),
    homeEditCallbacks: PinHomeEditCallbacks = PinHomeEditCallbacks(),
    onReportClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onCommunityClick: (String) -> Unit,
    isDeleting: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val displayProfile = pin.detailDisplayProfile()
    val canEdit = pin.canEditBy(currentUserId)
    val issueDetail = pin.detail as? IssuePinDetail
    val shopDetail = pin.detail as? ShopPinDetail
    val isEditing = homeEdit.isActive

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp)) {
            if (!isEditing && issueDetail != null) {
                ResolutionStatusBadge(
                    resolutionStatus = issueDetail.resolutionStatus,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                if (isEditing) {
                    CommonTextField(
                        value = homeEdit.title,
                        onValueChange = homeEditCallbacks.onTitleChange,
                        placeholder = "제목을 입력하세요.",
                        maxLength = 50,
                        textStyle = IssueTypo.Bold18,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Text(
                        text = pin.title,
                        style = IssueTypo.Bold18.copy(color = Title),
                        fontSize = 22.sp,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                if (isEditing) {
                    PinHomeEditActionButtons(
                        isSubmitting = homeEdit.isSubmittingHomeEdit,
                        onCancelClick = homeEditCallbacks.onCancel,
                        onSubmitClick = homeEditCallbacks.onSubmit,
                    )
                } else {
                    PinDetailActionButtons(
                        isAuthor = pin.isMine == true,
                        canEdit = canEdit,
                        isReported = pin.isReported,
                        isDeleting = isDeleting,
                        onReportClick = { onReportClick(pin.id) },
                        onEditClick = { onEditClick(pin.id) },
                        onDeleteClick = { onDeleteClick(pin.id) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pin.locationName?.takeIf { it.isNotBlank() } ?: pin.address,
                style = IssueTypo.Regular15.copy(color = Gray_7),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = listOf(
                    formatPinHomeCreatedAt(pin.createdAt),
                    "조회 ${pin.viewCount}",
                    "공감 ${pin.sympathyCount}",
                ).joinToString(" · "),
                style = IssueTypo.Regular12.copy(color = Gray_6),
            )

            if (!isEditing && (displayProfile != null || pin.communityPostId != null)) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (displayProfile != null) {
                        ProfileImageFrame(
                            size = 35.dp,
                            imageUrl = displayProfile.imageUrl,
                            contentDescription = "핀 프로필"
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = displayProfile.nickname,
                            style = IssueTypo.Bold12.copy(color = Title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (pin.communityPostId != null) {
                        CommunityChip(onClick = { onCommunityClick(pin.communityPostId) })
                    }
                }
            } else if (isEditing && displayProfile != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileImageFrame(
                        size = 35.dp,
                        imageUrl = displayProfile.imageUrl,
                        contentDescription = "핀 프로필"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = displayProfile.nickname,
                        style = IssueTypo.Bold12.copy(color = Title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        HorizontalDivider(color = Gray_3, thickness = 1.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 16.dp),
        ) {
            if (!isEditing) {
                shopDetail?.currentNews?.takeIf { it.isNotBlank() }?.let { discount ->
                    PinHomeDiscountBanner(text = discount)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = pin.description,
                    style = IssueTypo.Regular15.copy(color = TextColor),
                    lineHeight = 22.sp,
                )

                if (pin.imageUrls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    PinDetailImageSection(imageUrls = pin.imageUrls)
                }
            } else {
                PinHomeEditSectionLabel(
                    text = "사진",
                    trailing = {
                        Text(
                            text = "${homeEdit.existingImages.size + homeEdit.newImageUris.size}/${PinImageUploadConstraints.MAX_COUNT}",
                            style = IssueTypo.Regular12.copy(color = Gray_6),
                        )
                    },
                )

                PinHomeEditPhotoSection(
                    existingImages = homeEdit.existingImages,
                    newImageUris = homeEdit.newImageUris,
                    mainImageKey = homeEdit.mainImageKey,
                    onPhotoAddClick = homeEditCallbacks.onPhotoAddClick,
                    onExistingImageRemove = homeEditCallbacks.onExistingImageRemove,
                    onNewImageRemove = homeEditCallbacks.onNewImageRemove,
                    onSetMainImage = homeEditCallbacks.onMainImageSelect,
                    showHeader = false,
                )

                Spacer(modifier = Modifier.height(24.dp))

                PinHomeEditSectionLabel(text = "상세 설명")

                CommonTextField(
                    value = homeEdit.description,
                    onValueChange = homeEditCallbacks.onDescriptionChange,
                    placeholder = "상세 설명을 작성해 주세요.",
                    maxLines = 8,
                    maxLength = 500,
                    textStyle = IssueTypo.Regular16,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (issueDetail != null) {
                    Spacer(modifier = Modifier.height(24.dp))

                    PinHomeEditSectionLabel(
                        text = "AI 글쓰기",
                        description = "원하시는 말투를 누르면 상세 설명이 변경됩니다.",
                    )

                    IssueToneSelectionSection(
                        toneOptions = homeEdit.toneOptions,
                        isLoading = homeEdit.isLoadingToneOptions,
                        selectedTone = homeEdit.selectedTone,
                        onToneChange = homeEditCallbacks.onToneChange,
                        showTitle = false,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    IssueAiDraftButton(
                        isLoading = homeEdit.isGeneratingAiContent || homeEdit.isLoadingAiDraftQuota,
                        isEnabled = homeEdit.title.isNotBlank() &&
                            homeEdit.description.isNotBlank() &&
                            !homeEdit.isSubmittingHomeEdit &&
                            !homeEdit.isGeneratingAiContent &&
                            !homeEdit.isLoadingAiDraftQuota,
                        loadingText = when {
                            homeEdit.isGeneratingAiContent -> "AI 글 작성 중"
                            homeEdit.isLoadingAiDraftQuota -> "확인 중"
                            else -> "AI 글쓰기"
                        },
                        onClick = homeEditCallbacks.onAiDraftClick,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PinDetailImageSection(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
) {
    if (imageUrls.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            AsyncImage(
                model = imageUrls[page],
                contentDescription = "핀 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 320.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Gray_3),
                contentScale = ContentScale.Crop,
            )
        }

        if (imageUrls.size > 1) {
            DotPagerIndicator(
                pageCount = imageUrls.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun PinHomeEditSectionLabel(
    text: String,
    trailing: (@Composable () -> Unit)? = null,
    description: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = IssueTypo.Regular18.copy(color = Title),
            )
            if (trailing != null) {
                Spacer(modifier = Modifier.width(10.dp))
                trailing()
            }
        }
        description?.let {
            Text(
                text = it,
                style = IssueTypo.Regular15.copy(color = Gray_5),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PinHomeEditPhotoSection(
    existingImages: List<PinImageRef>,
    newImageUris: List<String>,
    mainImageKey: String?,
    onPhotoAddClick: () -> Unit,
    onExistingImageRemove: (String) -> Unit,
    onNewImageRemove: (String) -> Unit,
    onSetMainImage: (String) -> Unit,
    showHeader: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val totalCount = existingImages.size + newImageUris.size
    Column(modifier = modifier.fillMaxWidth()) {
        if (showHeader) {
            PinHomeEditSectionLabel(
                text = "사진",
                trailing = {
                    Text(
                        text = "$totalCount/${PinImageUploadConstraints.MAX_COUNT}",
                        style = IssueTypo.Regular12.copy(color = Gray_6),
                    )
                },
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (totalCount < PinImageUploadConstraints.MAX_COUNT) {
                PinHomePhotoAddBox(onClick = onPhotoAddClick)
            }
            existingImages.forEach { image ->
                PinHomeEditPhotoBox(
                    model = image.imageUrl,
                    isMain = image.imageUrl == mainImageKey,
                    onSetMainClick = { onSetMainImage(image.imageUrl) },
                    onRemoveClick = { onExistingImageRemove(image.imageUrl) },
                )
            }
            newImageUris.forEach { uri ->
                PinHomeEditPhotoBox(
                    model = uri,
                    isMain = uri == mainImageKey,
                    onSetMainClick = { onSetMainImage(uri) },
                    onRemoveClick = { onNewImageRemove(uri) },
                )
            }
        }
    }
}

@Composable
private fun PinHomePhotoAddBox(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Gray_1)
            .border(1.dp, Gray_3, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = "사진 추가",
            tint = Gray_5,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "사진 추가",
            style = IssueTypo.Regular12.copy(color = Gray_5),
        )
    }
}

@Composable
private fun PinHomeEditPhotoBox(
    model: String,
    isMain: Boolean,
    onSetMainClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Gray_1)
            .then(
                if (isMain) {
                    Modifier.border(2.dp, Orange, RoundedCornerShape(12.dp))
                } else {
                    Modifier.border(1.dp, Gray_3, RoundedCornerShape(12.dp))
                },
            )
            .clickable(onClick = onSetMainClick),
    ) {
        AsyncImage(
            model = model,
            contentDescription = "첨부 사진",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (isMain) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .background(Orange, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "대표",
                    style = IssueTypo.Bold12.copy(color = White, fontSize = 10.sp),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onRemoveClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "사진 삭제",
                tint = White,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun PinHomeEditActionButtons(
    isSubmitting: Boolean,
    onCancelClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CircleActionIcon(
            iconRes = R.drawable.ic_cancel,
            contentDescription = "취소",
            onClick = onCancelClick,
            enabled = !isSubmitting,
            iconTint = Gray_6,
        )
        CircleActionIcon(
            iconRes = R.drawable.ic_edit_complete,
            contentDescription = "수정 완료",
            onClick = onSubmitClick,
            enabled = !isSubmitting,
            containerColor = BrandColor,
        )
    }
}

@Composable
private fun PinHomeDiscountBanner(
    text: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(White)
            .border(width = 1.dp, color = Orange, shape = shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Orange),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "%",
                style = IssueTypo.Bold12.copy(color = White),
            )
        }
        Text(
            text = text,
            style = IssueTypo.Bold12.copy(color = Title),
            modifier = Modifier.weight(1f),
            maxLines = 2,
        )
    }
}

@Composable
private fun ResolutionStatusBadge(
    resolutionStatus: ResolutionStatus,
) {
    val (label, backgroundColor) = when (resolutionStatus) {
        ResolutionStatus.BEFORE_RESOLUTION -> "해결 전" to Title
        ResolutionStatus.IN_PROGRESS -> "해결 중" to Orange
        ResolutionStatus.RESOLVED -> "해결 완료" to BrandColor
    }
    Text(
        text = label,
        style = IssueTypo.Bold12.copy(color = White),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun PinDetailActionButtons(
    isAuthor: Boolean,
    canEdit: Boolean,
    isReported: Boolean,
    isDeleting: Boolean = false,
    onReportClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isAuthor) {
            if (canEdit) {
                CircleActionIcon(
                    iconRes = R.drawable.ic_edit,
                    contentDescription = "수정",
                    onClick = onEditClick,
                    iconTint = Gray_6
                )
            }
            CircleActionIcon(
                iconRes = R.drawable.ic_delete,
                contentDescription = "삭제",
                onClick = onDeleteClick,
                enabled = !isDeleting,
                iconTint = Gray_6
            )
        } else {
            CircleActionIcon(
                iconRes = R.drawable.ic_report,
                contentDescription = "신고",
                onClick = onReportClick,
                enabled = !isReported,
                iconTint = if (isReported) Gray_6 else Orange,
            )
        }
    }
}

@Composable
private fun CircleActionIcon(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconTint: Color? = null,
    containerColor: Color = White,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 2.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(CircleShape)
            .background(containerColor)
            .border(width = 1.dp, color = Gray_3, shape = CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.5f),
            colorFilter = iconTint?.let { ColorFilter.tint(it) }
        )
    }
}

@Composable
private fun CommunityChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Orange)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "커뮤니티 >",
            style = IssueTypo.Bold12.copy(color = White)
        )
    }
}

@Preview(name = "Issue · 작성자 본인", showBackground = true, heightDp = 800)
@Composable
private fun PinHomeTabPreview_IssueAuthor() {
    IssueissyuTheme {
        PinHomeTab(
            pin = PinSamples.findById(PinSamples.IssuePinId).copy(
                author = PinSamples.user1,
                isMine = true,
            ),
            onReportClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onCommunityClick = {}
        )
    }
}

@Preview(name = "Issue · 수정 모드", showBackground = true, heightDp = 900)
@Composable
private fun PinHomeTabPreview_IssueEditMode() {
    val pin = PinSamples.findById(PinSamples.IssuePinId).copy(
        author = PinSamples.user1,
        isMine = true,
    )
    IssueissyuTheme {
        PinHomeTab(
            pin = pin,
            onReportClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onCommunityClick = {},
            homeEdit = PinHomeEditUiState().openedFrom(pin),
        )
    }
}
