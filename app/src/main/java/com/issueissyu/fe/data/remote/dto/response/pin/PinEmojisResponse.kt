package com.issueissyu.fe.data.remote.dto.response.pin

data class PinEmojisResponse(
    val selectedEmojiId: Long? = null,
    val emojis: List<PinEmojiDto>? = null,
)
