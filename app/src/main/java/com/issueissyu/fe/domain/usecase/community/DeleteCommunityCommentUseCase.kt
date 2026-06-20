package com.issueissyu.fe.domain.usecase.community

import com.issueissyu.fe.domain.repository.CommunityRepository
import javax.inject.Inject

class DeleteCommunityCommentUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    suspend operator fun invoke(commentId: Long): Result<Unit> {
        return repository.deleteCommunityComment(commentId)
    }
}
