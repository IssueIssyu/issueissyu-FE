package com.issueissyu.fe.data.repository

import android.content.Context
import com.android.billingclient.api.*
import com.issueissyu.fe.domain.repository.BillingRepository

class BillingRepositoryImpl(private val context: Context) : BillingRepository {
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

    override fun acknowledgePurchase(purchase: Purchase) {
        //TODO: 추후 구현
    }
}
