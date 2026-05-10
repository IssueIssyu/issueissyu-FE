package com.issueissyu.fe.ui.screens.pindetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.issueissyu.fe.R
import com.issueissyu.fe.data.model.IssuePinDetail
import com.issueissyu.fe.data.model.IssueResolverParticipation
import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.data.model.PinUser
import com.issueissyu.fe.data.model.ResolutionStatus
import com.issueissyu.fe.ui.components.ActionState
import com.issueissyu.fe.ui.components.GoNowButton
import com.issueissyu.fe.ui.components.SignButton
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueContainerLight
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
fun PinResolutionTab(
    pin: Pin,
    issueDetail: IssuePinDetail,
    currentUserId: String,
    onGoNowClick: (String) -> Unit,
    onPetitionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isResolved = issueDetail.resolutionStatus == ResolutionStatus.RESOLVED
    val isWriter = issueDetail.writer.id == currentUserId
    val myParticipation = issueDetail.resolverParticipations
        .firstOrNull { it.user.id == currentUserId }
    val isAlreadyResolver = myParticipation != null

    // 시민해결사 상호작용은 RESOLVED 상태에서만 막는다.
    // 작성자 본인은 자기 핀에 지금가요를 누르지 않는 정책이라 비활성화 처리한다.
    val canGoNow = !isResolved && !isAlreadyResolver && !isWriter

    val goNowState = when {
        myParticipation?.isConfirmedByWriter == true -> ActionState.DONE
        myParticipation != null -> ActionState.MOVING
        else -> ActionState.DEFAULT
    }

    val targetCount = issueDetail.petitionTargetCount
    val petitionProgress = if (targetCount != null && targetCount > 0) {
        (issueDetail.petitionCount / targetCount.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val remainingCount = if (targetCount != null) {
        (targetCount - issueDetail.petitionCount).coerceAtLeast(0)
    } else {
        null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ResolutionStatusSection(
            resolutionStatus = issueDetail.resolutionStatus,
            resolvedAt = issueDetail.resolvedAt
        )

        ResolverParticipationSection(
            participations = issueDetail.resolverParticipations,
            resolvedBy = issueDetail.resolvedBy,
            currentUserId = currentUserId
        )

        PetitionStatusSection(
            petitionCount = issueDetail.petitionCount,
            targetCount = targetCount,
            remainingCount = remainingCount,
            progress = petitionProgress
        )

        ResolutionGuideSection()

        ResolutionActionRow(
            pinId = pin.id,
            canGoNow = canGoNow,
            goNowState = goNowState,
            isWriter = isWriter,
            onGoNowClick = onGoNowClick,
            petitionCount = issueDetail.petitionCount,
            isPetitionedByMe = issueDetail.isPetitionedByMe,
            onPetitionClick = onPetitionClick
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = IssueTypo.Bold18.copy(color = Title)
    )
}

@Composable
private fun ResolutionStatusSection(
    resolutionStatus: ResolutionStatus,
    resolvedAt: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("해결 상태")
        Row(verticalAlignment = Alignment.CenterVertically) {
            ResolutionStatusBadge(resolutionStatus = resolutionStatus)
            if (resolutionStatus == ResolutionStatus.RESOLVED && !resolvedAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${formatTimestamp(resolvedAt)} 해결",
                    style = IssueTypo.Regular12.copy(color = Gray_6)
                )
            }
        }
    }
}

@Composable
private fun ResolverParticipationSection(
    participations: List<IssueResolverParticipation>,
    resolvedBy: PinUser?,
    currentUserId: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("시민해결사 참여 현황 (${participations.size}명)")
        if (participations.isEmpty()) {
            Text(
                text = "아직 참여한 시민해결사가 없습니다.",
                style = IssueTypo.Regular15.copy(color = Gray_6)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                participations.forEach { participation ->
                    ResolverParticipationItem(
                        participation = participation,
                        isFinalResolver = resolvedBy?.id == participation.user.id,
                        isMe = participation.user.id == currentUserId
                    )
                }
            }
        }
        // 방어: resolvedBy가 어떤 이유로 participations에 포함되지 않은 경우에도 최종 해결자는 표시한다.
        if (resolvedBy != null && participations.none { it.user.id == resolvedBy.id }) {
            ResolvedByHighlight(user = resolvedBy)
        }
    }
}

@Composable
private fun ResolverParticipationItem(
    participation: IssueResolverParticipation,
    isFinalResolver: Boolean,
    isMe: Boolean
) {
    val statusLabel = when {
        participation.isConfirmedByWriter -> "작성자 확인 완료"
        participation.proofImageUrls.isNotEmpty() -> "인증 사진 제출"
        else -> "인증 대기"
    }

    val backgroundColor = if (isFinalResolver) IssueContainerLight else Gray_3.copy(alpha = 0.3f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(imageUrl = participation.user.imageUrl)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = participation.user.name + if (isMe) " (나)" else "",
                    style = IssueTypo.Bold12.copy(color = Title),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isFinalResolver) {
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusPill(
                        text = "최종 해결자",
                        backgroundColor = BrandColor,
                        contentColor = White
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = statusLabel,
                style = IssueTypo.Regular12.copy(color = Gray_6)
            )
        }
    }
}

@Composable
private fun ResolvedByHighlight(user: PinUser) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(IssueContainerLight)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(imageUrl = user.imageUrl)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "${user.name} · 최종 해결자",
            style = IssueTypo.Bold12.copy(color = Title),
            modifier = Modifier.weight(1f)
        )
        StatusPill(
            text = "최종 해결자",
            backgroundColor = BrandColor,
            contentColor = White
        )
    }
}

@Composable
private fun PetitionStatusSection(
    petitionCount: Int,
    targetCount: Int?,
    remainingCount: Int?,
    progress: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader("청원 현황")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "참여 ${petitionCount}명",
                style = IssueTypo.Bold12.copy(color = Title)
            )
            if (targetCount != null && remainingCount != null) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "민원 메일까지 ${remainingCount}명 남음",
                    style = IssueTypo.Regular12.copy(color = Gray_6)
                )
            }
        }
        if (targetCount != null && targetCount > 0) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Orange,
                trackColor = Gray_3
            )
        }
    }
}

@Composable
private fun ResolutionGuideSection() {
    val guides = listOf(
        "지금가요를 누르면 시민해결사로 참여할 수 있습니다.",
        "참여 후 해결 인증 사진을 제출할 수 있습니다.",
        "작성자가 인증을 확인하면 해결 완료 상태가 됩니다.",
        "청원은 해결 상태와 별개로 참여할 수 있습니다."
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader("진행 방식")
        guides.forEach { line ->
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "•",
                    style = IssueTypo.Regular15.copy(color = Gray_6)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = line,
                    style = IssueTypo.Regular15.copy(color = TextColor),
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun ResolutionActionRow(
    pinId: String,
    canGoNow: Boolean,
    goNowState: ActionState,
    isWriter: Boolean,
    onGoNowClick: (String) -> Unit,
    petitionCount: Int,
    isPetitionedByMe: Boolean,
    onPetitionClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 작성자 본인은 지금가요 버튼을 숨겨 노출 자체를 차단한다.
        if (!isWriter) {
            ResolutionGoNowButton(
                canGoNow = canGoNow,
                goNowState = goNowState,
                onClick = { onGoNowClick(pinId) },
                modifier = Modifier.weight(1f)
            )
        }
        // 청원은 시민해결사 흐름과 별개라 RESOLVED 여부와 무관하게 노출한다.
        // 중복 청원 차단은 SignButton 내부 isSigned로 처리.
        SignButton(
            isSigned = isPetitionedByMe,
            count = petitionCount,
            onClick = { onPetitionClick(pinId) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ResolutionGoNowButton(
    canGoNow: Boolean,
    goNowState: ActionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 공통 GoNowButton은 enabled를 외부에서 강제할 수 없고 state==DEFAULT일 때만 활성화된다.
    // RESOLVED + 비참여자처럼 시각적으로 disabled DEFAULT가 필요한 케이스만 로컬 fallback으로 처리한다.
    // TODO: 공통 GoNowButton에 외부 enabled 파라미터 노출 후 통합 검토.
    if (!canGoNow && goNowState == ActionState.DEFAULT) {
        DisabledGoNowFallback(modifier = modifier)
    } else {
        GoNowButton(state = goNowState, onClick = onClick, modifier = modifier)
    }
}

@Composable
private fun DisabledGoNowFallback(modifier: Modifier = Modifier) {
    Button(
        onClick = {},
        enabled = false,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = Gray_3,
            disabledContentColor = Gray_5
        )
    ) {
        Image(
            painter = painterResource(R.drawable.ic_fire),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            alpha = 0.5f
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "지금 가요",
            style = IssueTypo.Bold18.copy(color = Gray_5)
        )
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
private fun StatusPill(
    text: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Text(
        text = text,
        style = IssueTypo.Bold12.copy(color = contentColor),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun UserAvatar(
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
            contentDescription = "참여자 프로필",
            modifier = avatarModifier,
            contentScale = ContentScale.Crop
        )
    }
}

private fun formatTimestamp(raw: String): String {
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
