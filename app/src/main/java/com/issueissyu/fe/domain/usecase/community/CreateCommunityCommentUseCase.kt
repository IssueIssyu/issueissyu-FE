package com.issueissyu.fe.domain.usecase.community

import com.issueissyu.fe.domain.model.community.CommunityComment
import com.issueissyu.fe.domain.repository.CommunityRepository
import javax.inject.Inject

class CreateCommunityCommentUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    suspend operator fun invoke(communityId: Long, content: String): Result<CommunityComment> {
        return repository.createCommunityComment(communityId = communityId, content = content)
    }
}
