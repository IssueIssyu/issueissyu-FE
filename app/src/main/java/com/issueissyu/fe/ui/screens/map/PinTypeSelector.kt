package com.issueissyu.fe.ui.screens.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.theme.CommunicationContainerLight
import com.issueissyu.fe.ui.theme.IssueContainer
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White

@Composable
fun PinTypeSelector(
    onIssueClick: () -> Unit,
    onCommunicationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fontScale = LocalDensity.current.fontScale.coerceIn(1f, 1.3f)
    val containerWidth = (85f * fontScale).dp

    Column(
        modifier = modifier
            .width(containerWidth)
            .clip(RoundedCornerShape(30.dp))
            .background(White)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PinTypeSelectorItem(
            iconResId = R.drawable.ic_issue,
            label = "이슈 생성",
            backgroundColor = IssueContainer,
            onClick = onIssueClick
        )

        PinTypeSelectorItem(
            iconResId = R.drawable.communicate,
            label = "소통 생성",
            backgroundColor = CommunicationContainerLight,
            onClick = onCommunicationClick
        )
    }
}

@Composable
private fun PinTypeSelectorItem(
    iconResId: Int,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(start = 6.dp, top = 8.dp, end = 6.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(backgroundColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            style = IssueTypo.Bold12.copy(color = Title),
            textAlign = TextAlign.Center,
            maxLines = 2,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPinTypeSelector() {
    PinTypeSelector(
        onIssueClick = {},
        onCommunicationClick = {}
    )
}
