package com.issueissyu.fe.data.remote.dto.response.pin

data class PinEmojisResponse(
    val selectedEmojiId: Int? = null,
    val emojis: List<PinEmojiDto>? = null,
)
