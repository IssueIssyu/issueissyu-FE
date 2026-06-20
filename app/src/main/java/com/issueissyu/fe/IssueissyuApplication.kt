package com.issueissyu.fe

import android.app.Application
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
    }
}