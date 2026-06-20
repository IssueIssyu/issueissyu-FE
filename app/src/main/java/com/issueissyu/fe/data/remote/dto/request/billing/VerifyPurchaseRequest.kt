package com.issueissyu.fe.data.remote.dto.request.billing

data class VerifyPurchaseRequest(
    val productId: String,
    val purchaseToken: String,
)
