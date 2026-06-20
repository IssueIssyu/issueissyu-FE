package com.issueissyu.fe.data.remote.dto

internal fun String.decodeEscapedNewlines(): String {
    return replace("\\r\\n", "\n")
        .replace("\\n", "\n")
        .replace("\\r", "\n")
}
