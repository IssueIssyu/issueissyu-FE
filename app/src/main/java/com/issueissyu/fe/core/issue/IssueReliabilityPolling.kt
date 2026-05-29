package com.issueissyu.fe.core.issue

import com.issueissyu.fe.domain.model.issue.IssueReliability
import com.issueissyu.fe.domain.model.issue.IssueReliabilityStatus
import kotlinx.coroutines.delay

object IssueReliabilityPolling {
    private const val POLL_INTERVAL_MS = 30_000L
    /** AI 기준 약 5분 내 미완료 시 reliabilityStatus=failed 로 내려온다. */
    private const val MAX_POLL_DURATION_MS = 5 * 60 * 1_000L + 30_000L
    private const val MAX_ATTEMPTS = (MAX_POLL_DURATION_MS / POLL_INTERVAL_MS).toInt()

    /**
     * AI 신뢰도가 [IssueReliabilityStatus.PENDING]일 때 주기적으로 재조회한다.
     * [IssueReliabilityStatus.COMPLETED] 또는 [IssueReliabilityStatus.FAILED]가 되면 종료한다.
     */
    suspend fun fetchWithPolling(
        fetch: suspend () -> Result<IssueReliability>,
        onUpdate: suspend (IssueReliability) -> Unit,
    ) {
        repeat(MAX_ATTEMPTS) { attempt ->
            val reliability = fetch().getOrElse { return }
            onUpdate(reliability)
            if (reliability.status != IssueReliabilityStatus.PENDING) return
            if (attempt < MAX_ATTEMPTS - 1) {
                delay(POLL_INTERVAL_MS)
            }
        }
    }
}
