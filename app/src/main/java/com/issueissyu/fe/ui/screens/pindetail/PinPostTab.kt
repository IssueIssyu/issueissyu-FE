package com.issueissyu.fe.ui.screens.pindetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.data.model.PinEmojiReaction
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_4
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Text as TextColor
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White

// TODO: currentUserId는 추후 댓글 작성자 / 내 이모지 반응 식별 등에 사용된다.
//       현재는 공감·이모지·댓글 placeholder 단계라 직접 사용처는 없지만, 호출부 시그니처 안정성을 위해 그대로 받는다.
@Composable
fun PinPostTab(
    pin: Pin,
    @Suppress("UNUSED_PARAMETER") currentUserId: String,
    onSympathyClick: (String) -> Unit,
    onEmojiClick: (String) -> Unit,
    onCommentSubmit: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SympathySection(
            sympathyCount = pin.sympathyCount,
            isSympathizedByMe = pin.isSympathizedByMe,
            onClick = { onSympathyClick(pin.id) }
        )

        EmojiReactionSection(
            reactions = pin.emojiReactions,
            onAddEmojiClick = { onEmojiClick(pin.id) }
        )

        CommentSection(
            pinId = pin.id,
            onCommentSubmit = onCommentSubmit
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
private fun SympathySection(
    sympathyCount: Int,
    isSympathizedByMe: Boolean,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("공감")
        Row(verticalAlignment = Alignment.CenterVertically) {
            SympathyButton(
                sympathyCount = sympathyCount,
                isSympathizedByMe = isSympathizedByMe,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "공감 ${sympathyCount}명",
                style = IssueTypo.Regular15.copy(color = Gray_6)
            )
        }
    }
}

@Composable
private fun SympathyButton(
    sympathyCount: Int,
    isSympathizedByMe: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSympathizedByMe) BrandColor else White
    val contentColor = if (isSympathizedByMe) White else BrandColor
    val borderColor = if (isSympathizedByMe) BrandColor else Gray_4

    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ThumbUp,
            contentDescription = "공감",
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isSympathizedByMe) "공감 취소" else "공감하기",
            style = IssueTypo.Bold12.copy(color = contentColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$sympathyCount",
            style = IssueTypo.Bold12.copy(color = contentColor)
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun EmojiReactionSection(
    reactions: List<PinEmojiReaction>,
    onAddEmojiClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("이모지 반응")
        if (reactions.isEmpty()) {
            Text(
                text = "아직 이모지 반응이 없습니다.",
                style = IssueTypo.Regular15.copy(color = Gray_6)
            )
            AddEmojiChip(onClick = onAddEmojiClick)
        } else {
            FlowRow(
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
        // TODO: emojiId → 실제 이모지 이미지/유니코드 매핑이 정해지면 placeholder를 교체.
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

// TODO: PinCommentRepository 연결 후 댓글 목록 조회 (Pin 내부에 댓글을 두지 않고 별도 모델/Repository로 분리 예정)
// TODO: CommonTextField 기반 댓글 입력 UI 연결 시 onCommentSubmit(pinId, content)에 실제 입력값 전달
@Composable
private fun CommentSection(
    @Suppress("UNUSED_PARAMETER") pinId: String,
    @Suppress("UNUSED_PARAMETER") onCommentSubmit: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("댓글")
        Text(
            text = "댓글 기능은 추후 구현 예정입니다.",
            style = IssueTypo.Regular15.copy(color = Gray_6)
        )
        CommentInputPlaceholder()
        CommentListPlaceholder()
    }
}

@Composable
private fun CommentInputPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(White)
            .border(1.dp, Gray_4, RoundedCornerShape(15.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "댓글을 입력하세요. (추후 CommonTextField 적용 예정)",
            style = IssueTypo.Regular15.copy(color = Gray_5)
        )
    }
}

@Composable
private fun CommentListPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Gray_3.copy(alpha = 0.4f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "댓글 목록 자리",
            style = IssueTypo.Regular15.copy(color = Gray_6),
            lineHeight = 20.sp
        )
    }
}
