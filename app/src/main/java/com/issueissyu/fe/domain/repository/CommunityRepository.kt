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

    fun getCommunityComments(communityId: Long): Flow<List<CommunityComment>>

    suspend fun createCommunityComment(communityId: Long, content: String): Result<CommunityComment>

    suspend fun likeCommunity(communityId: Long): Result<PinLike>
}
