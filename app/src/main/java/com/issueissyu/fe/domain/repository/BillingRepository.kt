package com.issueissyu.fe.domain.repository

interface BillingRepository {
    fun connect()
    fun queryProducts()
    fun purchaseProduct(productId: String)
    suspend fun verifyPurchase(productId: String, purchaseToken: String): Result<Long>
}
