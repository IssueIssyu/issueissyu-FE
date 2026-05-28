package com.issueissyu.fe.domain.usecase.community

import com.issueissyu.fe.domain.model.pin.PinLike
import com.issueissyu.fe.domain.repository.CommunityRepository
import javax.inject.Inject

class LikeCommunityUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    suspend operator fun invoke(communityId: Long): Result<PinLike> {
        return repository.likeCommunity(communityId)
    }
}
