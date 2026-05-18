package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.community.CommunityDetail
import com.issueissyu.fe.domain.model.community.CommunityFeed
import com.issueissyu.fe.domain.model.community.CommunityTab
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getCommunityFeed(
        tab: CommunityTab,
        region: String,
        cursor: String?,
        size: Int
    ): Flow<CommunityFeed>

    fun getCommunityDetail(communityId: Long): Flow<CommunityDetail>
}
