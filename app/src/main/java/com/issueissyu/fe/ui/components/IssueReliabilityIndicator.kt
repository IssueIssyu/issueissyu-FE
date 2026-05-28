package com.issueissyu.fe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.issueissyu.fe.ui.theme.Gray_2
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Orange
import com.issueissyu.fe.ui.theme.Shop
import com.issueissyu.fe.ui.theme.Success
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White

private const val DEFAULT_REASON_VISIBLE_THRESHOLD = 70

enum class IssueReliabilityStatus {
    PENDING,
    COMPLETED,
    FAILED,
}

enum class IssueReliabilityType {
    PIN,
    COMMUNITY
}

enum class IssueReliabilityReasonDisplayMode {
    INLINE,
    MODAL,
    HIDDEN,
}

private data class IssueReliabilityStyle(
    val labelFontSize: TextUnit,
    val barHeight: Dp,
    val barWidth: Dp,
    val labelBarSpacing: Dp,
    val reasonFontSize: TextUnit,
    val reasonLineHeight: TextUnit,
    val reasonTopSpacing: Dp,
)

private fun IssueReliabilityType.toStyle(): IssueReliabilityStyle = when (this) {
    IssueReliabilityType.PIN -> IssueReliabilityStyle(
        labelFontSize = 15.sp,
        barHeight = 10.dp,
        barWidth = 140.dp,
        labelBarSpacing = 6.dp,
        reasonFontSize = 12.sp,
        reasonLineHeight = 16.sp,
        reasonTopSpacing = 6.dp,
    )
    IssueReliabilityType.COMMUNITY -> IssueReliabilityStyle(
        labelFontSize = 10.sp,
        barHeight = 6.dp,
        barWidth = 100.dp,
        labelBarSpacing = 4.dp,
        reasonFontSize = 10.sp,
        reasonLineHeight = 14.sp,
        reasonTopSpacing = 4.dp,
    )
}

@Composable
fun IssueReliabilityIndicator(
    score: Int?,
    reason: String?,
    type: IssueReliabilityType,
    modifier: Modifier = Modifier,
    status: IssueReliabilityStatus = if (score == null) {
        IssueReliabilityStatus.PENDING
    } else {
        IssueReliabilityStatus.COMPLETED
    },
    reasonVisibleThreshold: Int = DEFAULT_REASON_VISIBLE_THRESHOLD,
    reasonDisplayMode: IssueReliabilityReasonDisplayMode = IssueReliabilityReasonDisplayMode.INLINE,
) {
    val style = type.toStyle()
    var isReasonDialogVisible by remember { mutableStateOf(false) }
    val displayScore = score?.coerceIn(0, 100)
    val displayText = when (status) {
        IssueReliabilityStatus.PENDING -> "검사중"
        IssueReliabilityStatus.COMPLETED -> displayScore?.let { "$it%" } ?: "검사중"
        IssueReliabilityStatus.FAILED -> "fail"
    }
    val indicatorColor = when (status) {
        IssueReliabilityStatus.PENDING -> Gray_5
        IssueReliabilityStatus.COMPLETED -> displayScore?.let { getReliabilityColor(it) } ?: Gray_5
        IssueReliabilityStatus.FAILED -> Orange
    }
    val progress = when (status) {
        IssueReliabilityStatus.COMPLETED -> displayScore?.div(100f)
        IssueReliabilityStatus.FAILED -> 0f
        IssueReliabilityStatus.PENDING -> null
    }
    val hasCompletedReason = status == IssueReliabilityStatus.COMPLETED &&
        displayScore != null &&
        !reason.isNullOrBlank()
    val showInlineReason = hasCompletedReason &&
        displayScore < reasonVisibleThreshold &&
        reasonDisplayMode == IssueReliabilityReasonDisplayMode.INLINE
    val isModalReasonEnabled = hasCompletedReason &&
        reasonDisplayMode == IssueReliabilityReasonDisplayMode.MODAL

    Column(
        modifier = Modifier.fillMaxWidth().then(modifier),
        horizontalAlignment = Alignment.Start,
    ) {
        Column(
            modifier = Modifier
                .width(style.barWidth)
                .then(
                    if (isModalReasonEnabled) {
                        Modifier.clickable { isReasonDialogVisible = true }
                    } else {
                        Modifier
                    },
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "AI 신뢰도",
                    style = IssueTypo.Bold12.copy(color = Title, fontSize = style.labelFontSize),
                )
                Text(
                    text = displayText,
                    style = IssueTypo.Bold12.copy(
                        color = indicatorColor,
                        fontSize = style.labelFontSize,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(style.labelBarSpacing))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(style.barHeight)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Gray_2),
            ) {
                if (progress != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(999.dp))
                            .background(indicatorColor),
                    )
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(0.33f))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(White.copy(alpha = 0.5f)),
                    )
                    Spacer(modifier = Modifier.weight(0.33f))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(White.copy(alpha = 0.5f)),
                    )
                    Spacer(modifier = Modifier.weight(0.34f))
                }
            }
        }

        if (showInlineReason) {
            Spacer(modifier = Modifier.height(style.reasonTopSpacing))
            Text(
                text = reason.orEmpty(),
                modifier = Modifier.fillMaxWidth(),
                style = IssueTypo.Regular12.copy(
                    color = Gray_6,
                    fontSize = style.reasonFontSize,
                    lineHeight = style.reasonLineHeight,
                ),
                textAlign = TextAlign.Start,
            )
        }
    }

    if (isReasonDialogVisible) {
        AlertDialog(
            onDismissRequest = { isReasonDialogVisible = false },
            title = {
                Text(
                    text = "AI 신뢰도 평가",
                    style = IssueTypo.Bold12.copy(color = Title, fontSize = 16.sp),
                )
            },
            text = {
                Text(
                    text = reason.orEmpty(),
                    style = IssueTypo.Regular12.copy(
                        color = Gray_6,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = { isReasonDialogVisible = false }) {
                    Text(
                        text = "확인",
                        style = IssueTypo.Bold12.copy(color = Success, fontSize = 14.sp),
                    )
                }
            },
            containerColor = White,
        )
    }
}

private fun getReliabilityColor(score: Int): Color {
    val percentage = score.coerceIn(0, 100)
    return when {
        percentage <= 33 -> Issue
        percentage <= 66 -> Shop
        else -> Success
    }
}

@Preview(name = "핀 - 신뢰도 높음", showBackground = true)
@Composable
private fun IssueReliabilityIndicatorPinHighPreview() {
    IssueReliabilityIndicator(
        score = 90,
        reason = null,
        type = IssueReliabilityType.PIN,
    )
}

@Preview(name = "핀 - 신뢰도 낮음", showBackground = true)
@Composable
private fun IssueReliabilityIndicatorPinLowPreview() {
    IssueReliabilityIndicator(
        score = 20,
        reason = "출처가 불분명하며 허위 정보일 가능성이 있습니다.",
        type = IssueReliabilityType.PIN,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(name = "커뮤니티 - 신뢰도 검사중", showBackground = true)
@Composable
private fun IssueReliabilityIndicatorCommunityLoadingPreview() {
    IssueReliabilityIndicator(
        score = null,
        reason = null,
        type = IssueReliabilityType.COMMUNITY,
    )
}

@Preview(name = "핀 - 신뢰도 평가 실패", showBackground = true)
@Composable
private fun IssueReliabilityIndicatorPinFailedPreview() {
    IssueReliabilityIndicator(
        score = 0,
        reason = "- 지금은 이 제보에 대한 AI 검토 결과를 불러오지 못했어요.\n- 잠시 후 다시 열어보세요.",
        status = IssueReliabilityStatus.FAILED,
        type = IssueReliabilityType.PIN,
        modifier = Modifier.fillMaxWidth(),
    )
}
