package com.issueissyu.fe.domain.model.billing

sealed interface BillingPurchaseEvent {
    val productId: String

    data class Verified(
        override val productId: String,
        val emojiId: Long?,
    ) : BillingPurchaseEvent

    data class Pending(
        override val productId: String,
    ) : BillingPurchaseEvent

    data class Canceled(
        override val productId: String,
    ) : BillingPurchaseEvent

    data class Failed(
        override val productId: String,
        val message: String,
    ) : BillingPurchaseEvent
}
