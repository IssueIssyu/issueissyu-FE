package com.issueissyu.fe.domain.usecase.community

import com.issueissyu.fe.domain.model.community.CommunityComment
import com.issueissyu.fe.domain.repository.CommunityRepository
import javax.inject.Inject

class UpdateCommunityCommentUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    suspend operator fun invoke(commentId: Long, content: String): Result<CommunityComment> {
        return repository.updateCommunityComment(commentId = commentId, content = content)
    }
}
