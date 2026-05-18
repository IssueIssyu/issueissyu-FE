package com.issueissyu.fe.data.remote.dto.response.pin

data class PinEmojiDto(
    val emojiId: Int = 0,
    val emojiImageUrl: String = "",
    val count: Int = 0,
    val isDefault: Boolean = false,
    val isOwned: Boolean = false,
    val productId: String? = null,
)
