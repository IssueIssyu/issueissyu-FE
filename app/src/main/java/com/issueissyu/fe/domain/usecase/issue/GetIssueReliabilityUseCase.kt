package com.issueissyu.fe.domain.usecase.issue

import com.issueissyu.fe.domain.model.issue.IssueReliability
import com.issueissyu.fe.domain.repository.IssueRepository
import javax.inject.Inject

class GetIssueReliabilityUseCase @Inject constructor(
    private val repository: IssueRepository,
) {
    suspend operator fun invoke(pinId: Long): Result<IssueReliability> {
        return repository.getIssueReliability(pinId)
    }
}
