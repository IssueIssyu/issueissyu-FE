package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.collection.CollectionPageSummary
import com.issueissyu.fe.domain.model.mypage.BookmarkCollectionUpdate
import com.issueissyu.fe.domain.model.mypage.ProfileCollectionUpdate
import kotlinx.coroutines.flow.StateFlow

//컬렉션 조회
//마이페이지/프로필 변경: [refreshForMyPage] — 캐시가 유효하면 api 호출 x
//컬렉션 탭: [refreshForCollectionTab] — 진입 시마다 checkUnlock=true 조회 / newly 때문에 조회 필요
//성공 시 스냅샷·마이페이지 캐시를 함께 갱신
interface UserCollectionsStore {

    val snapshot: StateFlow<CollectionPageSummary?>

    //checkUnlock=false. force가 false이고 마이페이지 캐시가 유효하면 호출 x
    suspend fun refreshForMyPage(force: Boolean = false): Result<CollectionPageSummary>

    //checkUnlock=true. 컬렉션 탭 진입 시마다 호출
    suspend fun refreshForCollectionTab(): Result<CollectionPageSummary>

    //로그아웃 or 회원탈퇴 시 스냅샷 제거
    fun clear()

    suspend fun setProfile(collectionId: Long): Result<ProfileCollectionUpdate>

    suspend fun setBookmark(
        collectionId: Long,
        isBookmarked: Boolean,
    ): Result<BookmarkCollectionUpdate>

    fun patchNickname(nickname: String)
}
