package com.issueissyu.fe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White

@Composable
private fun DialogCard(
    onDismissRequest: () -> Unit,
    buttons: @Composable () -> Unit,
    title: String,
    message: String,
    titleColor: Color = Title,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 타이틀
                Text(
                    text = title,
                    style = IssueTypo.Bold18.copy(color = titleColor)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 메시지
                Text(
                    text = message,
                    style = IssueTypo.Regular16.copy(color = Text),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                buttons()
            }
        }
    }
}

@Composable
private fun DialogConfirmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = BrandColor,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        Text(
            text = text,
            style = IssueTypo.Bold18.copy(color = White)
        )
    }
}

@Composable
fun Dialog(
    title: String,
    message: String,
    confirmText: String = "확인",
    dismissText: String = "취소",
    isWarning: Boolean = false, // 경고성 여부 (회원탈퇴 등)
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DialogCard(
        onDismissRequest = onDismiss,
        title = title,
        message = message,
        titleColor = if (isWarning) Issue else Title,
        buttons = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gray_3
                    )
                ) {
                    Text(
                        text = dismissText,
                        style = IssueTypo.Bold18.copy(color = Gray_5)
                    )
                }

                DialogConfirmButton(
                    text = confirmText,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    containerColor = if (isWarning) Issue else BrandColor,
                )
            }
        },
    )
}

@Composable
fun InfoDialog(
    title: String,
    message: String,
    confirmText: String = "확인",
    onConfirm: () -> Unit,
) {
    DialogCard(
        onDismissRequest = onConfirm,
        title = title,
        message = message,
        buttons = {
            DialogConfirmButton(
                text = confirmText,
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
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
fun PreviewInfoDialog() {
    InfoDialog(
        title = "안내",
        message = "로그아웃에 실패했습니다.",
        onConfirm = {},
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewIssueDialog() {
    Dialog(
        title = "회원탈퇴",
        message = "정말로 탈퇴하시겠습니까?\n탈퇴 후에는 복구할 수 없습니다.\n할래말래\n\n할래말래\n할래말래\n할래말래",
        onDismiss = {},
        onConfirm = {}
    )
}