package com.issueissyu.fe.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.issueissyu.fe.domain.model.community.CommunityFeedItem
import com.issueissyu.fe.domain.model.community.CommunityItemKind

@Composable
fun CommunityFeedCard(
    item: CommunityFeedItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 좌측 썸네일
        if (item.thumbnailUrl != null) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 우측 정보
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Kind별 상세 정보 표시
            when (item.kind) {
                CommunityItemKind.STORE -> {
                    if (!item.content.isNullOrBlank()) {
                        Text(
                            text = item.content,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF757575)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!item.discount.isNullOrBlank()) {
                        Text(
                            text = "혜택: ${item.discount}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    if (!item.eventStartTime.isNullOrBlank()) {
                        Text(
                            text = "기간: ${formatDate(item.eventStartTime)} ~ ${formatDate(item.eventEndTime)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9E9E9E))
                        )
                    }
                }
                CommunityItemKind.FESTIVAL -> {
                    if (!item.eventStartTime.isNullOrBlank()) {
                        Text(
                            text = "일시: ${formatDate(item.eventStartTime)} ~ ${formatDate(item.eventEndTime)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF757575))
                        )
                    }
                    if (!item.content.isNullOrBlank()) {
                        Text(
                            text = item.content,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9E9E9E)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                CommunityItemKind.CONTEST -> {
                    if (!item.content.isNullOrBlank()) {
                        Text(
                            text = item.content,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF757575)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!item.eventStartTime.isNullOrBlank()) {
                        Text(
                            text = "접수: ${formatDate(item.eventStartTime)} ~ ${formatDate(item.eventEndTime)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9E9E9E))
                        )
                    }
                }
                else -> {
                    // ISSUE, COMMUNICATION, POLICY, CARDNEWS 등
                    if (!item.content.isNullOrBlank()) {
                        Text(
                            text = item.content,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF757575)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 작성자 및 주소 정보
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!item.writerNickname.isNullOrBlank() && 
                    (item.kind == CommunityItemKind.ISSUE || item.kind == CommunityItemKind.COMMUNICATION)) {
                    Text(
                        text = item.writerNickname,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFBDBDBD))
                    )
                }
                
                if (item.address != null) {
                    Text(
                        text = item.address,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 통계 정보
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color(0xFFBDBDBD)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = item.viewCount.toString(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Color(0xFFBDBDBD)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color(0xFFBDBDBD)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = item.likeCount.toString(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Color(0xFFBDBDBD)
                    )
                )
            }
        }
    }
}

@Composable
fun RepresentativeFeedCard(
    item: CommunityFeedItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좌측 이미지
            if (item.thumbnailUrl != null) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(120.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(120.dp)
                        .background(Color(0xFFF5F5F5))
                )
            }

            // 우측 텍스트
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (item.address != null) {
                    Text(
                        text = item.address,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString == null) return ""
    // "2026-05-14T00:00:00.000Z" -> "05.14" 간략화 (임시)
    return try {
        val parts = dateString.split("-")
        if (parts.size >= 3) {
            val month = parts[1]
            val day = parts[2].take(2)
            "$month.$day"
        } else dateString
    } catch (e: Exception) {
        dateString
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityFeedCardIssue() {
    CommunityFeedCard(
        item = createPreviewItem(
            kind = CommunityItemKind.ISSUE,
            writerNickname = "이슈알리미",
            title = "우리 동네 새로운 이슈가 발생했습니다",
            content = "마포구 성산동 부근에 새로운 공원이 조성될 예정입니다. 주민 여러분의 많은 관심 부탁드립니다."
        ),
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityFeedCardStore() {
    CommunityFeedCard(
        item = createPreviewItem(
            kind = CommunityItemKind.STORE,
            writerNickname = null,
            title = "맛있는 빵집 오픈 1주년 이벤트!",
            content = "오픈 1주년을 맞아 전 품목 할인 행사를 진행합니다. 맛있는 빵 드시러 오세요!",
            discount = "30% 할인",
            eventStartTime = "2026-05-14T00:00:00.000Z",
            eventEndTime = "2026-05-20T00:00:00.000Z"
        ),
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityFeedCardFestival() {
    CommunityFeedCard(
        item = createPreviewItem(
            kind = CommunityItemKind.FESTIVAL,
            writerNickname = null,
            title = "2026 마포구 봄꽃 축제",
            content = "경의선 숲길에서 펼쳐지는 봄꽃의 향연! 다양한 공연과 먹거리가 준비되어 있습니다.",
            eventStartTime = "2026-05-15T10:00:00.000Z",
            eventEndTime = "2026-05-17T20:00:00.000Z"
        ),
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityFeedCardPolicy() {
    CommunityFeedCard(
        item = createPreviewItem(
            kind = CommunityItemKind.POLICY,
            writerNickname = null,
            title = "청년 월세 지원 사업 안내",
            content = "마포구에 거주하는 무주택 청년들을 대상으로 월세를 지원해드립니다. 지금 바로 신청하세요!",
            thumbnailUrl = null
        ),
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityFeedCardContest() {
    CommunityFeedCard(
        item = createPreviewItem(
            kind = CommunityItemKind.CONTEST,
            writerNickname = null,
            title = "제 1회 마포구 숏폼 영상 공모전",
            content = "마포구의 매력을 1분 내외의 영상으로 담아주세요. 총 상금 1,000만원!",
            eventStartTime = "2026-05-01T00:00:00.000Z",
            eventEndTime = "2026-05-31T23:59:59.000Z"
        ),
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityFeedCardCardNews() {
    CommunityFeedCard(
        item = createPreviewItem(
            kind = CommunityItemKind.CARDNEWS,
            writerNickname = null,
            title = "이번 주 마포구 주요 소식 TOP 3",
            content = "1. 공원 조성 소식 2. 할인 행사 안내 3. 주말 날씨 정보",
            thumbnailUrl = "https://picsum.photos/400/300?random=1"
        ),
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewRepresentativeFeedCard() {
    RepresentativeFeedCard(
        item = createPreviewItem(
            kind = CommunityItemKind.ISSUE,
            title = "우리 동네 핫플레이스: 경의선 숲길 산책로",
            thumbnailUrl = "https://picsum.photos/600/400?random=2"
        ),
        onClick = {}
    )
}

private fun createPreviewItem(
    kind: CommunityItemKind,
    writerNickname: String? = null,
    title: String = "테스트 제목",
    content: String = "이것은 테스트 게시글의 상세 내용입니다.",
    discount: String? = null,
    eventStartTime: String? = null,
    eventEndTime: String? = null,
    thumbnailUrl: String? = "https://picsum.photos/200/200"
) = CommunityFeedItem(
    communityId = 1L,
    pinId = null,
    kind = kind,
    title = title,
    content = content,
    thumbnailUrl = thumbnailUrl,
    writerNickname = writerNickname,
    writerProfileUrl = null,
    address = "서울시 마포구 성산동",
    viewCount = 123,
    likeCount = 45,
    eventStartTime = eventStartTime,
    eventEndTime = eventEndTime,
    discount = discount,
    isHot = false
)
