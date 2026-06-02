package com.issueissyu.fe.data.repository

import com.issueissyu.fe.domain.model.collection.CollectionPageSummary
import com.issueissyu.fe.domain.model.mypage.BookmarkCollectionUpdate
import com.issueissyu.fe.domain.model.mypage.ProfileCollectionUpdate
import com.issueissyu.fe.domain.repository.CollectionRepository
import com.issueissyu.fe.domain.repository.UserCollectionsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserCollectionsStoreImpl @Inject constructor(
    private val collectionRepository: CollectionRepository,
) : UserCollectionsStore {

    private val mutex = Mutex()
    private var myPageCacheValid = false

    private val _snapshot = MutableStateFlow<CollectionPageSummary?>(null)
    override val snapshot: StateFlow<CollectionPageSummary?> = _snapshot.asStateFlow()

    override suspend fun refreshForMyPage(force: Boolean): Result<CollectionPageSummary> {
        if (!force && myPageCacheValid) {
            _snapshot.value?.let { return Result.success(it) }
        }
        return fetchAndCache(checkUnlock = false, markMyPageCacheValid = true, force = force)
    }

    override suspend fun refreshForCollectionTab(): Result<CollectionPageSummary> {
        // 컬렉션 탭 조회로 스냅샷이 갱신되면 마이페이지도 force=false 시 재사용 가능.
        return fetchAndCache(checkUnlock = true, markMyPageCacheValid = true)
    }

    override fun clear() {
        _snapshot.value = null
        myPageCacheValid = false
    }

    override suspend fun setProfile(collectionId: Long): Result<ProfileCollectionUpdate> {
        return collectionRepository.setProfile(collectionId)
            .onSuccess { update -> applyProfileUpdate(update) }
    }

    override suspend fun setBookmark(
        collectionId: Long,
        isBookmarked: Boolean,
    ): Result<BookmarkCollectionUpdate> {
        return collectionRepository.setBookmark(collectionId, isBookmarked)
            .onSuccess { update -> applyBookmarkUpdate(update) }
    }

    override fun patchNickname(nickname: String) {
        _snapshot.update { current ->
            current?.copy(nickname = nickname)
        }
        myPageCacheValid = true
    }

    private suspend fun fetchAndCache(
        checkUnlock: Boolean,
        markMyPageCacheValid: Boolean,
        force: Boolean = false,
    ): Result<CollectionPageSummary> = mutex.withLock {
        if (!force && !checkUnlock && myPageCacheValid) {
            _snapshot.value?.let { return Result.success(it) }
        }
        collectionRepository.getCollections(checkUnlock = checkUnlock)
            .onSuccess { summary ->
                _snapshot.value = summary
                if (markMyPageCacheValid) {
                    myPageCacheValid = true
                }
            }
    }

    private fun applyProfileUpdate(update: ProfileCollectionUpdate) {
        _snapshot.update { current ->
            val summary = current ?: return@update null
            val updatedProfile = summary.profileCollection.copy(
                collectionId = update.profileCollectionId,
                imageUrl = update.profileImageUrl.ifBlank { summary.profileCollection.imageUrl },
            )
            summary.copy(profileCollection = updatedProfile)
        }
        myPageCacheValid = true
    }

    private fun applyBookmarkUpdate(update: BookmarkCollectionUpdate) {
        _snapshot.update { current ->
            current?.copy(
                collections = current.collections.map { character ->
                    if (character.collectionId == update.collectionId) {
                        character.copy(isBookmarked = update.isBookmarked)
                    } else {
                        character
                    }
                },
            )
        }
        myPageCacheValid = true
    }
}
