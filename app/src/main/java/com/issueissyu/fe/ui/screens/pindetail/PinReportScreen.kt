package com.issueissyu.fe.ui.screens.pindetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Title

// TODO: 서버 신고 사유 enum 확정 후 PinReportReason으로 교체
private val PinReportReasons = listOf(
    "거짓 정보를 포함한 글이에요",
    "욕설 또는 비하 표현이 포함된 글이에요",
    "스팸 또는 도배성 글이에요",
    "불쾌감을 주는 글이에요",
    "종교 포교 목적의 글이에요"
)

// TODO: PinReportRepository 연결 후 실제 신고 요청 전송
@Composable
fun PinReportScreen(
    @Suppress("UNUSED_PARAMETER") pinId: String,
    onBackClick: () -> Unit,
    onSubmitClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IssueissyuTopAppBar(
            onBackClick = onBackClick,
            titleText = ""
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "핀 신고",
                style = IssueTypo.Bold18.copy(color = Title),
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = Gray_3, thickness = 1.dp)

            PinReportReasons.forEach { reason ->
                PinReportReasonItem(
                    reason = reason,
                    onClick = { onSubmitClick(reason) }
                )
                HorizontalDivider(color = Gray_3, thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PinReportReasonItem(
    reason: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = reason,
            style = IssueTypo.Regular15.copy(color = Title),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Gray_5,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(name = "PinReport · 신고 사유 목록", showBackground = true, heightDp = 800)
@Composable
private fun PinReportScreenPreview() {
    IssueissyuTheme {
        PinReportScreen(
            pinId = "preview_pin",
            onBackClick = {},
            onSubmitClick = {}
        )
    }
}
