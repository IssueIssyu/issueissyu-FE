package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.CollectionApi
import com.issueissyu.fe.data.remote.dto.collection.toMyPageCollectionSummary
import com.issueissyu.fe.domain.model.mypage.MyPageCollectionSummary
import com.issueissyu.fe.domain.repository.CollectionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val collectionApi: CollectionApi,
) : CollectionRepository {

    override suspend fun getCollections(checkUnlock: Boolean): Result<MyPageCollectionSummary> {
        return try {
            val response = collectionApi.getCollections(checkUnlock = checkUnlock)
            if (response.isSuccess) {
                val body = response.result
                    ?: return Result.failure(
                        Exception(response.message.ifBlank { "컬렉션 응답이 올바르지 않습니다." }),
                    )
                Result.success(body.toMyPageCollectionSummary())
            } else {
                Result.failure(
                    Exception(response.message.ifBlank { "컬렉션 조회에 실패했습니다." }),
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
