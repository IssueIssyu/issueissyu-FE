package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.CollectionApi
import com.issueissyu.fe.data.remote.dto.collection.toBookmarkCollectionUpdate
import com.issueissyu.fe.data.remote.dto.collection.toCollectionPageSummary
import com.issueissyu.fe.data.remote.dto.collection.toProfileCollectionUpdate
import com.issueissyu.fe.data.remote.dto.request.collection.SetBookmarkRequest
import com.issueissyu.fe.data.remote.dto.response.collection.GetCollectionResponse
import com.issueissyu.fe.domain.model.collection.CollectionPageSummary
import com.issueissyu.fe.domain.model.mypage.BookmarkCollectionUpdate
import com.issueissyu.fe.domain.model.mypage.ProfileCollectionUpdate
import com.issueissyu.fe.domain.repository.CollectionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val collectionApi: CollectionApi,
) : CollectionRepository {

    override suspend fun getCollections(checkUnlock: Boolean): Result<CollectionPageSummary> {
        return try {
            val response = collectionApi.getCollections(checkUnlock = checkUnlock)
            if (response.isSuccess) {
                val body = response.result
                    ?: return Result.failure(
                        Exception(response.message.ifBlank { INVALID_COLLECTION_RESPONSE_MESSAGE }),
                    )
                validateCollections(body).map { it.toCollectionPageSummary() }
            } else {
                Result.failure(
                    Exception(response.message.ifBlank { LOAD_COLLECTIONS_FAILED_MESSAGE }),
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setProfile(collectionId: Long): Result<ProfileCollectionUpdate> {
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

    override suspend fun setBookmark(
        collectionId: Long,
        isBookmarked: Boolean,
    ): Result<BookmarkCollectionUpdate> {
        return try {
            val response = collectionApi.setBookmark(
                customCollectionId = collectionId,
                request = SetBookmarkRequest(isBookmarked = isBookmarked),
            )
            when (response.code) {
                "CUSTOM_COLLECTION_BOOKMARK_UPDATE_SUCCESS" -> {
                    val body = response.result
                        ?: return Result.failure(
                            Exception(
                                response.message.ifBlank { "북마크 변경 응답이 올바르지 않습니다." },
                            ),
                        )
                    Result.success(body.toBookmarkCollectionUpdate())
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
                                    response.message.ifBlank { "북마크 변경 응답이 올바르지 않습니다." },
                                ),
                            )
                        Result.success(body.toBookmarkCollectionUpdate())
                    } else {
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "북마크 변경에 실패했습니다." },
                            ),
                        )
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validateCollections(body: GetCollectionResponse): Result<GetCollectionResponse> {
        return if (body.collections.isEmpty()) {
            Result.failure(Exception(INVALID_COLLECTION_RESPONSE_MESSAGE))
        } else {
            Result.success(body)
        }
    }

    companion object {
        private const val INVALID_COLLECTION_RESPONSE_MESSAGE = "컬렉션 응답이 올바르지 않습니다."
        private const val LOAD_COLLECTIONS_FAILED_MESSAGE = "컬렉션 조회에 실패했습니다."
    }
}
