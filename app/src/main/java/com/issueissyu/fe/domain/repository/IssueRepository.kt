package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.issue.IssueAiDraft
import com.issueissyu.fe.domain.model.issue.IssueReliability

interface IssueRepository {
    // suspend fun getIssues(): Flow<List<Issue>>
    suspend fun createIssueAiDraft(
        title: String,
        content: String,
        tone: String,
        latitude: Double,
        longitude: Double,
    ): Result<IssueAiDraft>

    suspend fun getIssueReliability(pinId: Long): Result<IssueReliability>
}
