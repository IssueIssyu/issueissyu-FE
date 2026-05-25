package com.issueissyu.fe.domain.usecase.community

import com.issueissyu.fe.domain.repository.CommunityRepository
import javax.inject.Inject

class DeclareCommunityUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    suspend operator fun invoke(communityId: Long, reasonIndex: Int): Result<Unit> {
        return repository.declareCommunity(
            communityId = communityId,
            reasonIndex = reasonIndex,
        )
    }
}
