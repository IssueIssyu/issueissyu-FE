package com.issueissyu.fe

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import com.issueissyu.fe.core.notification.NotificationHelper
import com.issueissyu.fe.domain.repository.AlarmRepository
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        // FCM 기기 등록 토큰(푸시 알림용).
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            CoroutineScope(Dispatchers.IO).launch {
                val alarmRepository = EntryPointAccessors.fromApplication(
                    this@IssueissyuApplication,
                    AlarmRepositoryEntryPoint::class.java
                ).alarmRepository()
                alarmRepository.storePushToken(token)
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AlarmRepositoryEntryPoint{
    fun alarmRepository(): AlarmRepository
}