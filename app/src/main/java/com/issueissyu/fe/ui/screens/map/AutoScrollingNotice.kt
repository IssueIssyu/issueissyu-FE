package com.issueissyu.fe.ui.screens.map

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.IssueTypo
import kotlinx.coroutines.delay

data class NoticeUiModel(
    val id: String,
    val title: String,
    val pinId: String? = null
)

@Composable
fun AutoScrollingNotice(
    notices: List<NoticeUiModel>,
    iconResId: Int,
    onClick: (NoticeUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (notices.isEmpty()) return

    var currentNoticeIndex by remember(notices) { mutableIntStateOf(0) }

    LaunchedEffect(key1 = notices) {
        if (notices.size > 1) {
            while (true) {
                delay(5000)
                currentNoticeIndex = (currentNoticeIndex + 1) % notices.size
            }
        }
    }

    val currentNotice = notices.getOrNull(currentNoticeIndex) ?: return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Gray_7.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick(currentNotice) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = "공지",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(32.dp)
                .padding(end = 8.dp)
        )

        AnimatedContent(
            targetState = currentNoticeIndex,
            transitionSpec = {
                (
                        slideInVertically(
                            animationSpec = tween(durationMillis = 300)
                        ) { height -> height } + fadeIn(
                            animationSpec = tween(durationMillis = 300)
                        )
                        ).togetherWith(
                        slideOutVertically(
                            animationSpec = tween(durationMillis = 300)
                        ) { height -> -height } + fadeOut(
                            animationSpec = tween(durationMillis = 300)
                        )
                    )
            },
            label = "Notice Animation"
        ) { targetIndex ->
            val notice = notices.getOrNull(targetIndex) ?: return@AnimatedContent

            Text(
                text = notice.title,
                style = IssueTypo.Regular15.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
        }
    }
}
