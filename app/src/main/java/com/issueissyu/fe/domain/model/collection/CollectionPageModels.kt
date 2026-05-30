package com.issueissyu.fe.domain.model.collection

data class CollectionPageSummary(
    val profileCollection: CollectionCharacter,
    val collections: List<CollectionCharacter>,
    val newlyUnlocked: List<CollectionCharacter> = emptyList(),
)

data class CollectionCharacter(
    val collectionId: Long,
    val name: String,
    val imageUrl: String,
    val isLocked: Boolean = false,
    val isBookmarked: Boolean = false,
    val unlockCondition: String = "",
)