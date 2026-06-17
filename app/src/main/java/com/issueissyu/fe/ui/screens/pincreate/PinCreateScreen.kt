package com.issueissyu.fe.ui.screens.pincreate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.core.constants.PinImageUploadConstraints
import com.issueissyu.fe.core.media.rememberPhotoSourcePicker
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinEditRateLimitQuota
import com.issueissyu.fe.ui.components.CommonButton
import com.issueissyu.fe.ui.components.CommonTextField
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.components.RemainingQuotaDialog
import com.issueissyu.fe.ui.theme.Gray_1
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_4
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Orange
import com.issueissyu.fe.ui.theme.Text as TextColor
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage

@Composable
fun PinCreateScreen(
    category: PinCategory,
    pinLat: Double,
    pinLng: Double,
    userLat: Double,
    userLng: Double,
    address: String,
    onBackClick: () -> Unit,
    onCreated: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PinCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val photoSourcePicker = rememberPhotoSourcePicker(
        currentCount = uiState.imageUris.size,
        maxCount = PinImageUploadConstraints.MAX_COUNT,
        onImagesPicked = viewModel::addImageUris,
        cameraFileNamePrefix = "pin-create",
    )
    photoSourcePicker.PhotoSourceBottomSheet()

    LaunchedEffect(category, pinLat, pinLng, userLat, userLng, address) {
        viewModel.initialize(category, pinLat, pinLng, userLat, userLng, address)
    }

    LaunchedEffect(Unit) {
        viewModel.createdEvents.collect { createdPinId ->
            onCreated(createdPinId)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PinCreateContent(
            category = category,
            uiState = uiState,
            onBackClick = onBackClick,
            onTitleChange = viewModel::onTitleChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onToneChange = viewModel::onToneChange,
            onPhotoAddClick = photoSourcePicker.showSourceSheet,
            onPhotoRemoveClick = viewModel::removeImageUri,
            onSetMainImageClick = viewModel::setMainImageUri,
            onAiDraftClick = viewModel::createAiDraft,
            onSubmit = viewModel::submitPin,
            modifier = Modifier.fillMaxSize(),
        )

        if (uiState.showAiDraftConfirmDialog) {
            val dialogContent = uiState.aiDraftRateLimitQuota.toAiDraftConfirmDialogContent()
            RemainingQuotaDialog(
                title = dialogContent.title,
                countLabel = dialogContent.countLabel,
                description = dialogContent.description,
                onDismiss = viewModel::dismissAiDraftConfirmDialog,
                onConfirm = viewModel::confirmAiDraft,
            )
        }
    }
}

@Composable
private fun PinCreateContent(
    category: PinCategory,
    uiState: PinCreateUiState,
    onBackClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onToneChange: (String) -> Unit,
    onPhotoAddClick: () -> Unit,
    onPhotoRemoveClick: (String) -> Unit,
    onSetMainImageClick: (String) -> Unit,
    onAiDraftClick: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleText = when (category) {
        PinCategory.ISSUE -> "이슈 작성"
        PinCategory.COMMUNICATION -> "소통 작성"
        PinCategory.SHOP, PinCategory.FESTIVAL -> ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IssueissyuTopAppBar(
            onBackClick = onBackClick,
            titleText = titleText
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PhotoUploadSection(
                imageUris = uiState.imageUris,
                mainImageUri = uiState.mainImageUri,
                onPhotoAddClick = onPhotoAddClick,
                onPhotoRemoveClick = onPhotoRemoveClick,
                onSetMainImageClick = onSetMainImageClick,
            )

            LocationSection(
                address = uiState.address,
                locationName = uiState.locationName
            )

            CommonTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = "제목",
                placeholder = "제목을 입력하세요.",
                maxLength = 50
            )

            CommonTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = "상세 설명",
                placeholder = when (category) {
                    PinCategory.ISSUE ->
                        "상세 설명을 작성해 주세요.\n해시태그를 눌러 이슈있슈 AI로 빠르게 원하는 말투로 글을 작성할 수 있어요!"
                    PinCategory.COMMUNICATION -> "상세 설명을 작성해 주세요."
                    PinCategory.SHOP, PinCategory.FESTIVAL -> "상세 설명을 작성해 주세요."
                },
                maxLines = 8,
                maxLength = 500,
                textStyle = IssueTypo.Regular16
            )

            if (category == PinCategory.ISSUE) {
                ToneSelectionSection(
                    toneOptions = uiState.toneOptions,
                    isLoading = uiState.isLoadingToneOptions,
                    selectedTone = uiState.selectedTone,
                    onToneChange = onToneChange,
                )
            }

            if (category == PinCategory.ISSUE) {
                AiDraftButton(
                    isLoading = uiState.isGeneratingAiContent || uiState.isLoadingAiDraftQuota,
                    isEnabled = uiState.title.isNotBlank() &&
                        uiState.description.isNotBlank() &&
                        !uiState.isGeneratingAiContent &&
                        !uiState.isLoadingAiDraftQuota,
                    loadingText = when {
                        uiState.isGeneratingAiContent -> "AI 글 작성 중"
                        uiState.isLoadingAiDraftQuota -> "확인 중"
                        else -> "AI 글쓰기"
                    },
                    onClick = onAiDraftClick,
                )
            }

            uiState.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
                Text(
                    text = message,
                    style = IssueTypo.Regular12.copy(color = Orange),
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        HorizontalDivider(color = Gray_3, thickness = 1.dp)

        CommonButton(
            onClick = onSubmit,
            text = if (uiState.isSubmitting) "작성 중" else "작성 완료",
            isEnabled = uiState.title.isNotBlank() &&
                uiState.description.isNotBlank() &&
                !uiState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun SectionLabel(
    text: String,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = IssueTypo.Regular18.copy(color = Title),
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhotoUploadSection(
    imageUris: List<String>,
    mainImageUri: String?,
    onPhotoAddClick: () -> Unit,
    onPhotoRemoveClick: (String) -> Unit,
    onSetMainImageClick: (String) -> Unit,
) {
    Column {
        SectionLabel(
            text = "사진",
            trailing = {
                Text(
                    text = "${imageUris.size}/${PinImageUploadConstraints.MAX_COUNT}",
                    style = IssueTypo.Regular12.copy(color = Gray_6)
                )
            }
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (imageUris.size < PinImageUploadConstraints.MAX_COUNT) {
                PhotoAddBox(onClick = onPhotoAddClick)
            }
            imageUris.forEach { uri ->
                SelectedPhotoBox(
                    uri = uri,
                    isMain = uri == mainImageUri,
                    onSetMainClick = { onSetMainImageClick(uri) },
                    onRemoveClick = { onPhotoRemoveClick(uri) },
                )
            }
        }
    }
}

@Composable
private fun PhotoAddBox(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Gray_1)
            .border(1.dp, Gray_3, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = "사진 추가",
            tint = Gray_5,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "사진 추가",
            style = IssueTypo.Regular12.copy(color = Gray_5)
        )
    }
}

@Composable
private fun SelectedPhotoBox(
    uri: String,
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
                    Modifier
                },
            )
            .clickable(onClick = onSetMainClick),
    ) {
        AsyncImage(
            model = uri,
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
                    style = IssueTypo.Regular12.copy(color = White),
                )
            }
        }
        IconButton(
            onClick = onRemoveClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .background(Title.copy(alpha = 0.6f), RoundedCornerShape(bottomStart = 12.dp)),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "사진 삭제",
                tint = White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// TODO: 좌표 기반 주소 변환 결과를 address/locationName으로 표시 (readonly 유지)
@Composable
private fun LocationSection(
    address: String,
    locationName: String?
) {
    Column {
        SectionLabel(text = "등록 장소")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Gray_1)
                .border(1.dp, Gray_3, RoundedCornerShape(15.dp))
                .padding(horizontal = 15.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val placeText = when {
                !locationName.isNullOrBlank() -> locationName
                address.isNotBlank() -> address
                else -> "주소 정보를 불러오는 중입니다."
            }
            val placeTextColor = if (placeText == "주소 정보를 불러오는 중입니다.") {
                Gray_6
            } else {
                TextColor
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Gray_6,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = placeText,
                    style = IssueTypo.Regular15.copy(color = placeTextColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ToneSelectionSection(
    toneOptions: List<String>,
    isLoading: Boolean,
    selectedTone: String?,
    onToneChange: (String) -> Unit,
) {
    Column {
        SectionLabel(text = "말투 설정")
        if (isLoading && toneOptions.isEmpty()) {
            Text(
                text = "말투 목록 불러오는 중…",
                style = IssueTypo.Regular12.copy(color = Gray_4),
            )
            return@Column
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            toneOptions.forEach { tone ->
                ToneChip(
                    label = tone,
                    selected = selectedTone == tone,
                    onClick = { onToneChange(tone) }
                )
            }
        }
    }
}

@Composable
private fun AiDraftButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    loadingText: String,
    onClick: () -> Unit,
) {
    CommonButton(
        onClick = onClick,
        text = if (isLoading) loadingText else "AI 글쓰기",
        isEnabled = isEnabled,
        textStyle = IssueTypo.Bold18.copy(fontSize = 16.sp),
        modifier = Modifier.fillMaxWidth()
    )
}

private data class AiDraftConfirmDialogContent(
    val title: String,
    val countLabel: String?,
    val description: String,
)

private fun PinEditRateLimitQuota?.toAiDraftConfirmDialogContent(): AiDraftConfirmDialogContent {
    if (this == null || !enabled) {
        return AiDraftConfirmDialogContent(
            title = "AI 글쓰기",
            countLabel = null,
            description = "이대로 AI 글쓰기를 진행하시겠습니까?",
        )
    }
    return AiDraftConfirmDialogContent(
        title = "남은 자동 글쓰기 횟수",
        countLabel = "$remainingCount/$dailyLimit",
        description = "자동 글쓰기는 정해진 일일 한도 내에서만 사용 가능합니다!",
    )
}

@Composable
private fun ToneChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) Orange else White
    val borderColor = if (selected) Orange else Gray_4
    val textColor = if (selected) White else Title

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = IssueTypo.Regular15.copy(color = textColor)
        )
    }
}

@Preview(name = "PinCreate · ISSUE 입력 채워짐", showBackground = true, heightDp = 900)
@Composable
private fun PinCreateScreenPreview_IssueFilled() {
    IssueissyuTheme {
        PinCreateContent(
            category = PinCategory.ISSUE,
            uiState = PinCreateUiState(
                category = PinCategory.ISSUE,
                title = "건대 입구역 횡단보도 신호 짧아요",
                description = "퇴근 시간대 사람이 너무 많은데 신호가 30초밖에 안 돼서 못 건너요.",
                address = "서울 광진구 능동로 120",
                locationName = "건국대학교 입구",
                toneOptions = listOf("없음", "한줄요약형", "상황설명형", "개선요청형", "긴급요청형", "불편호소형"),
                selectedTone = "개선요청형",
            ),
            onBackClick = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onToneChange = {},
            onPhotoAddClick = {},
            onPhotoRemoveClick = {},
            onSetMainImageClick = {},
            onAiDraftClick = {},
            onSubmit = {}
        )
    }
}

@Preview(name = "PinCreate · COMMUNICATION", showBackground = true, heightDp = 900)
@Composable
private fun PinCreateScreenPreview_Communication() {
    IssueissyuTheme {
        PinCreateContent(
            category = PinCategory.COMMUNICATION,
            uiState = PinCreateUiState(
                category = PinCategory.COMMUNICATION,
                title = "주말에 같이 산책하실 분 구해요",
                description = "어린이대공원 근처 살아요. 가볍게 한 바퀴 도실 분 모집합니다.",
                address = "서울 광진구 화양동",
                locationName = "화양동 주민센터 앞",
            ),
            onBackClick = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onToneChange = {},
            onPhotoAddClick = {},
            onPhotoRemoveClick = {},
            onSetMainImageClick = {},
            onAiDraftClick = {},
            onSubmit = {}
        )
    }
}
