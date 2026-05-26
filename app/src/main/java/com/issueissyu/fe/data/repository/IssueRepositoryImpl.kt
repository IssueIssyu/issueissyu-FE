package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.IssueApiService
import com.issueissyu.fe.data.remote.dto.response.issue.toDomain
import com.issueissyu.fe.domain.model.issue.IssueReliability
import com.issueissyu.fe.domain.repository.IssueRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssueRepositoryImpl @Inject constructor(
    private val apiService: IssueApiService,
) : IssueRepository {

    override suspend fun getIssueReliability(pinId: Long): Result<IssueReliability> {
        return try {
            val response = apiService.getIssueReliability(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(response.message.ifBlank { "이슈 신뢰도 응답이 올바르지 않습니다." }),
                    )
                Result.success(result.toDomain())
            } else {
                Result.failure(Exception(response.message.ifBlank { "이슈 신뢰도 조회에 실패했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
