package com.issueissyu.fe.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.issueissyu.fe.domain.model.community.CommunityFeedItem
import com.issueissyu.fe.domain.model.community.CommunityItemKind
import com.issueissyu.fe.ui.theme.IssueTypo // IssueTypo 임포트
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun CommunityFeedCard(
    item: CommunityFeedItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 개별 카드 모서리는 둥글게 하지 않고 리스트 아이템 형태로 유지
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(124.dp) // 높이 고정
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp), // 패딩 조정
        verticalAlignment = Alignment.Top
    ) {
        // 좌측 썸네일 (모서리 10dp 유지)
        if (item.thumbnailUrl != null) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(108.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        Spacer(modifier = Modifier.width(12.dp)) // 이미지와 텍스트 사이 간격 12.dp 유지

        // 우측 정보 Column
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight() // 높이에 맞춰 채움
        ) {
            // 1. 제목 (모든 카테고리)
            Text(
                text = item.title,
                style = IssueTypo.Bold18.copy(color = MaterialTheme.colorScheme.onSurface), // IssueTypo 스타일 기반, fontSize 18sp
                maxLines = 1, // 1줄로 제한
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp)) // 제목과 보조 정보 사이 간격 증가

            // 2. 카테고리별 보조 정보 (최대 2줄)
            when (item.kind) {
                CommunityItemKind.ISSUE, CommunityItemKind.COMMUNICATION -> {
                    if (item.writerNickname != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.writerProfileUrl != null) {
                                AsyncImage(
                                    model = item.writerProfileUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp)) // 프로필 이미지와 닉네임 사이 간격
                            Text(
                                text = item.writerNickname,
                                style = IssueTypo.Regular12.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    if (item.address != null) {
                        Text(
                            text = item.address,
                            style = IssueTypo.Regular12.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
                CommunityItemKind.STORE -> {
                    if (!item.content.isNullOrBlank()) {
                        Text(
                            text = item.content,
                            style = IssueTypo.Regular12.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            maxLines = 1, // 1줄로 제한
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    val periodText = formatPeriod(item.eventStartTime, item.eventEndTime)
                    val discountPeriodText = if (!item.discount.isNullOrBlank() && !periodText.isNullOrBlank()) {
                        "${item.discount} · 기간: $periodText"
                    } else if (!item.discount.isNullOrBlank()) {
                        item.discount
                    } else if (!periodText.isNullOrBlank()) {
                        "기간: $periodText"
                    } else {
                        null
                    }
                    discountPeriodText?.let {
                        Text(
                            text = it,
                            style = IssueTypo.Regular12.copy(color = MaterialTheme.colorScheme.outline),
                            maxLines = 1, // 1줄로 제한
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
                CommunityItemKind.FESTIVAL, CommunityItemKind.CONTEST -> {
                    val periodText = formatPeriod(item.eventStartTime, item.eventEndTime)
                    if (!periodText.isNullOrBlank()) {
                        Text(
                            text = "기간: $periodText",
                            style = IssueTypo.Regular12.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    if (item.address != null) {
                        Text(
                            text = item.address,
                            style = IssueTypo.Regular12.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    } else if (!item.content.isNullOrBlank()) { // 주소가 없으면 content 요약
                        Text(
                            text = item.content,
                            style = IssueTypo.Regular12.copy(color = MaterialTheme.colorScheme.outline),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
                CommunityItemKind.POLICY, CommunityItemKind.CARDNEWS -> {
                    if (!item.content.isNullOrBlank()) {
                        Text(
                            text = item.content,
                            style = IssueTypo.Regular12.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            maxLines = 1, // 1줄로 제한
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
                else -> {} // 다른 종류는 추가 정보 없음
            }

            Spacer(modifier = Modifier.weight(1f)) // 조회/공감 텍스트를 하단으로 밀어냄

            // 3. 조회수/공감수 (모든 카테고리)
            Text(
                text = "조회 ${item.viewCount}·공감 ${item.likeCount}",
                style = IssueTypo.Regular12.copy(color = MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}

@Composable
fun RepresentativeFeedCard(
    item: CommunityFeedItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 대표 카드는 단독 카드처럼 보이므로 Surface로 감싸서 둥근 모서리(16dp) 적용
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .height(124.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좌측 이미지 (모서리 10dp 유지)
            if (item.thumbnailUrl != null) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(108.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            Spacer(modifier = Modifier.width(12.dp)) // 이미지와 텍스트 사이 간격 유지

            // 우측 텍스트
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight() // 높이에 맞춰 채움
                    .padding(vertical = 4.dp) // 내부 상하 패딩 추가
            ) {
                Text(
                    text = item.title,
                    style = IssueTypo.Bold18.copy(color = MaterialTheme.colorScheme.onSurface),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp)) // 제목과 아래 요소 사이 간격

                if (!item.content.isNullOrBlank()) {
                    Text(
                        text = item.content,
                        style = IssueTypo.Regular12.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                } else if (item.address != null) {
                     Text(
                        text = item.address,
                        style = IssueTypo.Regular12.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }


                Spacer(modifier = Modifier.weight(1f)) // 조회/공감 텍스트를 하단으로 밀어냄

                Text(
                    text = "조회 ${item.viewCount}·공감 ${item.likeCount}",
                    style = IssueTypo.Regular12.copy(color = MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
    }
}

private fun formatPeriod(start: String?, end: String?): String? {
    if (start.isNullOrBlank()) return null

    val startText = formatDate(start)
    val endText = if (!end.isNullOrBlank()) formatDate(end) else null

    return if (endText != null) {
        "$startText ~ $endText"
    } else {
        startText
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        // "2026-05-14T00:00:00.000Z" -> "MM.dd" (예: 05.14) 형식으로 포매팅
        val zonedDateTime = ZonedDateTime.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("MM.dd", Locale.getDefault())
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        dateString // 파싱 실패 시 원본 문자열 반환
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
            content = "마포구 성산동 부근에 새로운 공원이 조성될 예정입니다. 주민 여러분의 많은 관심 부탁드립니다.",
            address = "서울시 마포구 성산동",
            writerProfileUrl = "https://picsum.photos/200/200?random=1"
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
            eventEndTime = "2026-05-17T20:00:00.000Z",
            address = "서울시 마포구 연남동"
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
    communityId: Long = 1L,
    pinId: Long? = null,
    kind: CommunityItemKind,
    writerNickname: String? = null,
    title: String = "테스트 제목",
    content: String = "이것은 테스트 게시글의 상세 내용입니다.",
    discount: String? = null,
    eventStartTime: String? = null,
    eventEndTime: String? = null,
    thumbnailUrl: String? = "https://picsum.photos/200/200",
    address: String? = "서울시 마포구 성산동",
    viewCount: Int = 123,
    likeCount: Int = 45,
    isHot: Boolean = false,
    writerProfileUrl: String? = null // Add writerProfileUrl parameter
) = CommunityFeedItem(
    communityId = communityId,
    pinId = pinId,
    kind = kind,
    title = title,
    content = content,
    thumbnailUrl = thumbnailUrl,
    writerNickname = writerNickname,
    writerProfileUrl = writerProfileUrl, // Assign writerProfileUrl
    address = address,
    viewCount = viewCount,
    likeCount = likeCount,
    eventStartTime = eventStartTime,
    eventEndTime = eventEndTime,
    discount = discount,
    isHot = isHot
)
