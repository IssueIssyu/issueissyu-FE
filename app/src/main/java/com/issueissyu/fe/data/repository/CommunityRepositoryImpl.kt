package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.CommunityApi
import com.issueissyu.fe.data.remote.dto.request.community.CommunityCommentRequest
import com.issueissyu.fe.data.remote.dto.request.community.CommunityDeclarationRequest
import com.issueissyu.fe.data.remote.dto.community.toCommunityComment
import com.issueissyu.fe.data.remote.dto.community.toCommunityDetail
import com.issueissyu.fe.data.remote.dto.community.toCommunityFeed
import com.issueissyu.fe.domain.model.community.CommunityComment
import com.issueissyu.fe.domain.model.community.CommunityDetail
import com.issueissyu.fe.domain.model.community.CommunityFeed
import com.issueissyu.fe.domain.model.community.CommunityTab
import com.issueissyu.fe.domain.model.pin.PinLike
import com.issueissyu.fe.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CommunityRepositoryImpl @Inject constructor(
    private val communityApi: CommunityApi,
) : CommunityRepository {

    override fun getCommunityFeed(
        tab: CommunityTab,
        locationId: Long?,
        cursor: String?,
        size: Int
    ): Flow<CommunityFeed> = flow {
        val response = communityApi.getCommunityFeed(
            tab = tab.toApiTab(),
            locationId = locationId,
            cursor = cursor,
            size = size,
        )

        if (!response.isSuccess) {
            throw IllegalStateException(response.message.ifBlank { "커뮤니티 소식을 불러오지 못했습니다." })
        }

        val result = response.result
            ?: throw IllegalStateException(response.message.ifBlank { "커뮤니티 피드 응답이 올바르지 않습니다." })

        val feed = result.toCommunityFeed()
        emit(feed.withHotPreview(tab))
    }

    override fun getCommunityDetail(
        communityId: Long
    ): Flow<CommunityDetail> = flow {
        val response = communityApi.getCommunityDetail(communityId)

        if (!response.isSuccess) {
            throw IllegalStateException(response.message.ifBlank { "게시글을 불러오지 못했습니다." })
        }

        val result = response.result
            ?: throw IllegalStateException(response.message.ifBlank { "게시글 응답이 올바르지 않습니다." })

        emit(result.toCommunityDetail())
    }

    override fun getCommunityComments(
        communityId: Long
    ): Flow<List<CommunityComment>> = flow {
        val response = communityApi.getCommunityComments(communityId)

        if (!response.isSuccess) {
            throw IllegalStateException(response.message.ifBlank { "댓글을 불러오지 못했습니다." })
        }

        emit(response.result.orEmpty().mapNotNull { it.toCommunityComment() })
    }

    override suspend fun createCommunityComment(
        communityId: Long,
        content: String
    ): Result<CommunityComment> {
        return try {
            val response = communityApi.createCommunityComment(
                communityId = communityId,
                request = CommunityCommentRequest(commentContent = content),
            )

            if (response.isSuccess) {
                val comment = response.result?.toCommunityComment()
                    ?: return Result.failure(
                        IllegalStateException(response.message.ifBlank { "댓글 작성 응답이 올바르지 않습니다." })
                    )
                Result.success(comment)
            } else {
                Result.failure(IllegalStateException(response.message.ifBlank { "댓글 작성에 실패했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCommunityComment(
        commentId: Long,
        content: String
    ): Result<CommunityComment> {
        return try {
            val response = communityApi.updateCommunityComment(
                commentId = commentId,
                request = CommunityCommentRequest(commentContent = content),
            )

            if (response.isSuccess) {
                val comment = response.result?.toCommunityComment()
                    ?: return Result.failure(
                        IllegalStateException(response.message.ifBlank { "댓글 수정 응답이 올바르지 않습니다." })
                    )
                Result.success(comment)
            } else {
                Result.failure(IllegalStateException(response.message.ifBlank { "댓글 수정에 실패했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCommunityComment(commentId: Long): Result<Unit> {
        return try {
            val response = communityApi.deleteCommunityComment(commentId)

            if (response.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException(response.message.ifBlank { "댓글 삭제에 실패했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun likeCommunity(communityId: Long): Result<PinLike> {
        return try {
            val response = communityApi.likeCommunity(communityId)

            if (response.isSuccess) {
                val like = response.result
                    ?: return Result.failure(
                        IllegalStateException(response.message.ifBlank { "커뮤니티 공감 응답이 올바르지 않습니다." })
                    )

                Result.success(
                    PinLike(
                        pinId = like.pinId,
                        pinLikeCount = like.pinLikeCount,
                        isLike = like.isLike,
                    )
                )
            } else {
                Result.failure(IllegalStateException(response.message.ifBlank { "커뮤니티 공감에 실패했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun declareCommunity(communityId: Long, reasonIndex: Int): Result<Unit> {
        return try {
            val response = communityApi.declareCommunity(
                communityId = communityId,
                request = CommunityDeclarationRequest(reasonIndex = reasonIndex),
            )

            if (response.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException(response.message.ifBlank { "커뮤니티 게시물 신고에 실패했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun CommunityTab.toApiTab(): String? {
        return when (this) {
            CommunityTab.ALL -> "HOME"
            CommunityTab.HOT -> "HOT"
            CommunityTab.ISSUE -> "ISSUE"
            CommunityTab.COMMUNICATION -> "COMMUNICATION"
            CommunityTab.STORE -> "STORE"
            CommunityTab.FESTIVAL -> "FESTIVAL"
            CommunityTab.POLICY -> "POLICY"
            CommunityTab.CONTEST -> "CONTEST"
            CommunityTab.CARDNEWS -> "CARDNEWS"
        }
    }

    private fun CommunityFeed.withHotPreview(tab: CommunityTab): CommunityFeed {
        if (tab != CommunityTab.ALL) return this

        return copy(
            items = items.mapIndexed { index, item ->
                item.copy(isHot = index < HOT_PREVIEW_COUNT)
            }
        )
    }

    private companion object {
        const val HOT_PREVIEW_COUNT = 3
    }
}
