package com.issueissyu.fe.data.model

import androidx.annotation.DrawableRes

data class DemoPin(
    val id: String,
    val name: String,
    @DrawableRes val imageResId: Int,
    val imageUrl: String,
    val isLocked: Boolean = false,
    val unlockCondition: String? = null
)
