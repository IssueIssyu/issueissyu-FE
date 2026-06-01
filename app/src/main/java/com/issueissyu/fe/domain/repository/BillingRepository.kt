package com.issueissyu.fe.domain.repository

import android.app.Activity
import com.issueissyu.fe.domain.model.billing.BillingPurchaseEvent
import kotlinx.coroutines.flow.SharedFlow

interface BillingRepository {
    val purchaseEvents: SharedFlow<BillingPurchaseEvent>

    fun connect()
    suspend fun purchaseProduct(activity: Activity, productId: String): Result<Unit>
    suspend fun verifyPurchase(productId: String, purchaseToken: String): Result<Long>
}
