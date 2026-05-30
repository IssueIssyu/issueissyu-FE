package com.issueissyu.fe.domain.model.mypage

data class MyPageCollectionSummary(
    val nickname: String,
    val profileImageUrl: String?,
    val bookmarkedCollections: List<MyPageCollectionItem>,
)

data class MyPageCollectionItem(
    val collectionId: Long,
    val name: String,
    val imageUrl: String,
)

data class ProfileCollectionUpdate(
    val profileCollectionId: Long,
    val profileImageUrl: String,
)

data class BookmarkCollectionUpdate(
    val collectionId: Long,
    val isBookmarked: Boolean,
)
