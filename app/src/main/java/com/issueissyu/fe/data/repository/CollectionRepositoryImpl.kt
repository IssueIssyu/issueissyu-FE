package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.CollectionApi
import com.issueissyu.fe.data.remote.dto.collection.toMyPageCollectionSummary
import com.issueissyu.fe.data.remote.dto.collection.toProfileCollectionUpdate
import com.issueissyu.fe.domain.model.mypage.MyPageCollectionSummary
import com.issueissyu.fe.domain.model.mypage.ProfileCollectionUpdate
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

    override suspend fun setProfile(collectionId: Int): Result<ProfileCollectionUpdate> {
        return try {
            val response = collectionApi.setProfile(collectionId = collectionId)
            when (response.code) {
                "PROFILE_COLLECTION_UPDATE_SUCCESS" -> {
                    val body = response.result
                        ?: return Result.failure(
                            Exception(
                                response.message.ifBlank { "프로필 컬렉션 변경 응답이 올바르지 않습니다." },
                            ),
                        )
                    Result.success(body.toProfileCollectionUpdate())
                }

                "CUSTOM_COLLECTION_NOT_FOUND" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "컬렉션을 찾을 수 없습니다." },
                        ),
                    )

                "CUSTOM_COLLECTION_LOCKED" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "잠금 상태인 컬렉션입니다." },
                        ),
                    )

                else ->
                    if (response.isSuccess) {
                        val body = response.result
                            ?: return Result.failure(
                                Exception(
                                    response.message.ifBlank { "프로필 컬렉션 변경 응답이 올바르지 않습니다." },
                                ),
                            )
                        Result.success(body.toProfileCollectionUpdate())
                    } else {
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "프로필 컬렉션 변경에 실패했습니다." },
                            ),
                        )
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
