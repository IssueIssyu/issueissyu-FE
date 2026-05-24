package com.issueissyu.fe.domain.usecase.community

import com.issueissyu.fe.domain.model.community.CommunityComment
import com.issueissyu.fe.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommunityCommentsUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    operator fun invoke(communityId: Long): Flow<List<CommunityComment>> {
        return repository.getCommunityComments(communityId)
    }
}
