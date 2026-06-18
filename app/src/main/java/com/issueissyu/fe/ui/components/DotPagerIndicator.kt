package com.issueissyu.fe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_3

@Composable
fun DotPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (isSelected) 7.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) BrandColor else Gray_3),
            )
        }
    }
}
