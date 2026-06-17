package com.issueissyu.fe.ui.screens.map

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clickable { onClick(currentNotice) },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .padding(start = 24.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(18.dp),
                    clip = false
                )
                .background(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(start = 36.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    style = IssueTypo.Bold18.copy(color = Color.Black),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(
                    elevation = 5.dp,
                    shape = CircleShape,
                    clip = false
                )
                .background(
                    color = Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = "공지",
                tint = Color.Unspecified,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
