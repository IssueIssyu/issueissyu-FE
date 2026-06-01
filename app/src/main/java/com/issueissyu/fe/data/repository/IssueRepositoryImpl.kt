package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.AiIssueApiService
import com.issueissyu.fe.data.remote.dto.response.issue.toDomain
import com.issueissyu.fe.domain.model.issue.IssueAiDraft
import com.issueissyu.fe.domain.model.issue.IssueReliability
import com.issueissyu.fe.domain.model.issue.IssueToneType
import com.issueissyu.fe.domain.repository.IssueRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssueRepositoryImpl @Inject constructor(
    private val aiIssueApiService: AiIssueApiService,
) : IssueRepository {

    override suspend fun getIssueToneTypes(): Result<List<IssueToneType>> {
        return try {
            val response = aiIssueApiService.getIssueToneTypes()
            if (response.isSuccess) {
                val tones = response.result.orEmpty()
                    .mapNotNull { it.toDomain() }
                if (tones.isEmpty()) {
                    Result.failure(Exception("말투 목록이 비어 있습니다."))
                } else {
                    Result.success(tones)
                }
            } else {
                Result.failure(
                    Exception(response.message.orEmpty().ifBlank { "말투 목록을 불러오지 못했습니다." }),
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createIssueAiDraft(
        title: String,
        content: String,
        tone: String,
        latitude: Double,
        longitude: Double,
    ): Result<IssueAiDraft> {
        return try {
            val response = aiIssueApiService.createIssuePinAiDraft(
                title = title,
                content = content,
                tone = tone,
                latitude = latitude,
                longitude = longitude,
            )
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(response.message.orEmpty().ifBlank { "AI 글쓰기 응답이 올바르지 않습니다." }),
                    )
                val draft = result.toDomain()
                if (draft.content.isNullOrBlank()) {
                    Result.failure(Exception(response.message.orEmpty().ifBlank { "AI 글쓰기 결과 본문이 비어 있습니다." }))
                } else {
                    Result.success(draft)
                }
            } else {
                Result.failure(Exception(response.message.orEmpty().ifBlank { "AI 글쓰기에 실패했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getIssueReliability(pinId: Long): Result<IssueReliability> {
        return try {
            val response = aiIssueApiService.getIssueReliability(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(response.message.orEmpty().ifBlank { "이슈 신뢰도 응답이 올바르지 않습니다." }),
                    )
                Result.success(result.toDomain())
            } else {
                Result.failure(Exception(response.message.orEmpty().ifBlank { "이슈 신뢰도 조회에 실패했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
