package com.issueissyu.fe.data.remote.dto.collection

import com.issueissyu.fe.data.remote.dto.response.collection.Collections
import com.issueissyu.fe.data.remote.dto.response.collection.GetCollectionResponse
import com.issueissyu.fe.data.remote.dto.response.collection.MyCollection
import com.issueissyu.fe.data.remote.dto.response.collection.NewlyUnlocked
import com.issueissyu.fe.data.remote.dto.response.collection.SetProfileResponse
import com.issueissyu.fe.domain.model.collection.CollectionCharacter
import com.issueissyu.fe.domain.model.collection.CollectionPageSummary
import com.issueissyu.fe.domain.model.mypage.MyPageCollectionItem
import com.issueissyu.fe.domain.model.mypage.MyPageCollectionSummary
import com.issueissyu.fe.domain.model.mypage.ProfileCollectionUpdate

fun GetCollectionResponse.toMyPageCollectionSummary(): MyPageCollectionSummary {
    return MyPageCollectionSummary(
        nickname = nickname,
        profileImageUrl = myCollection.imageUrl.takeIf { it.isNotBlank() },
        bookmarkedCollections = collections
            .filter { it.isBookmarked }
            .map { it.toMyPageCollectionItem() },
    )
}

private fun Collections.toMyPageCollectionItem(): MyPageCollectionItem {
    return MyPageCollectionItem(
        collectionId = collectionId,
        name = name,
        imageUrl = imageUrl,
    )
}

fun GetCollectionResponse.toCollectionPageSummary(): CollectionPageSummary {
    return CollectionPageSummary(
        profileCollection = myCollection.toCollectionCharacter(),
        collections = collections.map { it.toCollectionCharacter() },
        newlyUnlocked = newlyUnlocked.map { it.toCollectionCharacter() },
    )
}

private fun MyCollection.toCollectionCharacter(): CollectionCharacter {
    return CollectionCharacter(
        collectionId = collectionId,
        name = name,
        imageUrl = imageUrl,
    )
}

private fun Collections.toCollectionCharacter(): CollectionCharacter {
    return CollectionCharacter(
        collectionId = collectionId,
        name = name,
        imageUrl = imageUrl,
        isLocked = isLocked,
        isBookmarked = isBookmarked,
        unlockCondition = unlockCondition,
    )
}

private fun NewlyUnlocked.toCollectionCharacter(): CollectionCharacter {
    return CollectionCharacter(
        collectionId = collectionId,
        name = name,
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
