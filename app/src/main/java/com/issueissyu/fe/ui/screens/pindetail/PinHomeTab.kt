package com.issueissyu.fe.ui.screens.pindetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.issueissyu.fe.R
import com.issueissyu.fe.domain.model.CommunicationPinDetail
import com.issueissyu.fe.domain.model.IssuePinDetail
import com.issueissyu.fe.domain.model.Pin
import com.issueissyu.fe.domain.model.ResolutionStatus
import com.issueissyu.fe.domain.model.canEditBy
import com.issueissyu.fe.data.sample.PinSamples
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Orange
import com.issueissyu.fe.ui.theme.Text as TextColor
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PinHomeTab(
    pin: Pin,
    currentUserId: String,
    onReportClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onCommunityClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val writer = when (val detail = pin.detail) {
        is IssuePinDetail -> detail.writer
        is CommunicationPinDetail -> detail.writer
        else -> null
    }
    val isAuthor = writer?.id == currentUserId
    val canEdit = pin.canEditBy(currentUserId)
    val issueDetail = pin.detail as? IssuePinDetail

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp)) {
            if (issueDetail != null) {
                ResolutionStatusBadge(resolutionStatus = issueDetail.resolutionStatus)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pin.title,
                    style = IssueTypo.Bold18.copy(color = Title),
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))

                // TODO: 공통 EditButton과 통합 검토 (작성자+수정 불가에서 삭제만 노출하는 케이스 미지원)
                PinDetailActionButtons(
                    isAuthor = isAuthor,
                    canEdit = canEdit,
                    onReportClick = { onReportClick(pin.id) },
                    onEditClick = { onEditClick(pin.id) },
                    onDeleteClick = { onDeleteClick(pin.id) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pin.locationName?.takeIf { it.isNotBlank() } ?: pin.address,
                style = IssueTypo.Regular15.copy(color = Gray_7),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = listOf(
                    formatCreatedAt(pin.createdAt),
                    "조회 ${pin.viewCount}",
                    "공감 ${pin.sympathyCount}"
                ).joinToString(" · "),
                style = IssueTypo.Regular12.copy(color = Gray_6)
            )

            if (writer != null || pin.communityPostId != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (writer != null) {
                        WriterAvatar(imageUrl = writer.imageUrl)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = writer.name,
                            style = IssueTypo.Bold12.copy(color = Title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (pin.communityPostId != null) {
                        CommunityChip(
                            onClick = { onCommunityClick(pin.communityPostId) }
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Gray_3, thickness = 1.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 16.dp)
        ) {
            Text(
                text = pin.description,
                style = IssueTypo.Regular15.copy(color = TextColor),
                lineHeight = 22.sp
            )

            if (pin.imageUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    pin.imageUrls.forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "핀 이미지",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 320.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Gray_3),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ResolutionStatusBadge(
    resolutionStatus: ResolutionStatus
) {
    val (label, backgroundColor) = when (resolutionStatus) {
        ResolutionStatus.BEFORE_RESOLUTION -> "해결 전" to Title
        ResolutionStatus.IN_PROGRESS -> "해결 중" to Orange
        ResolutionStatus.RESOLVED -> "해결 완료" to BrandColor
    }
    Text(
        text = label,
        style = IssueTypo.Bold12.copy(color = White),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun PinDetailActionButtons(
    isAuthor: Boolean,
    canEdit: Boolean,
    onReportClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isAuthor) {
            if (canEdit) {
                CircleActionIcon(
                    iconRes = R.drawable.ic_edit,
                    contentDescription = "수정",
                    onClick = onEditClick,
                    iconTint = Gray_6
                )
            }
            // TODO: 삭제 가능 조건은 서버 정책 확정 후 canDeleteBy 또는 권한 응답값으로 분리
            CircleActionIcon(
                iconRes = R.drawable.ic_delete,
                contentDescription = "삭제",
                onClick = onDeleteClick,
                iconTint = Gray_6
            )
        } else {
            CircleActionIcon(
                iconRes = R.drawable.ic_report,
                contentDescription = "신고",
                onClick = onReportClick,
                iconTint = Orange
            )
        }
    }
}

@Composable
private fun CircleActionIcon(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconTint: Color? = null,
    containerColor: Color = White
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 2.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(CircleShape)
            .background(containerColor)
            .border(width = 1.dp, color = Gray_3, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.5f),
            colorFilter = iconTint?.let { ColorFilter.tint(it) }
        )
    }
}

@Composable
private fun WriterAvatar(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    val avatarModifier = modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(Gray_3)

    if (imageUrl.isNullOrBlank()) {
        Box(modifier = avatarModifier)
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = "작성자 프로필",
            modifier = avatarModifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun CommunityChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Orange)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "커뮤니티 >",
            style = IssueTypo.Bold12.copy(color = White)
        )
    }
}

private fun formatCreatedAt(raw: String): String {
    val pattern = DateTimeFormatter.ofPattern("MM.dd HH:mm")
    return runCatching {
        OffsetDateTime.parse(raw).format(pattern)
    }.getOrElse {
        runCatching {
            Instant.parse(raw)
                .atZone(ZoneId.systemDefault())
                .format(pattern)
        }.getOrDefault(raw)
    }
}

@Preview(name = "Issue · 다른 사용자", showBackground = true, heightDp = 800)
@Composable
private fun PinHomeTabPreview_IssueOtherUser() {
    IssueissyuTheme {
        PinHomeTab(
            pin = PinSamples.findById(PinSamples.IssuePinId),
            currentUserId = PinSamples.user2.id,
            onReportClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onCommunityClick = {}
        )
    }
}

@Preview(name = "Issue · 작성자 본인", showBackground = true, heightDp = 800)
@Composable
private fun PinHomeTabPreview_IssueAuthor() {
    IssueissyuTheme {
        PinHomeTab(
            pin = PinSamples.findById(PinSamples.IssuePinId),
            currentUserId = PinSamples.user1.id,
            onReportClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onCommunityClick = {}
        )
    }
}

@Preview(name = "Communication · 커뮤니티 포스트 있음", showBackground = true, heightDp = 800)
@Composable
private fun PinHomeTabPreview_CommunicationWithCommunity() {
    IssueissyuTheme {
        PinHomeTab(
            pin = PinSamples.findById(PinSamples.CommunicationPinId),
            currentUserId = PinSamples.user2.id,
            onReportClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onCommunityClick = {}
        )
    }
}

@Preview(name = "Shop · 작성자 없음", showBackground = true, heightDp = 800)
@Composable
private fun PinHomeTabPreview_Shop() {
    IssueissyuTheme {
        PinHomeTab(
            pin = PinSamples.findById(PinSamples.ShopPinId),
            currentUserId = PinSamples.user2.id,
            onReportClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onCommunityClick = {}
        )
    }
}
