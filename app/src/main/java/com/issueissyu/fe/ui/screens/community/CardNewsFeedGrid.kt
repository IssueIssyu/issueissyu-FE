package com.issueissyu.fe.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.issueissyu.fe.domain.model.community.CommunityFeedItem
import com.issueissyu.fe.domain.model.community.CommunityItemKind
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.White

private val CardNewsTileAspectRatio = 0.78f

@Composable
fun CardNewsFeedGrid(
    items: List<CommunityFeedItem>,
    onItemClick: (CommunityFeedItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 4.dp,
            end = 4.dp,
            top = 4.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(items, key = { it.communityId }) { item ->
            CardNewsFeedTile(
                item = item,
                onClick = { onItemClick(item) },
            )
        }
    }
}

@Composable
fun CardNewsFeedTile(
    item: CommunityFeedItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(CardNewsTileAspectRatio)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
    ) {
        if (item.thumbnailUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        } else {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.65f),
                        ),
                    ),
                )
                .padding(horizontal = 10.dp, vertical = 12.dp),
        ) {
            Text(
                text = item.title,
                style = IssueTypo.Bold12.copy(color = White),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        CardNewsMultiImageBadge(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
        )
    }
}

@Composable
private fun CardNewsMultiImageBadge(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(width = 22.dp, height = 18.dp)) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .offset(x = 6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .border(1.dp, White.copy(alpha = 0.8f), RoundedCornerShape(3.dp)),
        )
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(1.dp, White, RoundedCornerShape(3.dp)),
        )
    }
}

@Preview(showBackground = true, heightDp = 480)
@Composable
private fun PreviewCardNewsFeedGrid() {
    val items = listOf(
        previewCardNewsFeedItem(
            communityId = 1L,
            title = "마포구 청년 주거 지원 정책",
            thumbnailUrl = "https://picsum.photos/seed/card-news-feed-1/480/620",
        ),
        previewCardNewsFeedItem(
            communityId = 2L,
            title = "우리 동네 공모전 안내",
            thumbnailUrl = "https://picsum.photos/seed/card-news-feed-2/480/620",
        ),
        previewCardNewsFeedItem(
            communityId = 3L,
            title = "이번 주 마포구 소식",
            thumbnailUrl = "https://picsum.photos/seed/card-news-feed-3/480/620",
        ),
        previewCardNewsFeedItem(
            communityId = 4L,
            title = "카드뉴스 제목이 길어질 때 두 줄까지 표시",
            thumbnailUrl = "https://picsum.photos/seed/card-news-feed-4/480/620",
        ),
    )

    CardNewsFeedGrid(
        items = items,
        onItemClick = {},
    )
}

private fun previewCardNewsFeedItem(
    communityId: Long,
    title: String,
    thumbnailUrl: String,
) = CommunityFeedItem(
    communityId = communityId,
    pinId = null,
    kind = CommunityItemKind.CARDNEWS,
    title = title,
    content = null,
    thumbnailUrl = thumbnailUrl,
    writerNickname = null,
    writerProfileUrl = null,
    address = null,
    viewCount = 0,
    likeCount = 0,
    eventStartTime = null,
    eventEndTime = null,
    discount = null,
    isHot = false,
)
