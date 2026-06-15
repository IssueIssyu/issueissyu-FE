package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.collection.CollectionPageSummary
import com.issueissyu.fe.domain.model.mypage.BookmarkCollectionUpdate
import com.issueissyu.fe.domain.model.mypage.ProfileCollectionUpdate

interface CollectionRepository {
    suspend fun getCollections(checkUnlock: Boolean = false): Result<CollectionPageSummary>

    suspend fun setProfile(collectionId: Long): Result<ProfileCollectionUpdate>

    suspend fun setBookmark(
        collectionId: Long,
        isBookmarked: Boolean,
    ): Result<BookmarkCollectionUpdate>
}
