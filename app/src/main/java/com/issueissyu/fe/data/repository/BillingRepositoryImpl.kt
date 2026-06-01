package com.issueissyu.fe.data.repository

import android.content.Context
import com.android.billingclient.api.*
import com.google.gson.Gson
import com.issueissyu.fe.data.remote.api.BillingApi
import com.issueissyu.fe.data.remote.dto.request.billing.VerifyPurchaseRequest
import com.issueissyu.fe.domain.model.billing.BillingVerificationException
import com.issueissyu.fe.domain.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val billingApi: BillingApi,
    private val gson: Gson,
) : BillingRepository {
    private val billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchase ->
            //구매 업데이트 처리
        }
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    override fun connect() {
        billingClient.startConnection(object: BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                //연결 완료
                //if (billingResult.responseCode ==  BillingResponseCode.OK) {}
            }

            override fun onBillingServiceDisconnected() {
                // API 호출 시 BillingClient가 자동 재연결을 시도합니다.
            }
        })
    }

    //기타 등등
    override fun queryProducts() {
        //TODO: 추후 구현
    }

    override fun purchaseProduct(productId: String) {
        //TODO: 추후 구현
    }

    override suspend fun verifyPurchase(productId: String, purchaseToken: String): Result<Long> {
        return try {
            val response = billingApi.verifyPurchase(
                VerifyPurchaseRequest(
                    productId = productId,
                    purchaseToken = purchaseToken,
                )
            )
            val purchasedEmojiId = response.result

            if (response.isSuccess && purchasedEmojiId != null) {
                Result.success(purchasedEmojiId)
            } else {
                Result.failure(
                    BillingVerificationException(
                        message = response.message.ifBlank { "결제 검증에 실패했습니다." },
                        serverCode = response.code,
                    )
                )
            }
        } catch (e: HttpException) {
            val errorEnvelope = e.response()?.errorBody()?.string()
                ?.takeIf { it.isNotBlank() }
                ?.let { body ->
                    runCatching {
                        gson.fromJson(body, BillingVerificationErrorEnvelope::class.java)
                    }.getOrNull()
                }

            Result.failure(
                BillingVerificationException(
                    message = errorEnvelope?.message?.ifBlank { null } ?: "결제 검증에 실패했습니다.",
                    serverCode = errorEnvelope?.code,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class BillingVerificationErrorEnvelope(
        val code: String?,
        val message: String?,
    )
}
