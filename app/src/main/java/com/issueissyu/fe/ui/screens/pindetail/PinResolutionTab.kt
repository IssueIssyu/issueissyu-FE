package com.issueissyu.fe.ui.screens.pindetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.issueissyu.fe.R
import com.issueissyu.fe.data.model.IssuePinDetail
import com.issueissyu.fe.data.model.IssueResolverParticipation
import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.data.model.PinUser
import com.issueissyu.fe.data.model.ResolutionStatus
import com.issueissyu.fe.data.sample.PinSamples
import com.issueissyu.fe.ui.components.ActionState
import com.issueissyu.fe.ui.components.GoNowButton
import com.issueissyu.fe.ui.components.SignButton
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_1
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.IssueContainerLight
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

    // 시안 기준: 화면 전체는 스크롤하지 않고, 시민해결사 카드가 남는 공간을 모두 차지하며
    //         그 내부에서만 참여자 목록이 스크롤되도록 한다.
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ResolverParticipationCard(
            participations = issueDetail.resolverParticipations,
            resolvedBy = issueDetail.resolvedBy,
            currentUserId = currentUserId,
            resolutionStatus = issueDetail.resolutionStatus,
            resolvedAt = issueDetail.resolvedAt,
            modifier = Modifier.weight(1f)
        )

        PetitionStatusCard(
            petitionCount = issueDetail.petitionCount,
            targetCount = targetCount,
            remainingCount = remainingCount,
            progress = petitionProgress
        )

        ResolutionGuideCard()

        // TODO: 추후 입력/액션이 많아지면 PinPostTab처럼 화면 하단 고정 버튼 영역 분리 검토.
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

// 시안 기준: 각 정보 영역을 흰색 카드 + 옅은 회색 stroke로 구분.
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .border(1.dp, Gray_3, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun SectionHeader(
    text: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = IssueTypo.Bold18.copy(color = Title),
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

@Composable
private fun ResolverParticipationCard(
    participations: List<IssueResolverParticipation>,
    resolvedBy: PinUser?,
    currentUserId: String,
    resolutionStatus: ResolutionStatus,
    resolvedAt: String?,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        SectionHeader(
            text = "시민해결사 참여 현황 (${participations.size}명)",
            trailing = {
                // RESOLVED일 때만 헤더 우측에 해결 시각을 작게 표기.
                if (resolutionStatus == ResolutionStatus.RESOLVED && !resolvedAt.isNullOrBlank()) {
                    Text(
                        text = "${formatTimestamp(resolvedAt)} 해결",
                        style = IssueTypo.Regular12.copy(color = Gray_6)
                    )
                }
            }
        )
        // 카드가 부모로부터 weight(1f)를 받으므로 내부 컨텐츠도 남는 공간을 모두 차지하도록 처리한다.
        // 참여자가 있을 때는 LazyColumn으로 내부 스크롤을, 없을 때는 빈 안내가 영역을 채우도록 한다.
        when {
            participations.isEmpty() && resolvedBy == null -> {
                EmptyResolverPlaceholder(modifier = Modifier.weight(1f))
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(participations) { participation ->
                        ResolverParticipationItem(
                            participation = participation,
                            isFinalResolver = resolvedBy?.id == participation.user.id,
                            isMe = participation.user.id == currentUserId
                        )
                    }
                    // 방어: resolvedBy가 participations에 포함되지 않은 경우에도 최종 해결자는 표시한다.
                    if (resolvedBy != null && participations.none { it.user.id == resolvedBy.id }) {
                        item { ResolvedByHighlight(user = resolvedBy) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyResolverPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Gray_1)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "아직 참여한 시민해결사가 없어요.",
            style = IssueTypo.Regular15.copy(color = Gray_6)
        )
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

    val backgroundColor = if (isFinalResolver) IssueContainerLight else Gray_1

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
private fun PetitionStatusCard(
    petitionCount: Int,
    targetCount: Int?,
    remainingCount: Int?,
    progress: Float
) {
    SectionCard {
        SectionHeader(text = "청원 현황")
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

// 시안 기준: 안내 영역은 회색 카드 + 작은 본문으로 시각적 강도를 낮춘다.
@Composable
private fun ResolutionGuideCard() {
    val guides = listOf(
        "지금가요를 누르면 시민해결사로 참여할 수 있어요.",
        "인증 사진을 제출하면 작성자가 확인 후 해결 완료 처리해요.",
        "청원은 해결 상태와 별개로 참여할 수 있어요."
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Gray_1)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "진행 방식",
            style = IssueTypo.Bold12.copy(color = Title)
        )
        guides.forEach { line ->
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "•",
                    style = IssueTypo.Regular12.copy(color = Gray_6)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = line,
                    style = IssueTypo.Regular12.copy(color = TextColor),
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
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

@Preview(name = "RESOLUTION · 해결 전 (참여자 없음)", showBackground = true, heightDp = 1000)
@Composable
private fun PinResolutionTabPreview_BeforeResolution() {
    IssueissyuTheme {
        val pin = PinSamples.findById(PinSamples.IssuePinId)
        PinResolutionTab(
            pin = pin,
            issueDetail = pin.detail as IssuePinDetail,
            currentUserId = PinSamples.user2.id,
            onGoNowClick = {},
            onPetitionClick = {}
        )
    }
}

@Preview(name = "RESOLUTION · 해결 중 (내가 참여)", showBackground = true, heightDp = 1000)
@Composable
private fun PinResolutionTabPreview_InProgressMyParticipation() {
    IssueissyuTheme {
        // PinSamples.IssueInProgressPinId의 resolverParticipations에 user1이 포함되어 있어 user1 시점 사용.
        val pin = PinSamples.findById(PinSamples.IssueInProgressPinId)
        PinResolutionTab(
            pin = pin,
            issueDetail = pin.detail as IssuePinDetail,
            currentUserId = PinSamples.user1.id,
            onGoNowClick = {},
            onPetitionClick = {}
        )
    }
}

@Preview(name = "RESOLUTION · 해결 완료", showBackground = true, heightDp = 1000)
@Composable
private fun PinResolutionTabPreview_Resolved() {
    IssueissyuTheme {
        val pin = PinSamples.findById(PinSamples.IssueResolvedPinId)
        PinResolutionTab(
            pin = pin,
            issueDetail = pin.detail as IssuePinDetail,
            currentUserId = PinSamples.user2.id,
            onGoNowClick = {},
            onPetitionClick = {}
        )
    }
}

@Preview(name = "RESOLUTION · 작성자 본인 시점", showBackground = true, heightDp = 1000)
@Composable
private fun PinResolutionTabPreview_AuthorView() {
    IssueissyuTheme {
        // PinSamples.IssueInProgressPinId의 writer가 user2이므로 user2 시점 = 작성자 본인 시점.
        val pin = PinSamples.findById(PinSamples.IssueInProgressPinId)
        PinResolutionTab(
            pin = pin,
            issueDetail = pin.detail as IssuePinDetail,
            currentUserId = PinSamples.user2.id,
            onGoNowClick = {},
            onPetitionClick = {}
        )
    }
}
