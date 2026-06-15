package com.issueissyu.fe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.White

@Composable
fun CompactSympathyButton(
    sympathyCount: Int,
    isSympathizedByMe: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 24.dp,
    horizontalPadding: Dp = 8.dp,
    iconSize: Dp = 13.dp,
) {
    val backgroundColor = if (isSympathizedByMe) BrandColor else White
    val contentColor = if (isSympathizedByMe) White else BrandColor

    Row(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(backgroundColor)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ThumbUp,
            contentDescription = "공감",
            tint = contentColor,
            modifier = Modifier.size(iconSize)
        )

        Spacer(modifier = Modifier.width(3.dp))

        Text(
            text = "$sympathyCount",
            style = IssueTypo.Regular12.copy(color = contentColor),
            maxLines = 1
        )
    }
}
