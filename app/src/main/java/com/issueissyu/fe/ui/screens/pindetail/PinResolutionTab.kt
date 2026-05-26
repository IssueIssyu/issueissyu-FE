package com.issueissyu.fe.ui.screens.pindetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.issueissyu.fe.R
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.pin.IssueResolverParticipation
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinUser
import com.issueissyu.fe.domain.model.pin.ResolutionStatus
import com.issueissyu.fe.data.sample.PinSamples
import com.issueissyu.fe.ui.components.ActionState
import com.issueissyu.fe.ui.components.GoNowButton
import com.issueissyu.fe.ui.components.ProfileImageFrame
import com.issueissyu.fe.ui.components.SignButton
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.Gray_1
import com.issueissyu.fe.ui.theme.Gray_2
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_4
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Orange
import com.issueissyu.fe.ui.theme.Success
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
    onAttachProofClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isResolved = issueDetail.resolutionStatus == ResolutionStatus.RESOLVED
    val isWriter = issueDetail.writer.id == currentUserId
    var showGoNowConfirmCard by remember { mutableStateOf(false) }
    val myParticipation = issueDetail.resolverParticipations
        .firstOrNull { it.user.id == currentUserId }
    val isAlreadyResolver = myParticipation != null

    val canGoNow = !isResolved && !isAlreadyResolver && !isWriter

    val goNowState = when {
        myParticipation?.isConfirmedByWriter == true -> ActionState.DONE
        myParticipation?.proofImageUrls?.isNotEmpty() == true -> ActionState.DONE
        myParticipation != null -> ActionState.MOVING
        else -> ActionState.DEFAULT
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = ResolutionBottomBarInset),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            ResolverParticipationCard(
                participations = issueDetail.resolverParticipations,
                resolvedBy = issueDetail.resolvedBy,
                currentUserId = currentUserId,
                resolutionStatus = issueDetail.resolutionStatus,
                resolvedAt = issueDetail.resolvedAt,
                modifier = Modifier.height(ResolverParticipationCardHeight)
            )

            HorizontalDivider( thickness = 1.dp, color = Gray_3 )

            if (goNowState == ActionState.MOVING) {
                ResolutionPhotoProofCard(
                    onAttachClick = onAttachProofClick
                )
            }

            PetitionStatusCard(
                petitionCount = issueDetail.petitionCount
            )

            ResolutionGuideCard()
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            ResolutionActionRow(
                pinId = pin.id,
                canGoNow = canGoNow,
                goNowState = goNowState,
                isWriter = isWriter,
                onGoNowClick = { showGoNowConfirmCard = true },
                petitionCount = issueDetail.petitionCount,
                isPetitionedByMe = issueDetail.isPetitionedByMe,
                onPetitionClick = onPetitionClick
            )
        }

        if (showGoNowConfirmCard) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                ResolutionGoNowFloatingConfirmCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = ResolutionBottomBarInset + ResolutionGoNowConfirmCardBottomGap
                        ),
                    onDismiss = { showGoNowConfirmCard = false },
                    onConfirm = {
                        showGoNowConfirmCard = false
                        onGoNowClick(pin.id)
                    }
                )
            }
        }
    }
}

private val ResolutionBottomBarInset = 92.dp
private val ResolutionGoNowConfirmCardBottomGap = 12.dp
private val ResolutionGoNowConfirmButtonHeight = 46.dp

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(White),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun SectionHeader(
    text: String,
    leadingIconRes: Int? = null,
    leadingIconTint: Color? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIconRes != null) {
            Icon(
                painter = painterResource(leadingIconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
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
    val resolverItems = buildResolverParticipationItems(
        participations = participations,
        resolvedBy = resolvedBy,
        resolvedAt = resolvedAt,
        currentUserId = currentUserId
    )

    SectionCard(modifier = modifier) {
        SectionHeader(
            text = "시민해결사 참여 현황",
            leadingIconRes = R.drawable.ic_resolver,
            trailing = {
                if (resolutionStatus == ResolutionStatus.RESOLVED && !resolvedAt.isNullOrBlank()) {
                    Text(
                        text = "${formatTimestamp(resolvedAt)} 해결",
                        style = IssueTypo.Regular12.copy(color = Gray_6)
                    )
                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
        ) {
            when {
                resolverItems.isEmpty() -> {
                    EmptyResolverPlaceholder(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = resolverItems, key = { it.userId }) { item ->
                            ResolverParticipationItem(
                                item = item
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResolutionPhotoProofCard(
    onAttachClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "사진 인증하기",
    description: String = "현장에 도착하셨나요? 사진을 찍어서 인증해주세요.",
    buttonText: String = "사진 첨부하기",
    iconRes: Int = R.drawable.ic_camera,
) {
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(BrandColor.copy(alpha = 0.08f))
            .border(1.dp, BrandColor.copy(alpha = 0.18f), shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = IssueTypo.Bold18.copy(color = BrandColor)
                )
                Text(
                    text = description,
                    style = IssueTypo.Regular12.copy(color = Gray_6, 14.sp),
                    lineHeight = 22.sp
                )
            }
        }

        Button(
            onClick = onAttachClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandColor,
                contentColor = White
            )
        ) {
            Text(
                text = buttonText,
                style = IssueTypo.Bold18.copy(color = White)
            )
        }
    }
}

private val ResolverParticipationCardHeight = 340.dp

private data class ResolverParticipationItemUiModel(
    val userId: String,
    val profileImageUrl: String?,
    val displayName: String,
    val displayTime: String,
    val progressStatus: ResolverProgressStatus,
    val proofImageUrl: String?,
)

private enum class ResolverProgressStatus(
    val label: String,
    val chipColor: Color,
    val fallbackImageLabel: String,
) {
    RESOLVED(
        label = "해결 완료",
        chipColor = Success,
        fallbackImageLabel = "사진 없음"
    ),
    WAITING_CONFIRMATION(
        label = "확인 대기",
        chipColor = BrandColor,
        fallbackImageLabel = "사진 확인중"
    ),
    MOVING(
        label = "이동중",
        chipColor = Orange,
        fallbackImageLabel = "사진 대기"
    ),
}

private fun buildResolverParticipationItems(
    participations: List<IssueResolverParticipation>,
    resolvedBy: PinUser?,
    resolvedAt: String?,
    currentUserId: String,
): List<ResolverParticipationItemUiModel> {
    val items = participations.map { participation ->
        participation.toResolverParticipationItemUiModel(
            currentUserId = currentUserId,
            resolvedByUserId = resolvedBy?.id,
            resolvedAt = resolvedAt
        )
    }.toMutableList()

    if (resolvedBy != null && participations.none { it.user.id == resolvedBy.id }) {
        items += resolvedBy.toResolvedFallbackItemUiModel(
            currentUserId = currentUserId,
            resolvedAt = resolvedAt
        )
    }

    return items
}

private fun IssueResolverParticipation.toResolverParticipationItemUiModel(
    currentUserId: String,
    resolvedByUserId: String?,
    resolvedAt: String?,
): ResolverParticipationItemUiModel {
    val progressStatus = when {
        isConfirmedByWriter || user.id == resolvedByUserId -> ResolverProgressStatus.RESOLVED
        proofImageUrls.isNotEmpty() -> ResolverProgressStatus.WAITING_CONFIRMATION
        else -> ResolverProgressStatus.MOVING
    }

    val displayTime = when (progressStatus) {
        ResolverProgressStatus.RESOLVED -> confirmedAt ?: resolvedAt ?: proofSubmittedAt ?: joinedAt
        ResolverProgressStatus.WAITING_CONFIRMATION -> proofSubmittedAt ?: joinedAt
        ResolverProgressStatus.MOVING -> joinedAt
    }

    return ResolverParticipationItemUiModel(
        userId = user.id,
        profileImageUrl = user.imageUrl,
        displayName = user.name + if (user.id == currentUserId) " (나)" else "",
        displayTime = formatResolverDisplayTime(displayTime),
        progressStatus = progressStatus,
        proofImageUrl = proofImageUrls.firstOrNull()?.takeIf { it.isNotBlank() }
    )
}

private fun PinUser.toResolvedFallbackItemUiModel(
    currentUserId: String,
    resolvedAt: String?,
): ResolverParticipationItemUiModel {
    return ResolverParticipationItemUiModel(
        userId = id,
        profileImageUrl = imageUrl,
        displayName = name + if (id == currentUserId) " (나)" else "",
        displayTime = formatResolverDisplayTime(resolvedAt),
        progressStatus = ResolverProgressStatus.RESOLVED,
        proofImageUrl = null
    )
}

private fun formatResolverDisplayTime(raw: String?): String {
    return raw?.takeIf { it.isNotBlank() }?.let(::formatTimestamp) ?: "시간 미정"
}

@Composable
private fun EmptyResolverPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Gray_1)
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_spanner),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                colorFilter = ColorFilter.tint(Gray_5)
            )
            Text(
                text = "아직 해결 방안이 등록되지 않았습니다!",
                style = IssueTypo.Regular15.copy(Gray_5, fontWeight = SemiBold)
            )
            Text(
                text = "해결하기에 참여해주세요!",
                style = IssueTypo.Regular12.copy(Gray_5)
            )
        }
    }
}

@Composable
private fun ResolverParticipationItem(
    item: ResolverParticipationItemUiModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Gray_1)
            .padding(20.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileImageFrame(
                size = 45.dp,
                imageUrl = item.profileImageUrl,
                contentDescription = "참여자 프로필"
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    style = IssueTypo.Regular15.copy(color = Text, fontWeight = SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.displayTime,
                    style = IssueTypo.Regular12.copy(color = Gray_6, 11.sp)
                )
            }
        }

        ResolverProgressChip(status = item.progressStatus)
        if (item.progressStatus != ResolverProgressStatus.MOVING) {
            ResolverProofThumbnail(
                imageUrl = item.proofImageUrl,
                fallbackLabel = item.progressStatus.fallbackImageLabel
            )
        }
    }
}

@Composable
private fun PetitionStatusCard(
    petitionCount: Int
) {
    val displayTargetCount = PetitionMailThresholdCount
    val progress = (petitionCount / displayTargetCount.toFloat()).coerceIn(0f, 1f)

    SectionCard {
        SectionHeader(
            text = "청원 현황",
            leadingIconRes = R.drawable.ic_megaphone,
            trailing = {
                Text(
                    text = "${petitionCount}/${displayTargetCount}명 참여",
                    style = IssueTypo.Regular15.copy(color = Gray_5)
                )
            }
        )
        PetitionProgressBar(progress = progress)
        Text(
            text = "${displayTargetCount}명 이상 청원 시 관할 부서에 민원 메일이 자동 전송됩니다.",
            style = IssueTypo.Regular12.copy(color = Gray_5),
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun PetitionProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Gray_3)
    ) {
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(14.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Orange)
            )
        }
    }
}

private const val PetitionMailThresholdCount = 30

@Composable
private fun ResolverProgressChip(
    status: ResolverProgressStatus,
    modifier: Modifier = Modifier
) {
    Text(
        text = status.label,
        style = IssueTypo.Bold12.copy(color = White, 11.sp),
        modifier = Modifier
            .then(modifier)
            .clip(RoundedCornerShape(999.dp))
            .background(status.chipColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun ResolverProofThumbnail(
    imageUrl: String?,
    fallbackLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(100.dp)
            .height(75.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (imageUrl.isNullOrBlank()) Gray_2 else Gray_3)
            .border(1.dp, Gray_3, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNullOrBlank()) {
            Text(
                text = fallbackLabel,
                style = IssueTypo.Bold12.copy(color = Gray_5)
            )
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = "시민해결사 인증 사진",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun ResolutionGuideCard() {
    val steps = listOf(
        ResolutionGuideStep(
            title = "지금 가요",
            description = "현장을 방문하고 사진 인증을 해주세요.",
            iconRes = R.drawable.ic_fire
        ),
        ResolutionGuideStep(
            title = "사진 인증",
            description = "현장 사진을 첨부하면 글 작성자가 확인합니다.",
            iconRes = R.drawable.ic_camera
        ),
        ResolutionGuideStep(
            title = "완료 처리",
            description = "작성자가 확인 후 해결 완료 처리합니다.",
            iconRes = R.drawable.ic_complete
        ),
        ResolutionGuideStep(
            title = "청원하기",
            description = "개인이 해결하기 어려운 문제는 청원 버튼을 눌러주세요.",
            iconRes = R.drawable.ic_megaphone
        ),
        ResolutionGuideStep(
            title = "자동 민원 전송",
            description = "30명 이상 청원 시 관할 부서에 민원 메일이 자동으로 전송됩니다.",
            iconRes = R.drawable.img_email
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Gray_1)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "이렇게 진행돼요",
            style = IssueTypo.Bold18.copy(color = Title)
        )
        steps.forEach { step ->
            ResolutionGuideStepItem(step = step)
        }
    }
}

private data class ResolutionGuideStep(
    val title: String,
    val description: String,
    val iconRes: Int,
)

@Composable
private fun ResolutionGuideStepItem(
    step: ResolutionGuideStep,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(White)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(step.iconRes),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = step.title,
                style = IssueTypo.Bold12.copy(color = Title)
            )
            Text(
                text = step.description,
                style = IssueTypo.Regular12.copy(color = Gray_6),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun ResolutionGoNowFloatingConfirmCard(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .border(1.dp, BrandColor.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "현장에 방문하시겠어요?",
                style = IssueTypo.Bold18.copy(color = Title)
            )
            Text(
                text = "현장을 방문한 후 사진 인증을 해주세요. 글 작성자가 확인하면 해결 완료 처리됩니다.",
                style = IssueTypo.Regular12.copy(color = Gray_6),
                lineHeight = 20.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(ResolutionGoNowConfirmButtonHeight),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, Gray_3),
                colors = ButtonDefaults.buttonColors(
                    containerColor = White,
                    contentColor = Gray_5
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "취소",
                    style = IssueTypo.Bold18.copy(color = Gray_5)
                )
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(ResolutionGoNowConfirmButtonHeight),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColor,
                    contentColor = White
                )
            ) {
                Text(
                    text = "네, 지금 갈게요!",
                    style = IssueTypo.Bold18.copy(color = White)
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
        if (!isWriter) {
            ResolutionGoNowButton(
                canGoNow = canGoNow,
                goNowState = goNowState,
                onClick = { onGoNowClick(pinId) },
                modifier = Modifier.weight(1f)
            )
        }
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
    // TODO: GoNowButton에 외부 enabled 파라미터 추가 후 공통화 검토
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

@Preview(name = "RESOLUTION · 사진 인증 카드", showBackground = true, widthDp = 360)
@Composable
private fun ResolutionPhotoProofCardPreview() {
    IssueissyuTheme {
        Box(
            modifier = Modifier
                .background(White)
                .padding(20.dp)
        ) {
            ResolutionPhotoProofCard(onAttachClick = {})
        }
    }
}

@Preview(name = "RESOLUTION · 지금 가요 플로팅 확인", showBackground = true, widthDp = 360, heightDp = 240)
@Composable
private fun ResolutionGoNowFloatingConfirmCardPreview() {
    IssueissyuTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray_1)
                .padding(20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            ResolutionGoNowFloatingConfirmCard(
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}

@Preview(name = "RESOLUTION · 이동중 사진 인증 노출", showBackground = true, heightDp = 1000)
@Composable
private fun PinResolutionTabPreview_MovingWithPhotoProof() {
    IssueissyuTheme {
        val pin = PinSamples.findById(PinSamples.IssueInProgressPinId)
        PinResolutionTab(
            pin = pin,
            issueDetail = pin.detail as IssuePinDetail,
            currentUserId = PinSamples.user1.id,
            onGoNowClick = {},
            onPetitionClick = {},
            onAttachProofClick = {}
        )
    }
}

@Preview(name = "RESOLUTION CARD · 진행중", showBackground = true, widthDp = 360)
@Composable
private fun ResolverParticipationCardPreview_InProgress() {
    IssueissyuTheme {
        val detail = PinSamples.findById(PinSamples.IssueInProgressPinId).detail as IssuePinDetail
        Box(
            modifier = Modifier
        ) {
            ResolverParticipationCard(
                participations = detail.resolverParticipations,
                resolvedBy = detail.resolvedBy,
                currentUserId = PinSamples.user1.id,
                resolutionStatus = detail.resolutionStatus,
                resolvedAt = detail.resolvedAt
            )
        }
    }
}

@Preview(name = "RESOLUTION CARD · 해결완료", showBackground = true, widthDp = 360)
@Composable
private fun ResolverParticipationCardPreview_Resolved() {
    IssueissyuTheme {
        val detail = PinSamples.findById(PinSamples.IssueResolvedPinId).detail as IssuePinDetail
        Box(
            modifier = Modifier
                .background(Gray_1)
        ) {
            ResolverParticipationCard(
                participations = detail.resolverParticipations,
                resolvedBy = detail.resolvedBy,
                currentUserId = PinSamples.user2.id,
                resolutionStatus = detail.resolutionStatus,
                resolvedAt = detail.resolvedAt
            )
        }
    }
}

@Preview(name = "RESOLUTION · 해결 중 (내가 참여)", showBackground = true, heightDp = 1000)
@Composable
private fun PinResolutionTabPreview_InProgressMyParticipation() {
    IssueissyuTheme {
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
