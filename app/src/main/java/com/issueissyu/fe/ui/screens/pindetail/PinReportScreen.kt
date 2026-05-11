package com.issueissyu.fe.ui.screens.pindetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.issueissyu.fe.ui.components.CommonButton
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_4
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White

// 신고 사유 임시 목록.
// TODO: 서버 신고 사유 enum 확정 후 String 대신 PinReportReason으로 교체.
private val PinReportReasons = listOf(
    "허위 정보",
    "부적절한 내용",
    "욕설/비방",
    "광고/스팸",
    "기타"
)

// TODO: 실제 신고 API 연결 시 PinReportRepository / PinReportViewModel 분리 검토.
//       현재는 selectedReason String을 onSubmitClick으로 그대로 전달하는 구조만 유지.
//       pinId는 시그니처 안정성을 위해 받아 두지만, Repository 연결 전까지 직접 사용처가 없다.
@Composable
fun PinReportScreen(
    @Suppress("UNUSED_PARAMETER") pinId: String,
    onBackClick: () -> Unit,
    onSubmitClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedReason by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IssueissyuTopAppBar(
            onBackClick = onBackClick,
            titleText = "신고하기"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "신고 사유를 선택해 주세요.",
                style = IssueTypo.Bold18.copy(color = Title)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "신고된 내용은 운영팀이 확인 후 조치합니다.",
                style = IssueTypo.Regular12.copy(color = Gray_6)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PinReportReasons.forEach { reason ->
                    PinReportReasonItem(
                        reason = reason,
                        isSelected = selectedReason == reason,
                        onClick = { selectedReason = reason }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CommonButton(
                onClick = { selectedReason?.let { onSubmitClick(it) } },
                text = "신고하기",
                isEnabled = selectedReason != null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PinReportReasonItem(
    reason: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) BrandColor.copy(alpha = 0.1f) else White
    val borderColor = if (isSelected) BrandColor else Gray_4
    val textColor = if (isSelected) BrandColor else Title

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = reason,
            style = IssueTypo.Regular15.copy(color = textColor),
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "선택됨",
                tint = BrandColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(name = "PinReport · 미선택 (버튼 비활성)", showBackground = true, heightDp = 800)
@Composable
private fun PinReportScreenPreview_NoSelection() {
    IssueissyuTheme {
        PinReportScreen(
            pinId = "preview_pin",
            onBackClick = {},
            onSubmitClick = {}
        )
    }
}

// PinReportScreen 자체는 selectedReason을 내부 state로만 관리하므로
// "선택된 모습"은 PinReportReasonItem을 직접 노출해 미리본다.
@Preview(name = "PinReport · 항목 선택/미선택 비교", showBackground = true)
@Composable
private fun PinReportReasonItemPreview() {
    IssueissyuTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PinReportReasonItem(
                reason = "허위 정보",
                isSelected = true,
                onClick = {}
            )
            PinReportReasonItem(
                reason = "부적절한 내용",
                isSelected = false,
                onClick = {}
            )
            PinReportReasonItem(
                reason = "욕설/비방",
                isSelected = false,
                onClick = {}
            )
        }
    }
}
