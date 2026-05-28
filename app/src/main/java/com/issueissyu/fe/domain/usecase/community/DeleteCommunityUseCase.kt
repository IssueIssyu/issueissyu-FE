package com.issueissyu.fe.domain.usecase.community

import com.issueissyu.fe.domain.repository.CommunityRepository
import javax.inject.Inject

class DeleteCommunityUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    suspend operator fun invoke(communityId: Long): Result<Unit> {
        return repository.deleteCommunity(communityId)
    }
}
