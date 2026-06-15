package com.issueissyu.fe.domain.model.mypage

data class ProfileCollectionUpdate(
    val profileCollectionId: Long,
    val profileImageUrl: String,
)

data class BookmarkCollectionUpdate(
    val collectionId: Long,
    val isBookmarked: Boolean,
)
