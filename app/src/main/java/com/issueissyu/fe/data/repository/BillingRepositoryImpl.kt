package com.issueissyu.fe.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.google.gson.Gson
import com.issueissyu.fe.data.remote.api.BillingApi
import com.issueissyu.fe.data.remote.dto.request.billing.VerifyPurchaseRequest
import com.issueissyu.fe.domain.model.billing.BillingPurchaseEvent
import com.issueissyu.fe.domain.model.billing.BillingVerificationException
import com.issueissyu.fe.domain.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import kotlin.coroutines.resume
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val billingApi: BillingApi,
    private val gson: Gson,
) : BillingRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _purchaseEvents = MutableSharedFlow<BillingPurchaseEvent>(extraBufferCapacity = 8)
    override val purchaseEvents: SharedFlow<BillingPurchaseEvent> = _purchaseEvents.asSharedFlow()
    @Volatile
    private var activeProductId: String? = null
    private val processingPurchaseTokens = mutableSetOf<String>()
    private val connectionMutex = Mutex()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            handlePurchaseUpdate(billingResult, purchases.orEmpty())
        }
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    override suspend fun restorePurchases(): Result<Unit> {
        return runCatching {
            awaitConnection().getOrThrow()
            val purchases = queryPurchases().getOrThrow()
            purchases.forEach(::handlePurchase)
        }
    }

    override suspend fun purchaseProduct(activity: Activity, productId: String): Result<Unit> {
        return runCatching {
            check(activeProductId == null) { "진행 중인 결제가 있습니다." }
            activeProductId = productId

            awaitConnection().getOrThrow()
            val productDetails = queryProductDetails(productId).getOrThrow()
            val offerToken = productDetails.oneTimePurchaseOfferDetailsList
                ?.firstOrNull()
                ?.offerToken
                ?: throw IllegalStateException("구매 가능한 상품 옵션을 찾지 못했습니다.")
            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()
            val launchResult = withContext(Dispatchers.Main) {
                billingClient.launchBillingFlow(activity, billingFlowParams)
            }

            check(launchResult.responseCode == BillingClient.BillingResponseCode.OK) {
                launchResult.safeErrorMessage("결제창을 열지 못했습니다.")
            }
        }.onFailure {
            activeProductId = null
        }
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

    private suspend fun awaitConnection(): Result<Unit> {
        return connectionMutex.withLock {
            if (billingClient.isReady) return@withLock Result.success(Unit)

            suspendCancellableCoroutine { continuation ->
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (!continuation.isActive) return
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            continuation.resume(Result.success(Unit))
                        } else {
                            continuation.resume(
                                Result.failure(
                                    IllegalStateException(
                                        billingResult.safeErrorMessage("결제 서비스 연결에 실패했습니다.")
                                    )
                                )
                            )
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(IllegalStateException("결제 서비스 연결이 끊겼습니다.")))
                        }
                    }
                })
            }
        }
    }

    private suspend fun queryProductDetails(productId: String): Result<ProductDetails> {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productId)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        return suspendCancellableCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(params) { billingResult, queryResult ->
                if (!continuation.isActive) return@queryProductDetailsAsync
                val productDetails = queryResult.productDetailsList.firstOrNull()
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetails != null) {
                    continuation.resume(Result.success(productDetails))
                } else {
                    continuation.resume(
                        Result.failure(
                            IllegalStateException(
                                billingResult.safeErrorMessage("상품 정보를 불러오지 못했습니다.")
                            )
                        )
                    )
                }
            }
        }
    }

    private suspend fun queryPurchases(): Result<List<Purchase>> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        return suspendCancellableCoroutine { continuation ->
            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (!continuation.isActive) return@queryPurchasesAsync
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(Result.success(purchases))
                } else {
                    continuation.resume(
                        Result.failure(
                            IllegalStateException(
                                billingResult.safeErrorMessage("구매 내역을 불러오지 못했습니다.")
                            )
                        )
                    )
                }
            }
        }
    }

    private fun handlePurchaseUpdate(billingResult: BillingResult, purchases: List<Purchase>) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isEmpty()) {
                    activeProductId?.let { productId ->
                        emitFailed(productId, "결제 결과를 확인하지 못했습니다.")
                    }
                    activeProductId = null
                } else {
                    purchases.forEach(::handlePurchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                activeProductId?.let { productId ->
                    _purchaseEvents.tryEmit(BillingPurchaseEvent.Canceled(productId))
                }
                activeProductId = null
            }
            else -> {
                activeProductId?.let { productId ->
                    emitFailed(productId, billingResult.safeErrorMessage("결제에 실패했습니다."))
                }
                activeProductId = null
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: activeProductId ?: return
        if (activeProductId == productId) {
            activeProductId = null
        }
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                val shouldProcess = synchronized(processingPurchaseTokens) {
                    processingPurchaseTokens.add(purchase.purchaseToken)
                }
                if (!shouldProcess) return
                scope.launch {
                    try {
                        verifyPurchase(productId, purchase.purchaseToken)
                            .onSuccess { emojiId ->
                                _purchaseEvents.emit(BillingPurchaseEvent.Verified(productId, emojiId))
                            }
                            .onFailure { error ->
                                if (error is BillingVerificationException && error.isAlreadyProcessed) {
                                    _purchaseEvents.emit(BillingPurchaseEvent.Verified(productId, emojiId = null))
                                } else {
                                    _purchaseEvents.emit(
                                        BillingPurchaseEvent.Failed(
                                            productId = productId,
                                            message = error.message?.takeIf { it.isNotBlank() }
                                                ?: "결제 검증에 실패했습니다.",
                                        )
                                    )
                                }
                            }
                    } finally {
                        synchronized(processingPurchaseTokens) {
                            processingPurchaseTokens.remove(purchase.purchaseToken)
                        }
                    }
                }
            }
            Purchase.PurchaseState.PENDING -> {
                _purchaseEvents.tryEmit(BillingPurchaseEvent.Pending(productId))
            }
            else -> {
                emitFailed(productId, "결제 상태를 확인하지 못했습니다.")
            }
        }
    }

    private fun emitFailed(productId: String, message: String) {
        _purchaseEvents.tryEmit(BillingPurchaseEvent.Failed(productId, message))
    }

    private fun BillingResult.safeErrorMessage(fallback: String): String {
        return debugMessage.takeIf { it.isNotBlank() } ?: fallback
    }
}
