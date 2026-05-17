package com.issueissyu.fe

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import com.issueissyu.fe.core.notification.NotificationHelper
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class IssueissyuApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.NAVER_CLIENT_ID.isNotBlank()) {
            NaverIdLoginSDK.initialize(
                this,
                BuildConfig.NAVER_CLIENT_ID,
                BuildConfig.NAVER_CLIENT_SECRET,
                BuildConfig.NAVER_CLIENT_NAME,
            )
        }

        NotificationHelper.createChannel(this)

        // FCM 기기 등록 토큰(푸시 알림용). 네이버 로그인 SDK와는 무관합니다.
        FirebaseMessaging.getInstance().token.addOnSuccessListener { _ ->
            // 푸시 연동 시 서버로 token 전달
        }
    }
}
