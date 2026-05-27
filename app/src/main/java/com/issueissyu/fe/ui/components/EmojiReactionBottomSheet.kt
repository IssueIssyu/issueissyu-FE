package com.issueissyu.fe.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.issueissyu.fe.domain.model.pin.PinEmojiCandidate
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_4
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiReactionBottomSheet(
    candidates: List<PinEmojiCandidate>,
    selectedEmojiId: Long?,
    isLoading: Boolean,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onEmojiClick: (Long) -> Unit,
    onApplyClick: () -> Unit,
    allowLockedEmojiSelection: Boolean = false,
    allowApplyWithoutSelection: Boolean = false,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 46.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Gray_3)
            )
        }
    ) {
        EmojiReactionBottomSheetContent(
            candidates = candidates,
            selectedEmojiId = selectedEmojiId,
            isLoading = isLoading,
            isSubmitting = isSubmitting,
            errorMessage = errorMessage,
            onDismiss = onDismiss,
            onEmojiClick = onEmojiClick,
            onApplyClick = onApplyClick,
            allowLockedEmojiSelection = allowLockedEmojiSelection,
            allowApplyWithoutSelection = allowApplyWithoutSelection,
        )
    }
}

@Composable
fun EmojiReactionBottomSheetContent(
    candidates: List<PinEmojiCandidate>,
    selectedEmojiId: Long?,
    isLoading: Boolean,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onEmojiClick: (Long) -> Unit,
    onApplyClick: () -> Unit,
    allowLockedEmojiSelection: Boolean,
    allowApplyWithoutSelection: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 28.dp)
    ) {
        Text(
            text = "반응 추가하기",
            style = IssueTypo.Bold18.copy(color = Title)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "게시글에 대한 감정을 표현해보세요",
            style = IssueTypo.Regular15.copy(color = Gray_5)
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            isLoading -> {
                Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(236.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(236.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage,
                        style = IssueTypo.Regular15.copy(color = Gray_7)
                    )
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(236.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(candidates, key = { it.emojiId }) { candidate ->
                        EmojiCandidateButton(
                            candidate = candidate,
                            isSelected = candidate.emojiId == selectedEmojiId,
                            allowLockedEmojiSelection = allowLockedEmojiSelection,
                            onClick = { onEmojiClick(candidate.emojiId) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = White,
                    contentColor = Gray_7
                ),
                border = BorderStroke(1.dp, Gray_3)
            ) {
                Text(
                    text = "취소",
                    style = IssueTypo.Bold12.copy(color = Gray_7)
                )
            }

            Button(
                onClick = onApplyClick,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                enabled = (selectedEmojiId != null || allowApplyWithoutSelection) &&
                    !isLoading &&
                    !isSubmitting,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColor,
                    contentColor = White,
                    disabledContainerColor = Gray_4,
                    disabledContentColor = White
                )
            ) {
                Text(
                    text = when {
                        isSubmitting -> "등록 중"
                        selectedEmojiId == null && allowApplyWithoutSelection -> "반응 취소"
                        else -> "반응 남기기"
                    },
                    style = IssueTypo.Bold12.copy(color = White)
                )
            }
        }
    }
}

@Composable
private fun EmojiCandidateButton(
    candidate: PinEmojiCandidate,
    isSelected: Boolean,
    allowLockedEmojiSelection: Boolean,
    onClick: () -> Unit,
) {
    val canTap = allowLockedEmojiSelection || candidate.canReact
    Box(
        modifier = Modifier.size(52.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) BrandColor.copy(alpha = 0.12f) else Color.Transparent)
                .border(
                    width = if (isSelected) 1.dp else 0.dp,
                    color = if (isSelected) BrandColor else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(enabled = canTap) { onClick() }
                .alpha(if (canTap) 1f else 0.42f)
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = candidate.emojiImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
            )
        }

        if (!candidate.canReact) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Gray_7),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "잠김",
                    tint = White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

private fun previewEmojiCandidates(): List<PinEmojiCandidate> = listOf(
    PinEmojiCandidate(1L, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f525.png", isDefault = true, isOwned = true, productId = null),
    PinEmojiCandidate(2L, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/2764.png", isDefault = true, isOwned = true, productId = null),
    PinEmojiCandidate(3L, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f44f.png", isDefault = true, isOwned = false, productId = "p1"),
    PinEmojiCandidate(4L, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f602.png", isDefault = true, isOwned = true, productId = null),
    PinEmojiCandidate(5L, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f622.png", isDefault = false, isOwned = false, productId = "p2"),
    PinEmojiCandidate(6L, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f389.png", isDefault = true, isOwned = true, productId = null),
    PinEmojiCandidate(7L, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f60e.png", isDefault = true, isOwned = true, productId = null),
    PinEmojiCandidate(8L, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f44d.png", isDefault = false, isOwned = true, productId = null),
    PinEmojiCandidate(9L, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/2728.png", isDefault = true, isOwned = false, productId = "p3"),
    PinEmojiCandidate(10L, "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f64f.png", isDefault = true, isOwned = true, productId = null),
)

@Composable
private fun EmojiReactionBottomSheetPreviewSurface(
    content: @Composable () -> Unit,
) {
    IssueissyuTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            content()
        }
    }
}

@Preview(showBackground = true, name = "이모지 바텀시트 · 기본", heightDp = 480)
@Composable
private fun EmojiReactionBottomSheetPreview_Default() {
    EmojiReactionBottomSheetPreviewSurface {
        EmojiReactionBottomSheetContent(
            candidates = previewEmojiCandidates(),
            selectedEmojiId = 2L,
            isLoading = false,
            isSubmitting = false,
            errorMessage = null,
            onDismiss = {},
            onEmojiClick = {},
            onApplyClick = {},
            allowLockedEmojiSelection = false,
        )
    }
}

@Preview(showBackground = true, name = "이모지 바텀시트 · 로딩", heightDp = 480)
@Composable
private fun EmojiReactionBottomSheetPreview_Loading() {
    EmojiReactionBottomSheetPreviewSurface {
        EmojiReactionBottomSheetContent(
            candidates = emptyList(),
            selectedEmojiId = null,
            isLoading = true,
            isSubmitting = false,
            errorMessage = null,
            onDismiss = {},
            onEmojiClick = {},
            onApplyClick = {},
            allowLockedEmojiSelection = false,
        )
    }
}

@Preview(showBackground = true, name = "이모지 바텀시트 · 오류", heightDp = 480)
@Composable
private fun EmojiReactionBottomSheetPreview_Error() {
    EmojiReactionBottomSheetPreviewSurface {
        EmojiReactionBottomSheetContent(
            candidates = emptyList(),
            selectedEmojiId = null,
            isLoading = false,
            isSubmitting = false,
            errorMessage = "이모지 목록을 불러오지 못했습니다.",
            onDismiss = {},
            onEmojiClick = {},
            onApplyClick = {},
            allowLockedEmojiSelection = false,
        )
    }
}

@Preview(showBackground = true, name = "이모지 바텀시트 · 반응 취소", heightDp = 480)
@Composable
private fun EmojiReactionBottomSheetPreview_ClearReaction() {
    EmojiReactionBottomSheetPreviewSurface {
        EmojiReactionBottomSheetContent(
            candidates = previewEmojiCandidates(),
            selectedEmojiId = null,
            isLoading = false,
            isSubmitting = false,
            errorMessage = null,
            onDismiss = {},
            onEmojiClick = {},
            onApplyClick = {},
            allowLockedEmojiSelection = false,
            allowApplyWithoutSelection = true,
        )
    }
}
