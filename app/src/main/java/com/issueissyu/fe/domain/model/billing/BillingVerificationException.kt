package com.issueissyu.fe.domain.model.billing

class BillingVerificationException(
    message: String,
    val serverCode: String?,
) : Exception(message) {
    val isAlreadyProcessed: Boolean
        get() = serverCode == "BILLING_409_1"
}
