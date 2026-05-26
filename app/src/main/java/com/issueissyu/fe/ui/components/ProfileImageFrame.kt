package com.issueissyu.fe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.White

/**
 * 원형 프로필 이미지 틀.
 *
 * @param size 이미지 지름. 테두리는 원 내부에 그려져 실제 차지 크기는 `size`입니다.
 */
@Composable
fun ProfileImageFrame(
    size: Dp,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String = "프로필 사진",
    borderWidth: Dp = 3.dp,
    borderColor: Color = White.copy(alpha = 0.7f),
    imageScale: Float = 1.2f,
    contentAlignment: Alignment = BiasAlignment(
        horizontalBias = 0f,
        verticalBias = -0.8f,
    ),
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                modifier = Modifier
                    .matchParentSize()
                    .scale(imageScale),
                contentScale = ContentScale.Crop,
                alignment = contentAlignment,
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Gray_3, CircleShape),
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(borderWidth, borderColor, CircleShape),
        )
    }
}

@Preview(showBackground = true, name = "ProfileImageFrame · sizes")
@Composable
private fun ProfileImageFramePreview_Sizes() {
    IssueissyuTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProfileImageFrame(size = 40.dp, imageUrl = null)
                ProfileImageFrame(size = 56.dp, imageUrl = null)
                ProfileImageFrame(size = 72.dp, imageUrl = null)
            }
            ProfileImageFrame(size = 100.dp, imageUrl = null)
            ProfileImageFrame(size = 66.dp, imageUrl = null)
        }
    }
}

@Preview(showBackground = true, name = "ProfileImageFrame · pin post sizes")
@Composable
private fun ProfileImageFramePreview_PinPost() {
    IssueissyuTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileImageFrame(size = 66.dp, imageUrl = null)
            ProfileImageFrame(size = 25.dp, imageUrl = null)
        }
    }
}
