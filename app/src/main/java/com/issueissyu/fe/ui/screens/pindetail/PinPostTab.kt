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
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.annotation.DrawableRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.issueissyu.fe.R
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinComment
import com.issueissyu.fe.domain.model.pin.PinPostSympathyContent
import com.issueissyu.fe.core.time.formatPinCommentCreatedAt
import com.issueissyu.fe.data.sample.PinSamples
import com.issueissyu.fe.domain.model.pin.toPostSympathyContent
import com.issueissyu.fe.ui.components.ProfileImageFrame
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_1
import com.issueissyu.fe.ui.theme.Gray_2
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_4
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Orange
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
                .padding(20.dp, 50.dp, 20.dp, 0.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                }
            }

            Text(
                text = "반응",
                style = IssueTypo.ExtraBold18.copy(color = Title),
            )
            EmojiReactionRow(
                postEmojis = postEmojis,
                onAddEmojiClick = onEmojiClick,
                onEmojiChipClick = onEmojiChipClick,
            )
        }

        HorizontalDivider(color = Gray_2, thickness = 1.dp, modifier = Modifier.padding(20.dp))

        CommentList(
            comments = comments,
            isLoading = isCommentsLoading,
            editingCommentId = editingCommentId,
            onCommentEdit = onCommentEdit,
            onCommentDelete = onCommentDelete,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(0.dp,0.dp, 0.dp, 20.dp)
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
                .padding(start = 20.dp, end = 20.dp, bottom = 30.dp)
        )
    }
}

private fun buildSympathyRequestMessage(
    pinType: PinCategory,
    writerName: String?,
    pinTitle: String,
) = buildAnnotatedString {
    val bodyStyle = IssueTypo.Regular15.copy(color = Title).toSpanStyle()
    val authorStyle = IssueTypo.ExtraBold15.copy(color = BrandColor).toSpanStyle()
    val titleStyle = IssueTypo.ExtraBold15.copy(color = Title).toSpanStyle()
    val safeWriterName = writerName?.takeIf { it.isNotBlank() }
    val safeTitle = pinTitle.takeIf { it.isNotBlank() }

    when (pinType) {
        PinCategory.SHOP -> {
            safeTitle?.let { title ->
                withStyle(titleStyle) { append(title) }
                withStyle(bodyStyle) { append("님이 공감을 요청했어요.") }
            } ?: withStyle(bodyStyle) { append("공감을 요청했어요.") }
        }

        PinCategory.FESTIVAL -> {
            safeTitle?.let { title ->
                withStyle(titleStyle) { append(title) }
                withStyle(bodyStyle) { append("에 대한 공감을 요청했어요.") }
            } ?: withStyle(bodyStyle) { append("공감을 요청했어요.") }
        }

        PinCategory.ISSUE,
        PinCategory.COMMUNICATION -> {
            safeWriterName?.let { name ->
                withStyle(authorStyle) { append(name) }
                withStyle(bodyStyle) { append(" 님이 ") }
            }
            safeTitle?.let { title ->
                withStyle(titleStyle) { append(title) }
                withStyle(bodyStyle) { append("에 대한 공감을 요청했어요.") }
            } ?: withStyle(bodyStyle) {
                append("공감을 요청했어요.")
            }
        }
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
            imageUrl = content.detailDisplayProfile().imageUrl,
        )

        Column(modifier = Modifier.weight(1f)) {
            SpeechBubble {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = buildSympathyRequestMessage(
                            pinType = PinCategory.SHOP,
                            writerName = content.writer?.name,
                            pinTitle = content.pinTitle,
                        ),
                        style = IssueTypo.Regular15.copy(color = Title),
                        lineHeight = 22.sp,
                    )
                    content.discount?.takeIf { it.isNotBlank() }?.let { discount ->
                        ShopDiscountBanner(text = discount)
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
}

@Composable
private fun ShopDiscountBanner(
    text: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(White)
            .border(width = 1.dp, color = Orange, shape = shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Orange),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "%",
                style = IssueTypo.Bold12.copy(color = White),
            )
        }
        Text(
            text = text,
            style = IssueTypo.Bold12.copy(color = Title),
            modifier = Modifier.weight(1f),
            maxLines = 2,
        )
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
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = bubbleShape),
        shape = bubbleShape,
        color = Gray_1,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
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
            imageUrl = content.detailDisplayProfile().imageUrl,
        )

        Column(modifier = Modifier.weight(1f)) {
            SpeechBubble {
                Text(
                    text = buildSympathyRequestMessage(
                        pinType = content.pinType,
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

private val EmojiReactionChipHeight = 38.dp
private val EmojiReactionStatusHeight = 18.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmojiReactionRow(
    postEmojis: PinDetailPostEmojis,
    onAddEmojiClick: () -> Unit,
    onEmojiChipClick: (Long) -> Unit
) {
    val visibleChips = postEmojis.visibleChips

    Column() {
        Box(
            contentAlignment = Alignment.TopStart,
        ) {
            if (visibleChips.isEmpty()) {
                Text(
                    text = "아직 이모지 반응이 없어요.",
                    style = IssueTypo.Regular12.copy(color = Gray_5),
                    modifier = Modifier.padding(0.dp, 8.dp)
                )
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            .height(EmojiReactionChipHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!chip.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = chip.imageUrl,
                contentDescription = "이모지",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(5.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color = Gray_4, RoundedCornerShape(5.dp)),
            )
        }
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "${chip.count}",
            style = IssueTypo.Regular16.copy(color = TextColor)
        )
    }
}

@Composable
private fun AddEmojiChip(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(EmojiReactionChipHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(Gray_3)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "이모지 추가",
            tint = Gray_6,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "이모지",
            style = IssueTypo.Regular16.copy(color = TextColor)
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
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "댓글 ${comments.size}",
            style = IssueTypo.ExtraBold18.copy(color = Title),
        )
        Spacer(modifier = Modifier.height(10.dp))

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
                CommentEmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
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
                            createdAt = comment.createdAt,
                            isMine = comment.isMine,
                            onEditClick = { onCommentEdit(comment.commentId) },
                            onDeleteClick = { onCommentDelete(comment.commentId) },
                        )
                    }
                }
            }
        }
    }
}

private val CommentBubbleShape = RoundedCornerShape(
    topStart = 4.dp,
    topEnd = 16.dp,
    bottomEnd = 16.dp,
    bottomStart = 16.dp,
)

@Composable
private fun CommentItem(
    authorName: String,
    authorImageUrl: String?,
    content: String,
    edited: Boolean,
    createdAt: String,
    isMine: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val formattedTime = formatPinCommentCreatedAt(createdAt)
    val timeLabel = if (edited) "$formattedTime · 수정됨" else formattedTime
    val timeStyle = IssueTypo.Regular12.copy(color = Gray_5)
    val timeGap = 8.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ProfileImageFrame(
                size = 25.dp,
                imageUrl = authorImageUrl,
                contentDescription = "댓글 작성자 프로필",
                borderWidth = 1.dp,
            )
            Text(
                text = authorName,
                style = IssueTypo.Bold12.copy(color = Title),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }

        CommentBubbleWithTimestamp(
            timeLabel = timeLabel,
            timeStyle = timeStyle,
            timeGap = timeGap,
        ) {
                CommentBubble(
                    content = content,
                    isMine = isMine,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                )
        }
    }
}

@Composable
private fun CommentBubbleWithTimestamp(
    timeLabel: String,
    timeStyle: TextStyle,
    timeGap: Dp,
    modifier: Modifier = Modifier,
    bubbleContent: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = {
            bubbleContent()
            Text(
                text = timeLabel,
                style = timeStyle,
            )
        },
    ) { measurables, constraints ->
        val bubbleMeasurable = measurables[0]
        val timeMeasurable = measurables[1]
        val gapPx = timeGap.roundToPx()

        val timePlaceable = timeMeasurable.measure(
            Constraints(
                maxWidth = constraints.maxWidth,
                maxHeight = constraints.maxHeight,
            )
        )
        val bubbleMaxWidth = (constraints.maxWidth - timePlaceable.width - gapPx).coerceAtLeast(0)
        val bubblePlaceable = bubbleMeasurable.measure(
            Constraints(
                maxWidth = bubbleMaxWidth,
                maxHeight = constraints.maxHeight,
            )
        )

        val layoutWidth = (bubblePlaceable.width + gapPx + timePlaceable.width)
            .coerceIn(constraints.minWidth, constraints.maxWidth)
        val layoutHeight = maxOf(
            constraints.minHeight,
            bubblePlaceable.height,
            timePlaceable.height,
        )
        val timeX = (bubblePlaceable.width + gapPx)
            .coerceAtMost(layoutWidth - timePlaceable.width)
            .coerceAtLeast(0)

        layout(layoutWidth, layoutHeight) {
            bubblePlaceable.placeRelative(
                x = 0,
                y = layoutHeight - bubblePlaceable.height,
            )
            timePlaceable.placeRelative(
                x = timeX,
                y = layoutHeight - timePlaceable.height,
            )
        }
    }
}

@Composable
private fun CommentBubble(
    content: String,
    isMine: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bubblePadding = 12.dp
    val textStyle = IssueTypo.Regular15.copy(
        color = White,
        fontWeight = Medium,
    )

    Box(
        modifier = modifier
            .clip(CommentBubbleShape)
            .background(BrandColor)
            .padding(bubblePadding),
    ) {
        if (isMine) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = content,
                    style = textStyle,
                )
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CommentBubbleActionIcon(
                        iconRes = R.drawable.ic_edit,
                        contentDescription = "댓글 수정",
                        onClick = onEditClick,
                    )
                    CommentBubbleActionIcon(
                        iconRes = R.drawable.ic_delete,
                        contentDescription = "댓글 삭제",
                        onClick = onDeleteClick,
                    )
                }
            }
        } else {
            Text(
                text = content,
                style = textStyle,
            )
        }
    }
}

@Composable
private fun CommentEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.communicate),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Gray_3,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "첫 번째 댓글을 남겨보세요.",
            style = IssueTypo.Regular12.copy(color = Gray_5),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CommentBubbleActionIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .requiredSize(20.dp)
            .clip(CircleShape)
            .background(White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(12.dp),
            tint = Gray_6
        )
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
                            style = IssueTypo.Regular15.copy(color = Gray_5, fontWeight = Medium),
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

@Preview(name = "POST", showBackground = true, heightDp = 900)
@Composable
private fun PinPostTabPreview() {
    val samplePin = PinSamples.findById(PinSamples.IssuePinId)
    IssueissyuTheme {
        PinPostTab(
            sympathy = samplePin.toPostSympathyContent(),
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
            comments = listOf(
                PinComment(
                    commentId = 1,
                    nickname = "아좌",
                    profileImageUrl = null,
                    content = "와와 이거 없어요? ㄹㅇ혁명인데 이거. 헉 쓰바리",
                    edited = true,
                    createdAt = "2026-05-24T18:22:36",
                    isMine = true,
                ),
                PinComment(
                    commentId = 2,
                    nickname = "아좌",
                    profileImageUrl = null,
                    content = "테스트 2",
                    edited = false,
                    createdAt = "2026-05-24T19:42:00",
                    isMine = true,
                ),
            ),
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
