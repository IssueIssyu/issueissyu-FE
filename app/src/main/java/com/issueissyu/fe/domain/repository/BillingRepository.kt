package com.issueissyu.fe.domain.repository

import android.app.Activity
import com.issueissyu.fe.domain.model.billing.BillingPurchaseEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val purchaseEvents: SharedFlow<BillingPurchaseEvent>
    val pendingBillingProductId: StateFlow<String?>

    suspend fun restorePurchases(): Result<Unit>
    suspend fun purchaseProduct(activity: Activity, productId: String): Result<Unit>
    suspend fun verifyPurchase(productId: String, purchaseToken: String): Result<Long>
    fun acknowledgePurchaseResult(productId: String)
}
