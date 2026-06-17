package com.issueissyu.fe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.issueissyu.fe.ui.theme.Gray_4
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Orange
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White

object IssueAiDraftDefaults {
    const val DEFAULT_TONE = "없음"
    val FALLBACK_TONE_OPTIONS = listOf(
        "없음",
        "한줄요약형",
        "상황설명형",
        "개선요청형",
        "긴급요청형",
        "불편호소형",
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IssueToneSelectionSection(
    toneOptions: List<String>,
    isLoading: Boolean,
    selectedTone: String?,
    onToneChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
) {
    Column(modifier = modifier) {
        if (showTitle) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "말투 설정",
                    style = IssueTypo.Bold18.copy(color = Title),
                )
            }
        }
        if (isLoading && toneOptions.isEmpty()) {
            Text(
                text = "말투 목록 불러오는 중…",
                style = IssueTypo.Regular12.copy(color = Gray_4),
            )
            return@Column
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            toneOptions.forEach { tone ->
                IssueToneChip(
                    label = tone,
                    selected = selectedTone == tone,
                    onClick = { onToneChange(tone) },
                )
            }
        }
    }
}

@Composable
fun IssueAiDraftButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    loadingText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CommonButton(
        onClick = onClick,
        text = if (isLoading) loadingText else "AI 글쓰기",
        isEnabled = isEnabled,
        textStyle = IssueTypo.Bold18.copy(fontSize = 16.sp),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun IssueToneChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) Orange else White
    val borderColor = if (selected) Orange else Gray_4
    val textColor = if (selected) White else Title

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = IssueTypo.Regular15.copy(color = textColor),
        )
    }
}
