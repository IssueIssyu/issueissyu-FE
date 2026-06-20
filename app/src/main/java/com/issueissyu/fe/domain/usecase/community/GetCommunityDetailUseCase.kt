package com.issueissyu.fe.domain.usecase.community

import com.issueissyu.fe.domain.model.community.CommunityDetail
import com.issueissyu.fe.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommunityDetailUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    operator fun invoke(
        communityId: Long,
        kind: String? = null,
    ): Flow<CommunityDetail> {
        return repository.getCommunityDetail(communityId, kind)
    }
}
