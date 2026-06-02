package com.issueissyu.fe.data.remote.dto.collection

import com.issueissyu.fe.data.remote.dto.response.collection.Collections
import com.issueissyu.fe.data.remote.dto.response.collection.GetCollectionResponse
import com.issueissyu.fe.data.remote.dto.response.collection.MyCollection
import com.issueissyu.fe.data.remote.dto.response.collection.NewlyUnlocked
import com.issueissyu.fe.data.remote.dto.response.collection.SetBookmarkResponse
import com.issueissyu.fe.data.remote.dto.response.collection.SetProfileResponse
import com.issueissyu.fe.domain.model.collection.CollectionCharacter
import com.issueissyu.fe.domain.model.collection.CollectionPageSummary
import com.issueissyu.fe.domain.model.mypage.BookmarkCollectionUpdate
import com.issueissyu.fe.domain.model.mypage.ProfileCollectionUpdate

private const val DEFAULT_COLLECTION_CHARACTER_NAME = "기본"
private const val DEFAULT_COLLECTION_CHARACTER_API_NAME = "default"

private fun mapCollectionCharacterName(name: String): String {
    return if (name.equals(DEFAULT_COLLECTION_CHARACTER_API_NAME, ignoreCase = true)) {
        DEFAULT_COLLECTION_CHARACTER_NAME
    } else name
}

fun GetCollectionResponse.toCollectionPageSummary(): CollectionPageSummary {
    return CollectionPageSummary(
        nickname = nickname,
        profileCollection = myCollection.toCollectionCharacter(),
        collections = collections.map { it.toCollectionCharacter() },
        newlyUnlocked = newlyUnlocked.map { it.toCollectionCharacter() },
    )
}

private fun MyCollection.toCollectionCharacter(): CollectionCharacter {
    return CollectionCharacter(
        collectionId = collectionId,
        name = mapCollectionCharacterName(name),
        imageUrl = imageUrl,
    )
}

private fun Collections.toCollectionCharacter(): CollectionCharacter {
    return CollectionCharacter(
        collectionId = collectionId,
        name = mapCollectionCharacterName(name),
        imageUrl = imageUrl,
        isLocked = isLocked,
        isBookmarked = isBookmarked,
        unlockCondition = unlockCondition,
    )
}

private fun NewlyUnlocked.toCollectionCharacter(): CollectionCharacter {
    return CollectionCharacter(
        collectionId = collectionId,
        name = mapCollectionCharacterName(name),
        imageUrl = imageUrl,
        isLocked = false,
        unlockCondition = unlockCondition,
    )
}

fun SetProfileResponse.toProfileCollectionUpdate(): ProfileCollectionUpdate {
    return ProfileCollectionUpdate(
        profileCollectionId = profileCollectionId,
        profileImageUrl = profileImageUrl,
    )
}

fun SetBookmarkResponse.toBookmarkCollectionUpdate(): BookmarkCollectionUpdate {
    return BookmarkCollectionUpdate(
        collectionId = customCollectionId,
        isBookmarked = isBookmarked,
    )
}