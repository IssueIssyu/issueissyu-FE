package com.issueissyu.fe.ui.components

import com.issueissyu.fe.domain.model.issue.IssueAiDraft
import com.issueissyu.fe.domain.model.pin.PinEditRateLimitQuota
import com.issueissyu.fe.domain.repository.IssueRepository

sealed interface IssueAiDraftQuotaResult {
    data class Ready(val quota: PinEditRateLimitQuota) : IssueAiDraftQuotaResult

    data object Exceeded : IssueAiDraftQuotaResult

    data class Failed(val message: String) : IssueAiDraftQuotaResult
}

object IssueAiDraftFlow {
    suspend fun loadQuota(issueRepository: IssueRepository): IssueAiDraftQuotaResult {
        return issueRepository.getIssueAiDraftQuota()
            .fold(
                onSuccess = { quota ->
                    if (quota.enabled && quota.remainingCount <= 0) {
                        IssueAiDraftQuotaResult.Exceeded
                    } else {
                        IssueAiDraftQuotaResult.Ready(quota)
                    }
                },
                onFailure = { error ->
                    IssueAiDraftQuotaResult.Failed(
                        error.message ?: "AI 글쓰기 제한 횟수 조회에 실패했습니다.",
                    )
                },
            )
    }

    suspend fun createDraft(
        issueRepository: IssueRepository,
        title: String,
        content: String,
        tone: String,
        latitude: Double,
        longitude: Double,
    ): Result<IssueAiDraft> {
        return issueRepository.createIssueAiDraft(
            title = title,
            content = content,
            tone = tone,
            latitude = latitude,
            longitude = longitude,
        )
    }
}
