package com.issueissyu.fe.ui.screens.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.*

@Composable
fun MyIssueScreen(
    onBackClick: () -> Unit,
    onPinClick: (pinId: String, latitude: Double, longitude: Double) -> Unit,
    viewModel: MyIssueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .background(White)
            .fillMaxSize()
    ) {
        // 상단 바
        IssueissyuTopAppBar(
            titleText = "내 이슈",
            onBackClick = onBackClick
        )

        // 본문
        if (uiState.issues.isEmpty()) {
            // 빈 상태
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "아직 생성한 핀이 없어요",
                        style = IssueTypo.Bold18.copy(color = Title)
                    )
                    Text(
                        text = "지도에서 우리 동네를 기록해보세요!",
                        style = IssueTypo.Regular15.copy(color = Text)
                    )
                }
            }
        } else {

        // 본문
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 31.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.issues) { issue ->
                MyIssueCard(
                    issue = issue,
                    onNavClick = {
                        onPinClick(
                            issue.id,
                            issue.latitude,
                            issue.longitude
                        )
                    }
                )
            }
        }
    }
        }
}

// 알림 카드
@Composable
fun MyIssueCard(
    issue: MyIssueItem,
    onNavClick: () -> Unit
) {
    // 핀 타입 정보
    val (pinTypeText, pinTypeColor) = when (issue.pinType) {
        PinType.ISSUE -> "이슈" to Issue
        PinType.COMMUNICATION -> "소통" to Communication
    }

    val cardBackgroundColor = when (issue.status) {
        IssueStatus.BEFORE_RESOLUTION -> Gray_2
        IssueStatus.IN_PROGRESS -> IssueContainerLight
        IssueStatus.RESOLVED -> CommunicationContainerLight
        else -> White
    }

    // 소통 핀일 때만 테두리
    val borderModifier = if (issue.status == null) {
        Modifier.border(1.dp, Communication, RoundedCornerShape(15.dp))
    } else {
        Modifier
    }

    Column(
        modifier = Modifier
            .clickable(onClick = onNavClick)
            .then(borderModifier)
            .background(cardBackgroundColor, RoundedCornerShape(15.dp))
            .padding(20.dp)
    ) {
        // 윗줄 (제목 + 핀종류)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = issue.title,
                fontFamily = suiteFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                color = Title,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = pinTypeText,
                    tint = pinTypeColor,
                    modifier = Modifier.size(25.dp)
                )
                Text(
                    text = pinTypeText,
                    style = IssueTypo.ExtraBold15.copy(color = Text)
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        // 아랫줄 (위치 + 진척도)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 위치
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "위치",
                    tint = Title,
                    modifier = Modifier.size(25.dp)
                )
                Text(
                    text = issue.address,
                    style = IssueTypo.Regular15.copy(color = Text),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 진척도 (IssuePin만)
            if (issue.status != null) {
                ProgressChip(status = issue.status)
            }
        }
    }
}

@Composable
fun ProgressChip(status: IssueStatus) {
    val (text, backgroundColor) = when (status) {
        IssueStatus.BEFORE_RESOLUTION -> "해결 전" to Title
        IssueStatus.IN_PROGRESS -> "진행중" to Issue
        IssueStatus.RESOLVED -> "해결 완료" to BrandColor
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(15.dp))
            .padding(horizontal = 18.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = IssueTypo.Regular16.copy(
                color = White,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMyIssueCard() {
    val dummyIssue = MyIssueItem(
        id = "test_1",
        title = "쓰레기 무단투기",
        address = "서울특별시 강남구 역삼동",
        pinType = PinType.ISSUE,
        status = IssueStatus.BEFORE_RESOLUTION,
        latitude = 37.5665,
        longitude = 126.9780
    )

    MyIssueCard(
        issue = dummyIssue,
        onNavClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewMyIssueScreen() {
    MyIssueScreen(
        onBackClick = {},
        onPinClick = { _, _, _ -> }
    )
}