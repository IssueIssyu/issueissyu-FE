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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinComment
import com.issueissyu.fe.domain.model.pin.PinPostSympathyContent
import com.issueissyu.fe.data.sample.PinSamples
import com.issueissyu.fe.domain.model.pin.toPostSympathyContent
import com.issueissyu.fe.ui.components.ProfileImageFrame
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

@Composable
fun PinPostTab(
    sympathy: PinPostSympathyContent,
    postEmojis: PinDetailPostEmojis,
    comments: List<PinComment>,
    isCommentsLoading: Boolean,
    isCommentSubmitting: Boolean,
    commentInputRevision: Int,
    editingCommentId: Long?,
    onSympathyClick: () -> Unit,
    onEmojiClick: () -> Unit,
    onEmojiChipClick: (Long) -> Unit,
    onCommentSubmit: (String) -> Unit,
    onCommentEdit: (Long) -> Unit,
    onCommentEditCancel: () -> Unit,
    onCommentDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            when (sympathy.pinType) {
                PinCategory.SHOP -> ShopSympathyRequestSection(
                    content = sympathy,
                    onSympathyClick = onSympathyClick,
                )

                else -> {
                    SympathyRequestSection(
                        content = sympathy,
                        onSympathyClick = onSympathyClick,
                    )
                    sympathy.mainPinImageUrl?.let { imageUrl ->
                        PostCoverImage(imageUrl = imageUrl)
                    }
                }
            }

            EmojiReactionRow(
                postEmojis = postEmojis,
                onAddEmojiClick = onEmojiClick,
                onEmojiChipClick = onEmojiChipClick,
            )
        }

        HorizontalDivider(color = Gray_3, thickness = 1.dp)

        CommentList(
            comments = comments,
            isLoading = isCommentsLoading,
            editingCommentId = editingCommentId,
            onCommentEdit = onCommentEdit,
            onCommentDelete = onCommentDelete,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        val editingComment = comments.firstOrNull { it.commentId == editingCommentId }
        CommentInputBar(
            resetKey = commentInputRevision,
            editingCommentId = editingCommentId,
            initialText = editingComment?.content.orEmpty(),
            enabled = !isCommentSubmitting,
            isEditing = editingCommentId != null,
            onCancelEdit = onCommentEditCancel,
            onSubmit = onCommentSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, end = 28.dp, bottom = 30.dp)
        )
    }
}

private fun buildSympathyRequestMessage(
    writerName: String?,
    pinTitle: String,
) = buildAnnotatedString {
    val bodyStyle = IssueTypo.Regular15.copy(color = Title).toSpanStyle()
    val authorStyle = IssueTypo.ExtraBold15.copy(color = BrandColor).toSpanStyle()
    val titleStyle = IssueTypo.ExtraBold15.copy(color = Title).toSpanStyle()

    if (writerName.isNullOrBlank()) {
        withStyle(titleStyle) {
            append(pinTitle)
        }
        withStyle(bodyStyle) {
            append("에 대해 공감을 요청하고 있어요")
        }
        return@buildAnnotatedString
    }

    withStyle(authorStyle) {
        append(writerName)
    }
    withStyle(bodyStyle) {
        append("님이 ")
    }
    withStyle(titleStyle) {
        append(pinTitle)
    }
    withStyle(bodyStyle) {
        append("에 대해 공감을 요청했어요")
    }
}

@Composable
private fun ShopSympathyRequestSection(
    content: PinPostSympathyContent,
    onSympathyClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        ProfileImageFrame(
            size = 66.dp,
            imageUrl = content.avatarImageUrl,
        )

        Column(modifier = Modifier.weight(1f)) {
            SpeechBubble {
                val discount = content.discount
                if (!discount.isNullOrBlank()) {
                    Text(
                        text = discount,
                        style = IssueTypo.ExtraBold15.copy(color = Title),
                        lineHeight = 22.sp,
                    )
                } else if (content.pinTitle.isNotBlank()) {
                    Text(
                        text = content.pinTitle,
                        style = IssueTypo.ExtraBold15.copy(color = Title),
                        lineHeight = 22.sp,
                    )
                }
                SympathyPill(
                    sympathyCount = content.sympathyCount,
                    isSympathizedByMe = content.isSympathizedByMe,
                    onClick = onSympathyClick,
                )
            }
        }
    }
}

@Composable
private fun SpeechBubble(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val bubbleShape = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 18.dp,
        bottomEnd = 18.dp,
        bottomStart = 18.dp
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Gray_3, bubbleShape)
            .background(Gray_1, bubbleShape)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
    }
}

@Composable
private fun PostCoverImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "핀 대표 이미지",
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun SympathyRequestSection(
    content: PinPostSympathyContent,
    onSympathyClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        ProfileImageFrame(
            size = 66.dp,
            imageUrl = content.avatarImageUrl,
        )

        Column(modifier = Modifier.weight(1f)) {
            SpeechBubble {
                Text(
                    text = buildSympathyRequestMessage(
                        writerName = content.writer?.name,
                        pinTitle = content.pinTitle,
                    ),
                    style = IssueTypo.Regular15.copy(color = Title),
                    lineHeight = 22.sp
                )
                SympathyPill(
                    sympathyCount = content.sympathyCount,
                    isSympathizedByMe = content.isSympathizedByMe,
                    onClick = onSympathyClick
                )
            }
        }
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
    postEmojis: PinDetailPostEmojis,
    onAddEmojiClick: () -> Unit,
    onEmojiChipClick: (Long) -> Unit
) {
    val visibleChips = postEmojis.visibleChips

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (visibleChips.isEmpty()) {
            Text(
                text = "아직 이모지 반응이 없어요.",
                style = IssueTypo.Regular12.copy(color = Gray_5)
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            visibleChips.forEach { chip ->
                    EmojiReactionChip(
                        chip = chip,
                        onClick = { onEmojiChipClick(chip.emojiId) },
                    )
                }
            AddEmojiChip(onClick = onAddEmojiClick)
        }
    }
}

@Composable
private fun EmojiReactionChip(
    chip: PinDetailEmojiChip,
    onClick: () -> Unit
) {
    val backgroundColor = if (chip.isMine) {
        BrandColor.copy(alpha = 0.12f)
    } else {
        Gray_3
    }
    val borderColor = if (chip.isMine) BrandColor else Color.Transparent

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!chip.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = chip.imageUrl,
                contentDescription = "이모지",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Gray_4),
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${chip.count}",
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

@Composable
private fun CommentList(
    comments: List<PinComment>,
    isLoading: Boolean,
    editingCommentId: Long?,
    onCommentEdit: (Long) -> Unit,
    onCommentDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 28.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "댓글",
            style = IssueTypo.Bold18.copy(color = Title)
        )
        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading && comments.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "댓글을 불러오는 중...",
                        style = IssueTypo.Regular15.copy(color = Gray_5),
                    )
                }
            }

            comments.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "첫 댓글을 남겨보세요!",
                        style = IssueTypo.Regular15.copy(color = Gray_5),
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(comments, key = { it.commentId }) { comment ->
                        CommentItem(
                            authorName = comment.nickname,
                            authorImageUrl = comment.profileImageUrl,
                            content = comment.content,
                            edited = comment.edited,
                            isMine = comment.isMine,
                            isEditing = comment.commentId == editingCommentId,
                            onEditClick = { onCommentEdit(comment.commentId) },
                            onDeleteClick = { onCommentDelete(comment.commentId) },
                        )
                    }
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
    edited: Boolean,
    isMine: Boolean,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        ProfileImageFrame(
            size = 25.dp,
            imageUrl = authorImageUrl,
            contentDescription = "댓글 작성자 프로필",
            borderWidth = 1.dp
        )
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
                if (edited) {
                    Text(
                        text = "수정됨",
                        style = IssueTypo.Regular12.copy(color = Gray_5),
                    )
                }
                if (isMine) {
                    Text(
                        text = "수정",
                        style = IssueTypo.Regular12.copy(
                            color = if (isEditing) BrandColor else Gray_5,
                        ),
                        modifier = Modifier.clickable(onClick = onEditClick),
                    )
                    Text(
                        text = "삭제",
                        style = IssueTypo.Regular12.copy(color = Gray_5),
                        modifier = Modifier.clickable(onClick = onDeleteClick),
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

@Composable
private fun CommentInputBar(
    resetKey: Int,
    editingCommentId: Long?,
    initialText: String,
    enabled: Boolean,
    isEditing: Boolean,
    onCancelEdit: () -> Unit,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var input by remember(resetKey, editingCommentId) { mutableStateOf(initialText) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "댓글 수정 중",
                    style = IssueTypo.Regular12.copy(color = BrandColor),
                )
                Text(
                    text = "취소",
                    style = IssueTypo.Regular12.copy(color = Gray_5),
                    modifier = Modifier.clickable(onClick = onCancelEdit),
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
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
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                enabled = enabled,
                textStyle = IssueTypo.Regular15.copy(color = Title),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (input.isEmpty()) {
                        Text(
                            text = if (isEditing) "수정할 댓글을 입력하세요" else "댓글 입력",
                            style = IssueTypo.Regular15.copy(color = Gray_5),
                        )
                    }
                    innerTextField()
                },
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BrandColor)
                .clickable(enabled = enabled) {
                    val content = input.trim()
                    if (content.isNotEmpty()) {
                        onSubmit(content)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = if (isEditing) "댓글 수정" else "댓글 전송",
                tint = White,
                modifier = Modifier.size(20.dp)
            )
        }
        }
    }
}

@Preview(name = "POST · 빈 상태", showBackground = true, heightDp = 900)
@Composable
private fun PinPostTabPreview_Empty() {
    IssueissyuTheme {
        PinPostTab(
            sympathy = PinSamples.findById(PinSamples.IssuePinId).toPostSympathyContent().copy(
                writer = null,
                pinTitle = "",
            ),
            postEmojis = PinDetailPostEmojis(),
            comments = emptyList(),
            isCommentsLoading = false,
            isCommentSubmitting = false,
            commentInputRevision = 0,
            editingCommentId = null,
            onSympathyClick = {},
            onEmojiClick = {},
            onEmojiChipClick = {},
            onCommentSubmit = {},
            onCommentEdit = {},
            onCommentEditCancel = {},
            onCommentDelete = {},
        )
    }
}

@Preview(name = "POST · 데이터 있음", showBackground = true, heightDp = 900)
@Composable
private fun PinPostTabPreview_Filled() {
    val samplePin = PinSamples.findById(PinSamples.ShopPinId)
    IssueissyuTheme {
        PinPostTab(
            sympathy = PinPostSympathyContent(
                pinId = samplePin.id.toLongOrNull() ?: 1L,
                pinType = PinCategory.SHOP,
                pinTitle = samplePin.title,
                sympathyCount = samplePin.sympathyCount,
                isSympathizedByMe = samplePin.isSympathizedByMe,
                writer = null,
                discount = "전 품목 50% 할인",
                storeImageUrl = samplePin.imageUrls.firstOrNull(),
            ),
            postEmojis = PinDetailPostEmojis(
                chips = samplePin.emojiReactions.map { reaction ->
                    PinDetailEmojiChip(
                        emojiId = reaction.emojiId.toLongOrNull() ?: 0L,
                        count = reaction.count,
                        imageUrl = reaction.emojiImageUrl,
                        isMine = reaction.reactedByMe,
                    )
                },
            ),
            comments = emptyList(),
            isCommentsLoading = false,
            isCommentSubmitting = false,
            commentInputRevision = 0,
            editingCommentId = null,
            onSympathyClick = {},
            onEmojiClick = {},
            onEmojiChipClick = {},
            onCommentSubmit = {},
            onCommentEdit = {},
            onCommentEditCancel = {},
            onCommentDelete = {}
        )
    }
}
