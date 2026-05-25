package com.issueissyu.fe.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.Gray_2
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Orange
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White

private const val DEFAULT_REASON_VISIBLE_THRESHOLD = 70

@Composable
fun IssueReliabilityIndicator(
    score: Int?,
    reason: String?,
    modifier: Modifier = Modifier.width(100.dp),
    reasonVisibleThreshold: Int = DEFAULT_REASON_VISIBLE_THRESHOLD,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI 신뢰도",
                style = IssueTypo.Bold12.copy(color = Title, fontSize = 10.sp)
            )
            Text(
                text = if (score != null) "$score%" else "검사중",
                style = IssueTypo.Bold12.copy(
                    color = if (score != null) getReliabilityColor(score) else Gray_5,
                    fontSize = 10.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Gray_2)
        ) {
            if (score != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(score.coerceIn(0, 100) / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(getReliabilityColor(score))
                )
            }

            Row(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.weight(0.33f))
                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(White.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.weight(0.33f))
                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(White.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.weight(0.34f))
            }
        }

        if (score != null &&
            score < reasonVisibleThreshold &&
            !reason.isNullOrBlank()
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reason,
                style = IssueTypo.Regular12.copy(color = Gray_6, fontSize = 10.sp),
                textAlign = TextAlign.End,
                lineHeight = 14.sp
            )
        }
    }
}

private fun getReliabilityColor(score: Int): Color {
    val percentage = score.coerceIn(0, 100)
    return when {
        percentage <= 33 -> Orange
        percentage <= 66 -> Festival
        else -> BrandColor
    }
}

@Preview(name = "신뢰도 높음", showBackground = true)
@Composable
private fun IssueReliabilityIndicatorHighPreview() {
    IssueReliabilityIndicator(score = 90, reason = null)
}

@Preview(name = "신뢰도 낮음", showBackground = true)
@Composable
private fun IssueReliabilityIndicatorLowPreview() {
    IssueReliabilityIndicator(
        score = 20,
        reason = "출처가 불분명하며 허위 정보일 가능성이 있습니다."
    )
}

@Preview(name = "신뢰도 중간", showBackground = true)
@Composable
private fun IssueReliabilityIndicatorMediumPreview() {
    IssueReliabilityIndicator(score = 55, reason = null)
}

@Preview(name = "신뢰도 검사중", showBackground = true)
@Composable
private fun IssueReliabilityIndicatorLoadingPreview() {
    IssueReliabilityIndicator(score = null, reason = null)
}
