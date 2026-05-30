package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.collection.CollectionPageSummary
import com.issueissyu.fe.domain.model.mypage.MyPageCollectionSummary
import com.issueissyu.fe.domain.model.mypage.ProfileCollectionUpdate

interface CollectionRepository {
    suspend fun getCollections(checkUnlock: Boolean = false): Result<MyPageCollectionSummary>

    suspend fun getCollectionPage(): Result<CollectionPageSummary>

    suspend fun setProfile(collectionId: Long): Result<ProfileCollectionUpdate>
}
