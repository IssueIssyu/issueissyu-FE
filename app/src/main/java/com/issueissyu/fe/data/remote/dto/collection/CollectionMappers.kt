package com.issueissyu.fe.data.remote.dto.collection

import com.issueissyu.fe.data.remote.dto.response.collection.Collections
import com.issueissyu.fe.data.remote.dto.response.collection.GetCollectionResponse
import com.issueissyu.fe.data.remote.dto.response.collection.SetProfileResponse
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

fun SetProfileResponse.toProfileCollectionUpdate(): ProfileCollectionUpdate {
    return ProfileCollectionUpdate(
        profileCollectionId = profileCollectionId,
        profileImageUrl = profileImageUrl,
    )
}
