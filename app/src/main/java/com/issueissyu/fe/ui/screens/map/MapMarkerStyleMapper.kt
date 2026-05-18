package com.issueissyu.fe.ui.screens.map

import com.issueissyu.fe.R
import com.issueissyu.fe.domain.model.PinCategory

fun PinCategory.toMarkerIconRes(): Int {
    return when (this) {
        PinCategory.ISSUE -> R.drawable.issue
        PinCategory.COMMUNICATION -> R.drawable.communicate
        PinCategory.SHOP -> R.drawable.shop
        PinCategory.FESTIVAL -> R.drawable.festival
    }
}