package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.issue.IssueAiDraft
import com.issueissyu.fe.domain.model.issue.IssueReliability
import com.issueissyu.fe.domain.model.issue.IssueToneType
import com.issueissyu.fe.domain.model.pin.PinEditRateLimitQuota

interface IssueRepository {
    // suspend fun getIssues(): Flow<List<Issue>>
    suspend fun getIssueToneTypes(): Result<List<IssueToneType>>

    suspend fun createIssueAiDraft(
        title: String,
        content: String,
        tone: String,
        latitude: Double,
        longitude: Double,
    ): Result<IssueAiDraft>

    suspend fun getIssueAiDraftQuota(): Result<PinEditRateLimitQuota>

    suspend fun getIssueReliability(pinId: Long): Result<IssueReliability>
}
