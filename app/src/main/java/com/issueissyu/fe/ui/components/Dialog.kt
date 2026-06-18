package com.issueissyu.fe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White

@Composable
fun Dialog(
    title: String,
    message: String,
    confirmText: String = "확인",
    dismissText: String = "취소",
    isWarning: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val messageMaxHeight = (configuration.screenHeightDp * 0.25f).dp
    val dialogMaxWidth = (configuration.screenWidthDp * 0.92f).dp

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .widthIn(max = dialogMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            Column(
                modifier = Modifier
                    .background(White)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    style = IssueTypo.Bold18.copy(
                        color = if (isWarning) Issue else Title,
                    ),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = messageMaxHeight)
                        .verticalScroll(rememberScrollState()),
                    style = IssueTypo.Regular16.copy(color = Text),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DialogActionButton(
                        text = dismissText,
                        onClick = onDismiss,
                        containerColor = Gray_3,
                        textColor = Gray_5,
                        modifier = Modifier.weight(1f),
                    )
                    DialogActionButton(
                        text = confirmText,
                        onClick = onConfirm,
                        containerColor = if (isWarning) Issue else BrandColor,
                        textColor = White,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
fun RemainingQuotaDialog(
    title: String,
    countLabel: String?,
    description: String,
    confirmText: String = "확인",
    dismissText: String = "취소",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .background(White)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = IssueTypo.Bold18.copy(color = Title),
                    textAlign = TextAlign.Center,
                )

                if (!countLabel.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = countLabel,
                        style = IssueTypo.ExtraBold30.copy(color = Title),
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = description,
                    style = IssueTypo.Regular15.copy(color = Title, lineHeight = 22.sp),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gray_3),
                    ) {
                        Text(
                            text = dismissText,
                            style = IssueTypo.Bold18.copy(color = Gray_5),
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandColor),
                    ) {
                        Text(
                            text = confirmText,
                            style = IssueTypo.Bold18.copy(color = White),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogActionButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
    ) {
        DialogButtonText(
            text = text,
            color = textColor,
        )
    }
}

@Composable
private fun DialogButtonText(
    text: String,
    color: Color,
) {
    val baseStyle = IssueTypo.Bold18.copy(color = color)
    var fontSize by remember(text, color) { mutableStateOf(baseStyle.fontSize) }
    var readyToDraw by remember(text, color) { mutableStateOf(true) }

    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .drawWithContent { if (readyToDraw) drawContent() },
        style = baseStyle.copy(fontSize = fontSize),
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
        onTextLayout = { result ->
            if (result.didOverflowWidth && fontSize.value > 10f) {
                readyToDraw = false
                fontSize = (fontSize.value * 0.92f).sp
            } else {
                readyToDraw = true
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLogoutDialog() {
    Dialog(
        title = "로그아웃",
        message = "정말 로그아웃 하시겠어요?\n언제든지 다시 돌아올 수 있어요!",
        confirmText = "로그아웃",
        onDismiss = {},
        onConfirm = {},
    )
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
fun PreviewLogoutDialogLargeFont() {
    Dialog(
        title = "로그아웃",
        message = "정말 로그아웃 하시겠어요?\n언제든지 다시 돌아올 수 있어요!",
        confirmText = "로그아웃",
        onDismiss = {},
        onConfirm = {},
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewRemainingQuotaDialog() {
    RemainingQuotaDialog(
        title = "남은 이슈 핀 수정 횟수",
        countLabel = "2/3",
        description = "이슈 핀 수정은 정해진 일일 한도 내에서만 사용 가능합니다!",
        onDismiss = {},
        onConfirm = {},
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewIssueDialog() {
    Dialog(
        title = "회원탈퇴",
        message = "정말로 탈퇴하시겠습니까?\n탈퇴 후에는 복구할 수 없습니다.\n할래말래\n\n할래말래\n할래말래\n할래말래",
        confirmText = "탈퇴하기",
        isWarning = true,
        onDismiss = {},
        onConfirm = {},
    )
}
