package com.issueissyu.fe.ui.screens.pindetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.data.model.IssuePinDetail
import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar

@Composable
fun PinDetailScreen(
    pinId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PinDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(pinId) {
        viewModel.loadPin(pinId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IssueissyuTopAppBar(
            onBackClick = onBackClick,
            titleText = "핀 상세"
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
                    selectedTab = uiState.selectedTab,
                    onSelectTab = viewModel::selectTab,
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
    selectedTab: PinDetailTab,
    onSelectTab: (PinDetailTab) -> Unit,
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
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEach { tab ->
                Tab(
                    selected = effectiveTab == tab,
                    onClick = { onSelectTab(tab) },
                    text = { Text(text = tab.label()) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (effectiveTab) {
                PinDetailTab.HOME -> Text(text = "홈 탭은 추후 구현 예정입니다.")
                PinDetailTab.POST -> Text(text = "포스트 탭은 추후 구현 예정입니다.")
                PinDetailTab.RESOLUTION -> Text(text = "해결하기 탭은 추후 구현 예정입니다.")
            }
        }
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
