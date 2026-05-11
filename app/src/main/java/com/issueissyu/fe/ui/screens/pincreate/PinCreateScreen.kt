package com.issueissyu.fe.ui.screens.pincreate

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
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.data.model.PinCategory
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

// TODO: AI 초안 생성 기능 추후 구현.
//       - AI 초안 응답을 받으면 title/description을 update해서 CommonTextField에 반영.
//       - 작성자가 같은 CommonTextField에서 직접 수정 가능하도록 양방향 바인딩 유지.
//       - 요청 진행 중에는 uiState.isGeneratingAiContent로 로딩 상태 표시 예정.
@Composable
fun PinCreateScreen(
    category: PinCategory,
    pinLat: Double,
    pinLng: Double,
    userLat: Double,
    userLng: Double,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PinCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(category, pinLat, pinLng, userLat, userLng) {
        viewModel.initialize(category, pinLat, pinLng, userLat, userLng)
    }

    PinCreateContent(
        category = category,
        uiState = uiState,
        onBackClick = onBackClick,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onToneChange = viewModel::onToneChange,
        onSubmit = viewModel::submitPin,
        modifier = modifier
    )
}

// hiltViewModel 의존을 제거한 stateless 본체. Preview 및 단위 테스트 친화.
// pinLat/pinLng는 화면에 표시하지 않으므로 PinCreateContent 시그니처에서 제외한다
// (route argument는 PinCreateScreen에서 LaunchedEffect → viewModel.initialize로만 사용).
@Composable
private fun PinCreateContent(
    category: PinCategory,
    uiState: PinCreateUiState,
    onBackClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onToneChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 일반 유저 생성 대상은 ISSUE / COMMUNICATION 두 가지이며, 그 외 카테고리는 NavGraph에서 차단된다.
    // SHOP / FESTIVAL은 가드용 디폴트만 두고 실제로는 도달하지 않는다.
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

        // 본문 영역: 작성 완료 버튼은 화면 하단 고정, 그 위 입력 영역만 스크롤.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PhotoUploadSection(imageCount = uiState.imageUris.size)

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

            // TODO: CommonTextField가 multiline 입력 시 시안의 본문 영역 높이를 완전히 맞추지 못하면,
            //       PinCreateScreen 전용 multiline 컴포넌트 분리 검토.
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

            // 하단 버튼과의 사이 여백.
            Spacer(modifier = Modifier.height(4.dp))
        }

        HorizontalDivider(color = Gray_3, thickness = 1.dp)

        // TODO: createPin API 연결 (PinRepository.createPin(CreatePinRequest(...))).
        // TODO: 이미지 업로드 후 반환된 imageUrls로 생성 요청 구성.
        // TODO: AI 초안 적용 여부와 최종 title/description 값 검증.
        CommonButton(
            onClick = onSubmit,
            text = "작성 완료",
            isEnabled = uiState.title.isNotBlank() &&
                uiState.description.isNotBlank() &&
                !uiState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 16.dp)
        )
    }
}

// 시안 기준: 섹션 라벨은 CommonTextField의 label 스타일과 동일하게 정렬한다.
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

// TODO: 이미지 선택/업로드 기능 연결 후 imageUris 갱신.
// TODO: 최대 5장 제한 적용 (현재는 placeholder만 노출).
@Composable
private fun PhotoUploadSection(imageCount: Int) {
    Column {
        SectionLabel(
            text = "사진",
            trailing = {
                Text(
                    text = "$imageCount/5",
                    style = IssueTypo.Regular12.copy(color = Gray_6)
                )
            }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PhotoAddBox(
                onClick = {
                    // TODO: 사진 선택 launcher 호출 후 viewModel에 반영.
                }
            )
            // TODO: imageUris를 thumbnail로 PhotoAddBox 우측에 나열.
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

// TODO: 지도에서 선택한 위경도를 백엔드에 전달해 생성 가능 여부와 주소를 검증한다.
// TODO: 백엔드가 반환한 주소를 address/locationName에 반영한다.
// TODO: 등록 장소는 사용자 수정 불가 상태로 유지한다.
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
            // 등록 장소 영역에는 절대로 pinLat/pinLng 같은 좌표를 노출하지 않는다.
            // 좌표는 생성 요청/검증용 데이터이고, 사용자에게는 주소 문자열만 보여 준다.
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

// 말투 옵션 임시 목록.
// TODO: 서버/기획 확정 후 enum 또는 서버 응답 기반으로 교체.
private val PinToneOptions = listOf(
    "#가볍게",
    "#공손하게",
    "#친근하게",
    "#부드럽게",
    "#진중하게",
    "#공식적으로"
)

// TODO: 선택된 말투를 AI 초안 요청 파라미터에 포함.
// TODO: AI 초안 응답은 title/description 상태에 반영.
// TODO: 작성자가 CommonTextField에서 직접 수정 가능해야 함.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ToneSelectionSection(
    selectedTone: String?,
    onToneChange: (String) -> Unit
) {
    Column {
        SectionLabel(text = "말투 설정")
        // 시안 기준: 말투는 드롭다운이 아니라 chip 선택형. 사용자가 직접 보고 하나를 고른다.
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

// PinCreateScreen 본체는 hiltViewModel 의존이 있어 IDE Preview에서 직접 mount 하기 어렵다.
// 대신 stateless PinCreateContent를 직접 호출해 입력 상태별 외관을 미리본다.
// (pinLat/pinLng는 화면에 표시되지 않으므로 Preview에서도 전달하지 않는다.)

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
                selectedTone = "#공손하게"
            ),
            onBackClick = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onToneChange = {},
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
                selectedTone = "친근하게"
            ),
            onBackClick = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onToneChange = {},
            onSubmit = {}
        )
    }
}
