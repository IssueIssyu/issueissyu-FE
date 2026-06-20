package com.issueissyu.fe.data.remote.dto.response.pin

import com.google.gson.annotations.SerializedName

data class PinEmojiDto(
    val emojiId: Long = 0,
    val emojiImageUrl: String = "",
    val count: Int = 0,
    @SerializedName("default")
    val isDefault: Boolean = false,
    val owned: Boolean = false,
    val productId: String? = null,
)
