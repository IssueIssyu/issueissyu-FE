package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.community.CommunityComment
import com.issueissyu.fe.domain.model.community.CommunityDetail
import com.issueissyu.fe.domain.model.community.CommunityFeed
import com.issueissyu.fe.domain.model.community.CommunityTab
import com.issueissyu.fe.domain.model.pin.PinLike
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getCommunityFeed(
        tab: CommunityTab,
        locationId: Long?,
        cursor: String?,
        size: Int
    ): Flow<CommunityFeed>

    fun getCommunityDetail(communityId: Long): Flow<CommunityDetail>

    suspend fun deleteCommunity(communityId: Long): Result<Unit>

    suspend fun takedownCommunity(communityId: Long): Result<Unit>

    fun getCommunityComments(communityId: Long): Flow<List<CommunityComment>>

    suspend fun createCommunityComment(communityId: Long, content: String): Result<CommunityComment>

    suspend fun updateCommunityComment(commentId: Long, content: String): Result<CommunityComment>

    suspend fun deleteCommunityComment(commentId: Long): Result<Unit>

    suspend fun likeCommunity(communityId: Long): Result<PinLike>

    suspend fun declareCommunity(communityId: Long, reasonIndex: Int): Result<Unit>
}
