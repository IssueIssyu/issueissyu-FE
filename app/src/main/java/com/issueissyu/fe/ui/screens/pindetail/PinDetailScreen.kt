package com.issueissyu.fe.ui.screens.pindetail

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.data.model.IssuePinDetail
import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.data.sample.PinSamples
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Orange
import com.issueissyu.fe.ui.theme.Title

@Composable
fun PinDetailScreen(
    pinId: String,
    onBackClick: () -> Unit,
    onReportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PinDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(pinId) {
        viewModel.loadPin(pinId)
    }

    // TODO: 로그인 연동 후 실제 currentUserId로 교체.
    val currentUserId = "user_1"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IssueissyuTopAppBar(
            onBackClick = onBackClick,
            titleText = ""
        )

        val pin = uiState.pin
        val error = uiState.errorMessage

        when {
            uiState.isLoading -> {
                CenteredPlaceholder(
                    modifier = Modifier.weight(1f),
                    text = "핀 정보를 불러오는 중..."
                )
            }

            error != null -> {
                CenteredPlaceholder(
                    modifier = Modifier.weight(1f),
                    text = error
                )
            }

            pin != null -> {
                PinDetailTabs(
                    pin = pin,
                    currentUserId = currentUserId,
                    selectedTab = uiState.selectedTab,
                    onSelectTab = viewModel::selectTab,
                    onReportClick = onReportClick,
                    onEditClick = {
                        // TODO: 핀 수정 화면으로 이동
                    },
                    onDeleteClick = {
                        // TODO: 핀 삭제 확인 Dialog 또는 삭제 API 연결
                    },
                    onCommunityClick = {
                        // TODO: 커뮤니티 상세 화면으로 이동
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            else -> {
                CenteredPlaceholder(
                    modifier = Modifier.weight(1f),
                    text = "핀 상세 화면은 추후 구현 예정입니다."
                )
            }
        }
    }
}

@Composable
private fun PinDetailTabs(
    pin: Pin,
    currentUserId: String,
    selectedTab: PinDetailTab,
    onSelectTab: (PinDetailTab) -> Unit,
    onReportClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onCommunityClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // RESOLVED 상태여도 해결하기 탭 자체는 표시한다.
    // RESOLVED 이후 막아야 하는 것은 해결하기 탭 내부의 시민해결사 상호작용이다.
    val tabs = buildList {
        add(PinDetailTab.HOME)
        add(PinDetailTab.POST)
        if (pin.detail is IssuePinDetail) {
            add(PinDetailTab.RESOLUTION)
        }
    }

    val selectedTabIndex = tabs.indexOf(selectedTab).takeIf { it >= 0 } ?: 0
    val effectiveTab = tabs[selectedTabIndex]

    Column(modifier = modifier.fillMaxSize()) {
        PinDetailTabBar(
            tabs = tabs,
            effectiveTab = effectiveTab,
            onSelectTab = onSelectTab
        )

        when (effectiveTab) {
            PinDetailTab.HOME -> {
                PinHomeTab(
                    pin = pin,
                    currentUserId = currentUserId,
                    onReportClick = onReportClick,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    onCommunityClick = onCommunityClick,
                    modifier = Modifier.fillMaxSize()
                )
            }

            PinDetailTab.POST -> {
                PinPostTab(
                    pin = pin,
                    currentUserId = currentUserId,
                    onSympathyClick = {
                        // TODO: 공감 API 연결
                    },
                    onEmojiClick = {
                        // TODO: 이모지 반응 API 연결 (이모지 선택 UI 포함)
                    },
                    onCommentSubmit = { _, _ ->
                        // TODO: 댓글 작성 API 연결 (PinCommentRepository.addComment 등)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            PinDetailTab.RESOLUTION -> {
                val issueDetail = pin.detail as? IssuePinDetail
                if (issueDetail != null) {
                    PinResolutionTab(
                        pin = pin,
                        issueDetail = issueDetail,
                        currentUserId = currentUserId,
                        onGoNowClick = {
                            // TODO: 지금가요 API 연결 (PinResolutionRepository.joinAsResolver 등)
                        },
                        onPetitionClick = {
                            // TODO: 청원 API 연결 (PinPetitionRepository.petition 등)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CenteredPlaceholder(
                        modifier = Modifier.fillMaxSize(),
                        text = "해결하기는 이슈 핀에서만 사용할 수 있습니다."
                    )
                }
            }
        }
    }
}

@Composable
private fun PinDetailTabBar(
    tabs: List<PinDetailTab>,
    effectiveTab: PinDetailTab,
    onSelectTab: (PinDetailTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEach { tab ->
                PinDetailTabItem(
                    label = tab.label(),
                    selected = effectiveTab == tab,
                    onClick = { onSelectTab(tab) },
                    modifier = Modifier.weight(1f)
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
    modifier: Modifier = Modifier
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, style = textStyle)
        // 시안 기준: 선택 탭 아래만 주황 underline.
        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth(0.5f)
                .background(if (selected) Orange else Color.Transparent)
        )
    }
}

@Composable
private fun CenteredPlaceholder(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}

private fun PinDetailTab.label(): String {
    return when (this) {
        PinDetailTab.HOME -> "홈"
        PinDetailTab.POST -> "포스트"
        PinDetailTab.RESOLUTION -> "해결하기"
    }
}

// PinDetailScreen 본체는 hiltViewModel/PinRepository 의존이 있어 IDE Preview에서 직접 mount 하기 어렵다.
// 대신 같은 파일 안에 있는 PinDetailTabs / CenteredPlaceholder 를 직접 호출해 화면 골격(상단바 + 탭/Placeholder)을 미리본다.

@Preview(name = "PinDetail · 이슈 핀 로드 완료", showBackground = true, heightDp = 900)
@Composable
private fun PinDetailScreenPreview_IssueLoaded() {
    IssueissyuTheme {
        var selectedTab by remember { mutableStateOf(PinDetailTab.HOME) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            IssueissyuTopAppBar(
                onBackClick = {},
                titleText = ""
            )
            PinDetailTabs(
                pin = PinSamples.findById(PinSamples.IssueInProgressPinId),
                currentUserId = PinSamples.user1.id,
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                onReportClick = {},
                onEditClick = {},
                onDeleteClick = {},
                onCommunityClick = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(name = "PinDetail · 로딩", showBackground = true, heightDp = 900)
@Composable
private fun PinDetailScreenPreview_Loading() {
    IssueissyuTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            IssueissyuTopAppBar(
                onBackClick = {},
                titleText = ""
            )
            CenteredPlaceholder(
                modifier = Modifier.weight(1f),
                text = "핀 정보를 불러오는 중..."
            )
        }
    }
}

@Preview(name = "PinDetail · 에러", showBackground = true, heightDp = 900)
@Composable
private fun PinDetailScreenPreview_Error() {
    IssueissyuTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            IssueissyuTopAppBar(
                onBackClick = {},
                titleText = ""
            )
            CenteredPlaceholder(
                modifier = Modifier.weight(1f),
                text = "핀 정보를 불러오지 못했습니다."
            )
        }
    }
}
