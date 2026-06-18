package com.issueissyu.fe.ui.components

import com.issueissyu.fe.domain.model.pin.PinEditRateLimitQuota

data class RemainingQuotaDialogContent(
    val title: String,
    val countLabel: String?,
    val description: String,
)

fun PinEditRateLimitQuota?.toAiDraftConfirmDialogContent(): RemainingQuotaDialogContent {
    if (this == null || !enabled) {
        return RemainingQuotaDialogContent(
            title = "AI 글쓰기",
            countLabel = null,
            description = "이대로 AI 글쓰기를 진행하시겠습니까?",
        )
    }
    return RemainingQuotaDialogContent(
        title = "남은 자동 글쓰기 횟수",
        countLabel = "$remainingCount/$dailyLimit",
        description = "자동 글쓰기는 정해진 일일 한도 내에서만 사용 가능합니다!",
    )
}

fun PinEditRateLimitQuota?.toHomeEditConfirmDialogContent(): RemainingQuotaDialogContent {
    if (this == null || !enabled) {
        return RemainingQuotaDialogContent(
            title = "이슈 핀 수정",
            countLabel = null,
            description = "이대로 수정하시겠습니까?",
        )
    }
    return RemainingQuotaDialogContent(
        title = "남은 이슈 핀 수정 횟수",
        countLabel = "$remainingCount/$dailyLimit",
        description = "이슈 핀 수정은 정해진 일일 한도 내에서만 사용 가능합니다!",
    )
}
