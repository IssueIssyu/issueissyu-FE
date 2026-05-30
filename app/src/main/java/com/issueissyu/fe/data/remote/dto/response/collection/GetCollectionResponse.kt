package com.issueissyu.fe.data.remote.dto.response.collection

import com.google.gson.annotations.SerializedName

data class GetCollectionResponse (
    val nickname: String,
    val myCollection: MyCollection,
    val collections: List<Collections>,
    val newlyUnlocked: List<NewlyUnlocked>
)


data class MyCollection(
    val collectionId: Int,
    val name: String,
    val imageUrl: String
)

data class Collections(
    val collectionId: Int,
    val name: String,
    val imageUrl: String,
    @SerializedName("isLocked")
    val isLocked: Boolean,
    @SerializedName("isBookmarked")
    val isBookmarked: Boolean,
    val unlockCondition: String
)

data class NewlyUnlocked(
    val collectionId: Int,
    val name: String,
    val imageUrl: String,
    val unlockCondition: String
)