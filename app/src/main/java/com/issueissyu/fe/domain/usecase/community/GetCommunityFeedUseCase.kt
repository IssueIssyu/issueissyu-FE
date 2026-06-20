package com.issueissyu.fe.domain.usecase.community

import com.issueissyu.fe.domain.model.community.CommunityFeed
import com.issueissyu.fe.domain.model.community.CommunityTab
import com.issueissyu.fe.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommunityFeedUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    operator fun invoke(
        tab: CommunityTab,
        locationId: Long?,
        cursor: String? = null,
        size: Int = 20
    ): Flow<CommunityFeed> {
        return repository.getCommunityFeed(tab, locationId, cursor, size)
    }
}
