package com.issueissyu.fe.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.White


@Composable
fun ProfileImageFrame(
    size: Dp,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String = "프로필 사진",
    borderWidth: Dp = 3.dp,
    borderColor: Color = White.copy(alpha = 0.7f),
    imageScale: Float = 1.1f,
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
        val defaultCharacterPainter = painterResource(R.drawable.ic_character_default)
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                modifier = Modifier
                    .matchParentSize()
                    .scale(imageScale),
                contentScale = ContentScale.Crop,
                alignment = contentAlignment,
                placeholder = defaultCharacterPainter,
                error = defaultCharacterPainter,
            )
        } else {
            ProfileImagePlaceholder(
                contentDescription = contentDescription,
                imageScale = imageScale,
                contentAlignment = contentAlignment,
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(borderWidth, borderColor, CircleShape),
        )
    }
}

@Composable
private fun ProfileImagePlaceholder(
    contentDescription: String,
    imageScale: Float,
    contentAlignment: Alignment,
) {
    Image(
        painter = painterResource(R.drawable.ic_character_default),
        contentDescription = contentDescription,
        modifier = Modifier
            .fillMaxSize()
            .scale(imageScale),
        contentScale = ContentScale.Crop,
        alignment = contentAlignment,
    )
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
