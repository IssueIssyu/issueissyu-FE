package com.issueissyu.fe.data.repository

import com.issueissyu.fe.core.network.ApiErrorMapper
import com.issueissyu.fe.data.remote.api.CollectionApi
import com.issueissyu.fe.data.remote.dto.collection.toBookmarkCollectionUpdate
import com.issueissyu.fe.data.remote.dto.collection.toCollectionPageSummary
import com.issueissyu.fe.data.remote.dto.collection.toProfileCollectionUpdate
import com.issueissyu.fe.data.remote.dto.request.collection.SetBookmarkRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.collection.SetBookmarkResponse
import com.issueissyu.fe.data.remote.dto.response.collection.SetProfileResponse
import com.issueissyu.fe.domain.model.collection.CollectionPageSummary
import com.issueissyu.fe.domain.model.mypage.BookmarkCollectionUpdate
import com.issueissyu.fe.domain.model.mypage.ProfileCollectionUpdate
import com.issueissyu.fe.domain.repository.CollectionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val collectionApi: CollectionApi,
    private val apiErrorMapper: ApiErrorMapper,
) : CollectionRepository {

    private fun <T> failureFrom(e: Exception, fallback: String): Result<T> =
        Result.failure(apiErrorMapper.toException(e, fallback))

    override suspend fun getCollections(checkUnlock: Boolean): Result<CollectionPageSummary> {
        return try {
            val response = collectionApi.getCollections(checkUnlock = checkUnlock)
            if (response.isSuccess) {
                val body = response.result
                    ?: return Result.failure(
                        Exception(response.message.ifBlank { INVALID_COLLECTION_RESPONSE_MESSAGE }),
                    )
                Result.success(body.toCollectionPageSummary())
            } else {
                Result.failure(
                    Exception(response.message.ifBlank { LOAD_COLLECTIONS_FAILED_MESSAGE }),
                )
            }
        } catch (e: Exception) {
            failureFrom(e, LOAD_COLLECTIONS_FAILED_MESSAGE)
        }
    }

    override suspend fun setProfile(collectionId: Long): Result<ProfileCollectionUpdate> {
        return handleCollectionMutation(
            apiCall = { collectionApi.setProfile(collectionId = collectionId) },
            successCode = "PROFILE_COLLECTION_UPDATE_SUCCESS",
            invalidBodyMessage = "프로필 컬렉션 변경 응답이 올바르지 않습니다.",
            genericFailureMessage = "프로필 컬렉션 변경에 실패했습니다.",
            mapResult = { body: SetProfileResponse -> body.toProfileCollectionUpdate() },
        )
    }

    override suspend fun setBookmark(
        collectionId: Long,
        isBookmarked: Boolean,
    ): Result<BookmarkCollectionUpdate> {
        return handleCollectionMutation(
            apiCall = {
                collectionApi.setBookmark(
                    customCollectionId = collectionId,
                    request = SetBookmarkRequest(isBookmarked = isBookmarked),
                )
            },
            successCode = "CUSTOM_COLLECTION_BOOKMARK_UPDATE_SUCCESS",
            invalidBodyMessage = "북마크 변경 응답이 올바르지 않습니다.",
            genericFailureMessage = "북마크 변경에 실패했습니다.",
            mapResult = { body: SetBookmarkResponse -> body.toBookmarkCollectionUpdate() },
        )
    }

    private suspend fun <T, R> handleCollectionMutation(
        apiCall: suspend () -> BaseResponse<T?>,
        successCode: String,
        invalidBodyMessage: String,
        genericFailureMessage: String,
        mapResult: (T) -> R,
    ): Result<R> {
        return try {
            val response = apiCall()
            when (response.code) {
                successCode -> {
                    val body = response.result
                        ?: return Result.failure(
                            Exception(response.message.ifBlank { invalidBodyMessage }),
                        )
                    Result.success(mapResult(body))
                }

                COLLECTION_NOT_FOUND_CODE ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { COLLECTION_NOT_FOUND_MESSAGE },
                        ),
                    )

                COLLECTION_LOCKED_CODE ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { COLLECTION_LOCKED_MESSAGE },
                        ),
                    )

                else ->
                    if (response.isSuccess) {
                        val body = response.result
                            ?: return Result.failure(
                                Exception(response.message.ifBlank { invalidBodyMessage }),
                            )
                        Result.success(mapResult(body))
                    } else {
                        Result.failure(
                            Exception(response.message.ifBlank { genericFailureMessage }),
                        )
                    }
            }
        } catch (e: Exception) {
            failureFrom(e, genericFailureMessage)
        }
    }

    companion object {
        private const val COLLECTION_NOT_FOUND_CODE = "CUSTOM_COLLECTION_NOT_FOUND"
        private const val COLLECTION_LOCKED_CODE = "CUSTOM_COLLECTION_LOCKED"
        private const val COLLECTION_NOT_FOUND_MESSAGE = "컬렉션을 찾을 수 없습니다."
        private const val COLLECTION_LOCKED_MESSAGE = "잠금 상태인 컬렉션입니다."
        private const val INVALID_COLLECTION_RESPONSE_MESSAGE = "컬렉션 응답이 올바르지 않습니다."
        private const val LOAD_COLLECTIONS_FAILED_MESSAGE = "컬렉션 조회에 실패했습니다."
    }
}
