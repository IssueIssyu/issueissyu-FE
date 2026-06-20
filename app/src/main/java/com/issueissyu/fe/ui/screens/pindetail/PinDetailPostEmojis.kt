package com.issueissyu.fe.ui.screens.pindetail

import com.issueissyu.fe.domain.model.pin.PinEmojis

data class PinDetailPostEmojis(
    val myEmojiId: Long? = null,
    val chips: List<PinDetailEmojiChip> = emptyList()
) {
    // 반응 수 0 숨김, 많은 순 정렬
    val visibleChips: List<PinDetailEmojiChip>
        get() = chips
            .filter { it.count > 0 }
            .sortedByDescending { it.count }
}

// 이모지 칩
data class PinDetailEmojiChip(
    val emojiId: Long,
    val count: Int,
    val imageUrl: String?,
    val isMine: Boolean
)

// GET 조회 결과 -> 포스트 탭 칩 목록
fun PinEmojis.toPinDetailPostEmojis(
    emojiImageLookup: (Long) -> String? = { null }
): PinDetailPostEmojis {
    val mine = selectedEmojiId
    val chips = emojis.map { emoji ->
        PinDetailEmojiChip(
            emojiId = emoji.emojiId,
            count = emoji.count,
            imageUrl = emoji.emojiImageUrl.takeIf { it.isNotBlank() },
            isMine = emoji.emojiId == mine
        )
    }.toMutableList()

    // 서버 목록에 없는 내 반응도 칩으로 표시
    if (mine != null && chips.none { it.emojiId == mine }) {
        chips += PinDetailEmojiChip(
            emojiId = mine,
            count = 1,
            imageUrl = emojiImageLookup(mine),
            isMine = true
        )
    }

    return PinDetailPostEmojis(
        myEmojiId = mine,
        chips = chips
    )
}
