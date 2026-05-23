package com.issueissyu.fe.domain.repository

import com.android.billingclient.api.Purchase

interface BillingRepository {
    fun connect()
    fun queryProducts()
    fun purchaseProduct(productId: String)
    fun acknowledgePurchase(purchase: Purchase)
}