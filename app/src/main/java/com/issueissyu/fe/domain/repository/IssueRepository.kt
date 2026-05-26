package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.issue.IssueReliability

interface IssueRepository {
    // suspend fun getIssues(): Flow<List<Issue>>
    suspend fun getIssueReliability(pinId: Long): Result<IssueReliability>
}
