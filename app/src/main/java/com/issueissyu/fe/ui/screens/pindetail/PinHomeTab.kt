package com.issueissyu.fe.ui.screens.pindetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.issueissyu.fe.R
import com.issueissyu.fe.data.model.CommunicationPinDetail
import com.issueissyu.fe.data.model.IssuePinDetail
import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.data.model.ResolutionStatus
import com.issueissyu.fe.data.model.canEditBy
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueTypo
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
    // writer는 Pin 공통 필드가 아니다. detail 타입 기준으로 가져온다.
    val writer = when (val detail = pin.detail) {
        is IssuePinDetail -> detail.writer
        is CommunicationPinDetail -> detail.writer
        else -> null
    }
    val isAuthor = writer?.id == currentUserId
    val canEdit = pin.canEditBy(currentUserId)
    val issueDetail = pin.detail as? IssuePinDetail

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 20.dp)
    ) {
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

            // TODO: EditButton/CommonButton과 공통화 검토.
            //       현재 Button.kt의 EditButton은 (작성자 + 수정 가능)일 때만 [수정+삭제]를 묶어 보여주고,
            //       (작성자 + 수정 불가)에서 [삭제만] 노출하는 케이스를 다루지 않아 여기서는 로컬 버튼 Row로 처리.
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

        Spacer(modifier = Modifier.height(16.dp))

        if (writer != null || pin.communityPostId != null) {
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

            Spacer(modifier = Modifier.height(16.dp))
        }

        HorizontalDivider(color = Gray_3, thickness = 1.dp)

        Spacer(modifier = Modifier.height(16.dp))

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

@Composable
private fun ResolutionStatusBadge(
    resolutionStatus: ResolutionStatus
) {
    val (label, backgroundColor) = when (resolutionStatus) {
        ResolutionStatus.BEFORE_RESOLUTION -> "해결 전" to Issue
        ResolutionStatus.IN_PROGRESS -> "해결 중" to Orange
        ResolutionStatus.RESOLVED -> "해결 완료" to BrandColor
    }
    Text(
        text = label,
        style = IssueTypo.Bold12.copy(color = White),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
                    onClick = onEditClick
                )
            }
            // TODO: 삭제 정책 확정 후 canDelete 조건을 별도로 분리.
            //       현재는 작성자 본인에게만 노출하고 실제 삭제 동작은 연결하지 않는다.
            CircleActionIcon(
                iconRes = R.drawable.ic_delete,
                contentDescription = "삭제",
                onClick = onDeleteClick
            )
        } else {
            CircleActionIcon(
                iconRes = R.drawable.ic_report,
                contentDescription = "신고",
                onClick = onReportClick
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
    containerColor: Color = White
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 3.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.56f)
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
            .background(BrandColor)
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
