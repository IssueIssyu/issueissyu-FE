package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.request.billing.VerifyPurchaseRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface BillingApi {

    @POST("billing/purchases/verify")
    suspend fun verifyPurchase(
        @Body request: VerifyPurchaseRequest,
    ): BaseResponse<Long?>
}
