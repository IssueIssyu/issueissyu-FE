package com.issueissyu.fe.ui.screens.pincreate

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.data.model.PinCategory
import com.issueissyu.fe.ui.components.CommonButton
import com.issueissyu.fe.ui.components.CommonTextField
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.Gray_1
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Title

// TODO: AI 초안 생성 기능 추후 구현.
//       - AI 초안 응답을 받으면 title/description을 update 해서 CommonTextField에 반영.
//       - 사용자가 같은 CommonTextField에서 직접 수정 가능하도록 양방향 바인딩 유지.
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

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            PhotoUploadSection(imageCount = uiState.imageUris.size)

            LocationSection(
                pinLat = pinLat,
                pinLng = pinLng,
                address = uiState.address
            )

            CommonTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = "제목",
                placeholder = "제목을 입력하세요.",
                maxLength = 50
            )

            CommonTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "상세 설명",
                placeholder = "상세 설명을 작성해 주세요.\n해시태그를 눌러 이슈있슈 AI로 빠르게 원하는 말투로 글을 작성할 수 있어요!",
                maxLines = 8,
                maxLength = 500,
                textStyle = IssueTypo.Regular16
            )

            ToneSelectionPlaceholder(selectedTone = uiState.selectedTone)
        }

        HorizontalDivider(color = Gray_3, thickness = 1.dp)

        CommonButton(
            onClick = { viewModel.submitPin() },
            text = "작성 완료",
            isEnabled = uiState.title.isNotBlank() &&
                uiState.description.isNotBlank() &&
                !uiState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

// TODO: 실제 이미지 선택 launcher(PhotoPicker) + 업로드 흐름 연결.
//       선택된 이미지는 viewModel.imageUris에 반영하고 썸네일 미리보기로 교체.
@Composable
private fun PhotoUploadSection(imageCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "사진",
                style = IssueTypo.Regular18.copy(color = Title),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$imageCount/5",
                style = IssueTypo.Regular12.copy(color = Gray_6)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Gray_1)
                .clickable {
                    // TODO: 사진 선택 launcher 호출
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "사진 추가",
                style = IssueTypo.Regular15.copy(color = Gray_5)
            )
        }
    }
}

// TODO: 좌표 → 주소 역지오코딩 결과를 받아 address/locationName으로 표시.
//       역지오코딩은 추후 백엔드 API 또는 지도 SDK 연결로 처리 예정.
//       address는 사용자가 수정할 수 없는 readonly 영역으로 둔다.
@Composable
private fun LocationSection(
    pinLat: Double,
    pinLng: Double,
    address: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "등록 장소",
            style = IssueTypo.Regular18.copy(color = Title)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(Gray_1)
                .padding(15.dp)
        ) {
            val locationText = if (address.isNotBlank()) {
                address
            } else {
                "위도 %.6f, 경도 %.6f".format(pinLat, pinLng)
            }
            Text(
                text = locationText,
                style = IssueTypo.Regular15.copy(color = Gray_6)
            )
        }
    }
}

// TODO: 말투 설정 BottomSheet/Screen 구현 후 선택값을 selectedTone에 반영.
//       선택한 말투는 추후 AI 초안 생성 요청 파라미터로 사용 예정.
@Composable
private fun ToneSelectionPlaceholder(selectedTone: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "말투 설정",
            style = IssueTypo.Regular18.copy(color = Title)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(Gray_1)
                .padding(15.dp)
        ) {
            val text = selectedTone ?: "말투 설정 기능은 추후 구현 예정입니다."
            Text(
                text = text,
                style = IssueTypo.Regular15.copy(color = Gray_5)
            )
        }
    }
}
