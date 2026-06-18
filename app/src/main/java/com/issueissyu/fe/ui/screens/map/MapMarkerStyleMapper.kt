package com.issueissyu.fe.ui.screens.map

import com.issueissyu.fe.R
import com.issueissyu.fe.domain.model.pin.PinCategory

fun PinCategory.toMarkerIconRes(): Int {
    return when (this) {
        PinCategory.ISSUE -> R.drawable.ic_issue
        PinCategory.COMMUNICATION -> R.drawable.communicate
        PinCategory.SHOP -> R.drawable.shop
        PinCategory.FESTIVAL -> R.drawable.festival
    }
}

fun PinCategory.markerScaleMultiplier(): Float {
    return when (this) {
        PinCategory.ISSUE -> 0.92f
        PinCategory.SHOP -> 0.9f
        else -> 1f
    }
}