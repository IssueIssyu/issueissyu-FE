package com.issueissyu.fe.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.HorizontalDivider // 변경됨: Divider -> HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.issueissyu.fe.R
import com.issueissyu.fe.domain.model.pin.CommunicationPinDetail
import com.issueissyu.fe.domain.model.pin.FestivalPinDetail
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.domain.model.pin.PinEmojiReaction
import com.issueissyu.fe.domain.model.pin.PinUser
import com.issueissyu.fe.domain.model.pin.ResolutionStatus
import com.issueissyu.fe.domain.model.pin.ShopPinDetail
import com.issueissyu.fe.domain.model.pin.canEditBy
import com.issueissyu.fe.ui.theme.*

@Composable
fun PinSummaryCard(
    pin: Pin,
    currentUserId: String,
    onDetailClick: (String) -> Unit,
    onCommunityClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onSympathyClick: (String) -> Unit,
    onEmojiClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBackgroundColor = when (pin.category) {
        PinCategory.ISSUE -> IssueContainer
        PinCategory.COMMUNICATION -> CommunicationContainerLight
        PinCategory.SHOP -> ShopContainer
        PinCategory.FESTIVAL -> FestivalContainer
    }

    val canEdit = pin.canEditBy(currentUserId)

    Box(
        modifier = modifier
            .height(260.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(cardBackgroundColor)
            .clickable { onDetailClick(pin.id) }
            .padding(16.dp)
    ) {
        Column {
            // TopRow: 제목/장소/사용자액션 + 이미지
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 1. 제목
                    Text(
                        text = pin.title,
                        style = IssueTypo.Bold18.copy(color = Title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. 장소 + 해결 상태
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "위치",
                            tint = Gray_7,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pin.locationName ?: pin.address,
                            style = IssueTypo.Regular12.copy(color = Gray_7),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 190.dp)
                        )

                        val issueDetail = pin.detail as? IssuePinDetail
                        if (issueDetail != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            ResolutionStatusPill(
                                resolutionStatus = issueDetail.resolutionStatus
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. 사용자 프로필 + 사용자 이름 + 액션 버튼 + 공감 버튼
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (val detail = pin.detail) {
                            is IssuePinDetail -> {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Gray_3)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = detail.writer.name,
                                    style = IssueTypo.Regular12.copy(color = Text),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 80.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                if (canEdit) {
                                    CompactCircleIconButton(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "수정",
                                        onClick = { onEditClick(pin.id) }
                                    )

                                    Spacer(modifier = Modifier.width(3.dp))

                                    CompactCircleIconButton(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "삭제",
                                        onClick = { onDeleteClick(pin.id) }
                                    )

                                    Spacer(modifier = Modifier.width(3.dp))
                                }

                                CompactSympathyButton(pin.id, pin.sympathyCount, pin.isSympathizedByMe, onSympathyClick)
                            }
                            is CommunicationPinDetail -> {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Gray_3)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = detail.writer.name,
                                    style = IssueTypo.Regular12.copy(color = Text),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 80.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                if (canEdit) {
                                    CompactCircleIconButton(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "수정",
                                        onClick = { onEditClick(pin.id) }
                                    )

                                    Spacer(modifier = Modifier.width(3.dp))

                                    CompactCircleIconButton(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "삭제",
                                        onClick = { onDeleteClick(pin.id) }
                                    )

                                    Spacer(modifier = Modifier.width(3.dp))
                                }

                                CompactSympathyButton(pin.id, pin.sympathyCount, pin.isSympathizedByMe, onSympathyClick)
                            }
                            is ShopPinDetail, is FestivalPinDetail -> {
                                // 상점/축제는 작성자 정보 없음, 공감 버튼만 자연스럽게 배치
                                CompactSympathyButton(pin.id, pin.sympathyCount, pin.isSympathizedByMe, onSympathyClick)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                PinThumbnail(
                    imageUrl = pin.imageUrls.firstOrNull(),
                    modifier = Modifier.size(96.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4) Divider
            HorizontalDivider(color = Gray_3, thickness = 1.dp) 

            Spacer(modifier = Modifier.height(12.dp))

            // 3) CategoryInfoRow 또는 CategoryInfoBox
            when (val detail = pin.detail) {
                is ShopPinDetail -> {
                    detail.currentNews?.let { news ->
                        CategoryInfoBox(
                            text = "최신 소식: $news",
                            modifier = Modifier.padding(horizontal = 0.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                is FestivalPinDetail -> {
                    val startDateText = detail.startDate?.take(10)
                    val endDateText = detail.endDate?.take(10)

                    if (startDateText != null || endDateText != null) {
                        CategoryInfoBox(
                            text = "기간: ${startDateText ?: "시작일 미정"} ~ ${endDateText ?: "종료일 미정"}",
                            modifier = Modifier.padding(horizontal = 0.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                is IssuePinDetail,
                is CommunicationPinDetail -> {
                    // 이슈/소통 핀은 이 영역에 추가 정보 표시 없음
                }
            }

            // 4) DescriptionArea: 본문 + "더보기"
            Column {
                Text(
                    text = pin.description,
                    style = IssueTypo.Regular15.copy(color = Text),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "더보기",
                    style = IssueTypo.Bold12.copy(color = BrandColor),
                    modifier = Modifier.clickable { onDetailClick(pin.id) }
                )
            }

            // 기존 카테고리별 추가 박스 제거되었으므로 Spacer 유지
            Spacer(modifier = Modifier.height(12.dp))

            // 6) BottomActionRow: 이모지/반응 영역 + 커뮤니티 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 이모지/반응 영역
                EmojiReactionRow(
                    pin = pin,
                    onEmojiClick = onEmojiClick,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 커뮤니티 버튼 (pin.communityPostId가 있을 때만 표시)
                if (pin.communityPostId != null) {
                    CommunityLinkButton(
                        communityPostId = pin.communityPostId,
                        onCommunityClick = onCommunityClick
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityLinkButton(
    communityPostId: String,
    onCommunityClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Orange)
            .clickable { onCommunityClick(communityPostId) }
            .padding(start = 12.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "커뮤니티",
            maxLines = 1,
            softWrap = false,
            style = IssueTypo.Bold12.copy(color = White)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CompactSympathyButton(
    pinId: String,
    sympathyCount: Int,
    isSympathizedByMe: Boolean,
    onSympathyClick: (String) -> Unit
) {
    val backgroundColor = if (isSympathizedByMe) BrandColor else White
    val contentColor = if (isSympathizedByMe) White else BrandColor

    Row(
        modifier = Modifier
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onSympathyClick(pinId) }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ThumbUp,
            contentDescription = "공감",
            tint = contentColor,
            modifier = Modifier.size(13.dp)
        )

        Spacer(modifier = Modifier.width(3.dp))

        Text(
            text = "$sympathyCount",
            style = IssueTypo.Regular12.copy(color = contentColor),
            maxLines = 1
        )
    }
}

@Composable
private fun CompactCircleIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = Gray_7,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ResolutionStatusPill(
    resolutionStatus: ResolutionStatus
) {
    val backgroundColor = when (resolutionStatus) {
        ResolutionStatus.BEFORE_RESOLUTION -> Issue
        ResolutionStatus.IN_PROGRESS -> Orange
        ResolutionStatus.RESOLVED -> BrandColor
    }

    Text(
        text = resolutionStatus.toDisplayText(),
        style = IssueTypo.Bold12.copy(color = White),
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun EmojiReactionRow(
    pin: Pin,
    onEmojiClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 이모지 추가 버튼
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable { onEmojiClick(pin.id) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add_emoji),
                contentDescription = "이모지 추가",
                tint = Color.Unspecified,
                modifier = Modifier.size(26.dp)
            )
        }

        val visibleReactions = pin.emojiReactions
            .sortedByDescending { it.count }
            .take(3)

        visibleReactions.forEach { reaction ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Gray_3, RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                if (!reaction.emojiImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = reaction.emojiImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${reaction.count}",
                    style = IssueTypo.Regular12.copy(color = Text)
                )
            }
        }

        val hiddenReactionTypeCount = (pin.emojiReactions.size - visibleReactions.size).coerceAtLeast(0)
        if (hiddenReactionTypeCount > 0) {
            Text(
                text = "+${hiddenReactionTypeCount}",
                style = IssueTypo.Regular12.copy(color = Text),
                modifier = Modifier
                    .background(Gray_3, RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun PinThumbnail(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    if (!imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "핀 이미지",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Gray_3)
        )
        return
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Gray_3),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "이미지",
            style = IssueTypo.Regular12.copy(color = Text)
        )
    }
}

private fun ResolutionStatus.toDisplayText(): String {
    return when (this) {
        ResolutionStatus.BEFORE_RESOLUTION -> "해결 전"
        ResolutionStatus.IN_PROGRESS -> "해결 중"
        ResolutionStatus.RESOLVED -> "해결 완료"
    }
}

@Composable
private fun CategoryInfoBox(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = IssueTypo.Regular12.copy(color = Text),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPinSummaryCard() {
    IssueissyuTheme {
        PinSummaryCard(
            pin = Pin(
                id = "preview_issue",
                title = "쓰레기 무단투기. 너무 길면 어떻게 보일까? 두 줄 이상 넘어가면 ellipsis 처리 필요",
                description = "골목에 쓰레기가 계속 방치되고 있습니다. 이곳은 테스트 이슈 핀의 상세 설명입니다. 길게 작성될 수도 있습니다.",
                coordinate = PinCoordinate(
                    latitude = 37.5665,
                    longitude = 126.9780
                ),
                address = "서울특별시 중구 세종대로 110",
                locationName = "시청 앞 골목",
                imageUrls = emptyList(),
                viewCount = 10,
                sympathyCount = 21,
                isSympathizedByMe = true,
                emojiReactions = listOf(
                    PinEmojiReaction(
                        emojiId = "emoji_fire",
                        count = 10,
                        reactedByMe = true
                    ),
                    PinEmojiReaction(
                        emojiId = "emoji_heart",
                        count = 5,
                        reactedByMe = false
                    ),
                    PinEmojiReaction(
                        emojiId = "emoji_clap",
                        count = 3,
                        reactedByMe = false
                    ),
                    PinEmojiReaction(
                        emojiId = "emoji_laugh",
                        count = 2,
                        reactedByMe = false
                    )
                ),
                communityPostId = "community_preview_issue",
                createdAt = "2026-05-04T00:00:00Z",
                updatedAt = null,
                detail = IssuePinDetail(
                    writer = PinUser(
                        id = "user_preview",
                        name = "미리보기 사용자",
                        imageUrl = null
                    ),
                    resolutionStatus = ResolutionStatus.BEFORE_RESOLUTION,
                    petitionCount = 5,
                    isPetitionedByMe = false
                )
            ),
            currentUserId = "user_preview",
            onDetailClick = { },
            onCommunityClick = { },
            onEditClick = { },
            onDeleteClick = { },
            onSympathyClick = { },
            onEmojiClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPinSummaryCardCommunication() {
    IssueissyuTheme {
        PinSummaryCard(
            pin = Pin(
                id = "preview_communication",
                title = "동네 소통 글",
                description = "오늘 저녁 같이 산책하실 분 있나요? 함께 이야기 나누고 싶습니다.",
                coordinate = PinCoordinate(
                    latitude = 37.5665,
                    longitude = 126.9780
                ),
                address = "서울특별시 중구 세종대로 110",
                locationName = "동네 공원",
                imageUrls = emptyList(),
                viewCount = 5,
                sympathyCount = 3,
                isSympathizedByMe = false,
                emojiReactions = listOf(
                    PinEmojiReaction(
                        emojiId = "emoji_laugh",
                        count = 8,
                        reactedByMe = false
                    )
                ),
                communityPostId = null,
                createdAt = "2026-05-04T00:00:00Z",
                updatedAt = null,
                detail = CommunicationPinDetail(
                    writer = PinUser(
                        id = "user_preview",
                        name = "미리보기 사용자",
                        imageUrl = null
                    )
                )
            ),
            currentUserId = "user_preview",
            onDetailClick = { },
            onCommunityClick = { },
            onEditClick = { },
            onDeleteClick = { },
            onSympathyClick = { },
            onEmojiClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPinSummaryCardShop() {
    IssueissyuTheme {
        PinSummaryCard(
            pin = Pin(
                id = "preview_shop",
                title = "커피빈 신메뉴 출시! 라떼 반값 할인 이벤트",
                description = "신메뉴 할인 행사 중입니다. 놓치지 마세요!",
                coordinate = PinCoordinate(
                    latitude = 37.5665,
                    longitude = 126.9780
                ),
                address = "서울특별시 중구 세종대로 110",
                locationName = "커피빈 시청점",
                imageUrls = emptyList(),
                viewCount = 20,
                sympathyCount = 6,
                isSympathizedByMe = false,
                emojiReactions = emptyList(),
                communityPostId = "shop1",
                createdAt = "2026-05-04T00:00:00Z",
                updatedAt = null,
                detail = ShopPinDetail(
                    keywords = listOf("카페", "할인", "커피"),
                    currentNews = "전 메뉴 50% 할인"
                )
            ),
            currentUserId = "user_preview_shop",
            onDetailClick = { },
            onCommunityClick = { },
            onEditClick = { },
            onDeleteClick = { },
            onSympathyClick = { },
            onEmojiClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPinSummaryCardFestival() {
    IssueissyuTheme {
        PinSummaryCard(
            pin = Pin(
                id = "preview_festival",
                title = "여의도 불꽃축제",
                description = "아름다운 불꽃놀이를 즐겨보세요! 놓치면 후회할 특별한 이벤트",
                coordinate = PinCoordinate(
                    latitude = 37.5665,
                    longitude = 126.9780
                ),
                address = "여의도 한강공원",
                locationName = "여의도",
                imageUrls = emptyList(),
                viewCount = 30,
                sympathyCount = 12,
                isSympathizedByMe = false,
                emojiReactions = emptyList(),
                communityPostId = "festival1",
                createdAt = "2026-05-04T00:00:00Z",
                updatedAt = null,
                detail = FestivalPinDetail(
                    keywords = listOf("불꽃", "축제", "한강"),
                    startDate = "2026-10-01",
                    endDate = "2026-10-03"
                )
            ),
            currentUserId = "user_preview_festival",
            onDetailClick = { },
            onCommunityClick = { },
            onEditClick = { },
            onDeleteClick = { },
            onSympathyClick = { },
            onEmojiClick = { }
        )
    }
}
