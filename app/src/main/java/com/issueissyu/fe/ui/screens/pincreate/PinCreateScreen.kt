package com.issueissyu.fe.ui.screens.pincreate

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.ui.components.CommonButton
import com.issueissyu.fe.ui.components.CommonTextField
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
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
    modifier: Modifier = Modifier,
    viewModel: PinCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_IMAGE_COUNT),
    ) { uris ->
        viewModel.addImageUris(uris.map { it.toString() })
    }

    LaunchedEffect(category, pinLat, pinLng, userLat, userLng, address) {
        viewModel.initialize(category, pinLat, pinLng, userLat, userLng, address)
    }

    LaunchedEffect(Unit) {
        viewModel.createdEvents.collect {
            onBackClick()
        }
    }

    PinCreateContent(
        category = category,
        uiState = uiState,
        onBackClick = onBackClick,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onToneChange = viewModel::onToneChange,
        onPhotoAddClick = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onPhotoRemoveClick = viewModel::removeImageUri,
        onAiDraftClick = viewModel::createAiDraft,
        onSubmit = viewModel::submitPin,
        modifier = modifier
    )
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
                onPhotoAddClick = onPhotoAddClick,
                onPhotoRemoveClick = onPhotoRemoveClick,
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
                placeholder = "상세 설명을 작성해 주세요.\n해시태그를 눌러 이슈있슈 AI로 빠르게 원하는 말투로 글을 작성할 수 있어요!",
                maxLines = 8,
                maxLength = 500,
                textStyle = IssueTypo.Regular16
            )

            ToneSelectionSection(
                selectedTone = uiState.selectedTone,
                onToneChange = onToneChange
            )

            if (category == PinCategory.ISSUE) {
                AiDraftButton(
                    isLoading = uiState.isGeneratingAiContent,
                    isEnabled = uiState.title.isNotBlank() &&
                        uiState.description.isNotBlank() &&
                        !uiState.isGeneratingAiContent,
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
    onPhotoAddClick: () -> Unit,
    onPhotoRemoveClick: (String) -> Unit,
) {
    Column {
        SectionLabel(
            text = "사진",
            trailing = {
                Text(
                    text = "${imageUris.size}/$MAX_IMAGE_COUNT",
                    style = IssueTypo.Regular12.copy(color = Gray_6)
                )
            }
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (imageUris.size < MAX_IMAGE_COUNT) {
                PhotoAddBox(onClick = onPhotoAddClick)
            }
            imageUris.forEach { uri ->
                SelectedPhotoBox(
                    uri = uri,
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
    onRemoveClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Gray_1)
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "첨부 사진",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
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

// TODO: 서버/기획 확정 후 enum 또는 서버 응답 기반으로 교체
private val PinToneOptions = listOf(
    "없음",
    "한줄요약형",
    "상황설명형",
    "개선요청형",
    "긴급요청형",
    "불편호소형"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ToneSelectionSection(
    selectedTone: String?,
    onToneChange: (String) -> Unit
) {
    Column {
        SectionLabel(text = "말투 설정")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PinToneOptions.forEach { tone ->
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
    onClick: () -> Unit,
) {
    CommonButton(
        onClick = onClick,
        text = if (isLoading) "AI 글 작성 중" else "AI 글쓰기",
        isEnabled = isEnabled,
        textStyle = IssueTypo.Bold18.copy(fontSize = 16.sp),
        modifier = Modifier.fillMaxWidth()
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
                selectedTone = "개선요청형"
            ),
            onBackClick = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onToneChange = {},
            onPhotoAddClick = {},
            onPhotoRemoveClick = {},
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
                selectedTone = "상황설명형"
            ),
            onBackClick = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onToneChange = {},
            onPhotoAddClick = {},
            onPhotoRemoveClick = {},
            onAiDraftClick = {},
            onSubmit = {}
        )
    }
}

private const val MAX_IMAGE_COUNT = 5
