package com.issueissyu.fe.domain.model.collection

data class CollectionPageSummary(
    val nickname: String,
    val profileCollection: CollectionCharacter,
    val collections: List<CollectionCharacter>,
    val newlyUnlocked: List<CollectionCharacter> = emptyList(),
) {
    val profileImageUrl: String?
        get() = profileCollection.imageUrl.takeIf { it.isNotBlank() }

    val bookmarkedCollections: List<CollectionCharacter>
        get() = collections.filter { it.isBookmarked }
}

data class CollectionCharacter(
    val collectionId: Long,
    val name: String,
    val imageUrl: String,
    val isLocked: Boolean = false,
    val isBookmarked: Boolean = false,
    val unlockCondition: String = "",
)