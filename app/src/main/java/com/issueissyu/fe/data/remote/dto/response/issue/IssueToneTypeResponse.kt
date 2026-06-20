package com.issueissyu.fe.data.remote.dto.response.issue

import com.issueissyu.fe.domain.model.issue.IssueToneType

data class IssueToneTypeResponse(
    val key: String = "",
    val label: String = "",
)

fun IssueToneTypeResponse.toDomain(): IssueToneType? {
    val normalizedLabel = label.takeIf { it.isNotBlank() } ?: return null
    return IssueToneType(
        key = key.takeIf { it.isNotBlank() },
        label = normalizedLabel,
    )
}
