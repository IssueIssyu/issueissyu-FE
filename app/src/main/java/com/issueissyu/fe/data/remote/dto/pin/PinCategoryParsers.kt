package com.issueissyu.fe.data.remote.dto.pin

import com.issueissyu.fe.domain.model.pin.PinCategory

fun String?.toPinCategoryOrNull(): PinCategory? {
    return when (this?.trim()?.uppercase()) {
        "ISSUE" -> PinCategory.ISSUE
        "COMMUNICATION" -> PinCategory.COMMUNICATION
        "STORE", "SHOP" -> PinCategory.SHOP
        "FESTIVAL" -> PinCategory.FESTIVAL
        null, "" -> null
        else -> null
    }
}

fun String?.toUnsupportedPinTypeMessage(): String {
    val rawType = this?.trim().orEmpty()
    return if (rawType.isBlank()) {
        "핀 타입 정보가 비어 있습니다."
    } else {
        "지원하지 않는 핀 타입입니다: $rawType"
    }
}
