package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.mypage.MyPageCollectionSummary

interface CollectionRepository {
    suspend fun getCollections(checkUnlock: Boolean = false): Result<MyPageCollectionSummary>
}
