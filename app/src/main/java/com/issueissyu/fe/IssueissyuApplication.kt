package com.issueissyu.fe

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import com.issueissyu.fe.core.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class IssueissyuApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //알림 채널 생성
        NotificationHelper.createChannel(this)

        //FCM 토큰 발급 요청
        //비동기로 처리
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            //토큰 발급 완료되면 실행되는 블록
        }
    }
}
