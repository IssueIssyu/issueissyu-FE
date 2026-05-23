package com.issueissyu.fe.ui.screens.community

internal fun String.toRegionDisplayName(): String {
    return substringAfterLast(" ").ifBlank { this }
}
