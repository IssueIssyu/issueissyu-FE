package com.issueissyu.fe.ui.screens.pindetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.issueissyu.fe.domain.model.CommunicationPinDetail
import com.issueissyu.fe.domain.model.IssuePinDetail
import com.issueissyu.fe.domain.model.Pin
import com.issueissyu.fe.domain.model.PinEmojiReaction
import com.issueissyu.fe.domain.model.PinUser
import com.issueissyu.fe.data.sample.PinSamples
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_1
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_4
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Text as TextColor
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White

// TODO: PinCommentRepository 연결 후 댓글 목록 조회
private const val DummyMyAuthorId = "user1_id"
private const val DummyOtherAuthorId = "user2_id"

private data class CommentPlaceholder(
    val authorId: String,
    val authorName: String,
    val authorImageUrl: String? = null,
    val content: String
)

private val DummyComments = listOf(
    CommentPlaceholder(
        authorId = DummyMyAuthorId,
        authorName = "현재 사용자",
        content = "여기에 내가 쓴 댓글이 표시될 예정이에요."
    ),
    CommentPlaceholder(
        authorId = DummyOtherAuthorId,
        authorName = "다른 사용자",
        content = "여기에 다른 사람 댓글이 표시될 예정이에요."
    )
)

@Composable
fun PinPostTab(
    pin: Pin,
    currentUserId: String,
    onSympathyClick: (String) -> Unit,
    onEmojiClick: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onCommentSubmit: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val writer = when (val detail = pin.detail) {
        is IssuePinDetail -> detail.writer
        is CommunicationPinDetail -> detail.writer
        else -> null
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SympathyRequestSection(
                writer = writer,
                pinTitle = pin.title,
                sympathyCount = pin.sympathyCount,
                isSympathizedByMe = pin.isSympathizedByMe,
                onSympathyClick = { onSympathyClick(pin.id) }
            )

            EmojiReactionRow(
                reactions = pin.emojiReactions,
                onAddEmojiClick = { onEmojiClick(pin.id) }
            )
        }

        HorizontalDivider(color = Gray_3, thickness = 1.dp)

        CommentList(
            comments = DummyComments,
            currentUserId = currentUserId,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        CommentInputBar(
            onSubmit = {
                // TODO: 입력값을 캡처해 onCommentSubmit(pin.id, content)으로 전달
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, end = 28.dp, bottom = 30.dp)
        )
    }
}

@Composable
private fun SympathyRequestSection(
    writer: PinUser?,
    pinTitle: String,
    sympathyCount: Int,
    isSympathizedByMe: Boolean,
    onSympathyClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        WriterAvatar(imageUrl = writer?.imageUrl, size = 56.dp)

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 18.dp,
                        bottomEnd = 18.dp,
                        bottomStart = 18.dp
                    )
                )
                .background(Gray_1)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val text = if (writer != null) {
                    "${writer.name}님이 \"$pinTitle\"에 대해 공감을 요청했어요"
                } else {
                    "이 핀에 대한 공감을 요청하고 있어요"
                }
                Text(
                    text = text,
                    style = IssueTypo.Regular15.copy(color = Title),
                    lineHeight = 22.sp
                )
                SympathyPill(
                    sympathyCount = sympathyCount,
                    isSympathizedByMe = isSympathizedByMe,
                    onClick = onSympathyClick
                )
            }
        }
    }
}

@Composable
private fun WriterAvatar(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    // TODO: PinHomeTab WriterAvatar와 공통화 검토
    val avatarModifier = modifier
        .size(size)
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

// TODO: PinSummaryCard.CompactSympathyButton과 공통 컴포넌트로 통합 검토
@Composable
private fun SympathyPill(
    sympathyCount: Int,
    isSympathizedByMe: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSympathizedByMe) BrandColor else White
    val contentColor = if (isSympathizedByMe) White else BrandColor

    Row(
        modifier = Modifier
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmojiReactionRow(
    reactions: List<PinEmojiReaction>,
    onAddEmojiClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (reactions.isEmpty()) {
            Text(
                text = "아직 이모지 반응이 없어요.",
                style = IssueTypo.Regular12.copy(color = Gray_5)
            )
        }
        // TODO: 이모지 선택 BottomSheet 연결
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            reactions
                .sortedByDescending { it.count }
                .forEach { reaction ->
                    EmojiReactionChip(reaction = reaction)
                }
            AddEmojiChip(onClick = onAddEmojiClick)
        }
    }
}

@Composable
private fun EmojiReactionChip(reaction: PinEmojiReaction) {
    val backgroundColor = if (reaction.reactedByMe) {
        BrandColor.copy(alpha = 0.12f)
    } else {
        Gray_3
    }
    val borderColor = if (reaction.reactedByMe) BrandColor else Color.Transparent

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TODO: emojiId → 실제 이모지 이미지/유니코드 매핑
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Gray_4)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${reaction.count}",
            style = IssueTypo.Regular12.copy(color = TextColor)
        )
    }
}

@Composable
private fun AddEmojiChip(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Gray_3)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "이모지 추가",
            tint = Gray_6,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "이모지",
            style = IssueTypo.Regular12.copy(color = TextColor)
        )
    }
}

// TODO: PinCommentRepository 연결 후 실제 댓글 목록 전달
@Suppress("SameParameterValue")
@Composable
private fun CommentList(
    comments: List<CommentPlaceholder>,
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 28.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "댓글",
            style = IssueTypo.Bold18.copy(color = Title)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (comments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "첫 댓글을 남겨보세요!",
                    style = IssueTypo.Regular15.copy(color = Gray_5)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(comments) { comment ->
                    CommentItem(
                        authorName = comment.authorName,
                        authorImageUrl = comment.authorImageUrl,
                        content = comment.content,
                        isMine = comment.authorId == currentUserId,
                        onEditClick = {
                            // TODO: PinCommentRepository.update 연결
                        },
                        onDeleteClick = {
                            // TODO: PinCommentRepository.delete 연결
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    authorName: String,
    authorImageUrl: String?,
    content: String,
    isMine: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        WriterAvatar(imageUrl = authorImageUrl, size = 40.dp)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = authorName,
                    style = IssueTypo.Bold12.copy(color = Title),
                    modifier = Modifier.weight(1f)
                )
                if (isMine) {
                    Text(
                        text = "수정",
                        style = IssueTypo.Regular12.copy(color = Gray_5),
                        modifier = Modifier.clickable(onClick = onEditClick)
                    )
                    Text(
                        text = "삭제",
                        style = IssueTypo.Regular12.copy(color = Gray_5),
                        modifier = Modifier.clickable(onClick = onDeleteClick)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 16.dp,
                            bottomEnd = 16.dp,
                            bottomStart = 16.dp
                        )
                    )
                    .background(BrandColor)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = content,
                    style = IssueTypo.Regular15.copy(color = White)
                )
            }
        }
    }
}

// TODO: CommonTextField 기반 입력 칸으로 교체 후 onSubmit으로 입력값 전달
@Composable
private fun CommentInputBar(
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(White)
                .border(1.dp, BrandColor, RoundedCornerShape(24.dp))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "댓글 입력",
                style = IssueTypo.Regular15.copy(color = Gray_5)
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BrandColor)
                .clickable(onClick = onSubmit),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "댓글 전송",
                tint = White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(name = "POST · 빈 상태", showBackground = true, heightDp = 900)
@Composable
private fun PinPostTabPreview_Empty() {
    IssueissyuTheme {
        PinPostTab(
            pin = PinSamples.findById(PinSamples.CommunicationPlainPinId),
            currentUserId = DummyMyAuthorId,
            onSympathyClick = {},
            onEmojiClick = {},
            onCommentSubmit = { _, _ -> }
        )
    }
}

@Preview(name = "POST · 데이터 있음", showBackground = true, heightDp = 900)
@Composable
private fun PinPostTabPreview_Filled() {
    IssueissyuTheme {
        PinPostTab(
            pin = PinSamples.findById(PinSamples.IssuePinId),
            currentUserId = DummyMyAuthorId,
            onSympathyClick = {},
            onEmojiClick = {},
            onCommentSubmit = { _, _ -> }
        )
    }
}
