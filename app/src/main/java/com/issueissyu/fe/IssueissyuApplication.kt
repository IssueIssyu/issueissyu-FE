package com.issueissyu.fe

import android.app.Application
import com.issueissyu.fe.BuildConfig
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.NaverMapSdk.NcpKeyClient
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class IssueissyuApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NaverMapSdk.getInstance(this).setClient(NcpKeyClient(BuildConfig.NAVER_MAP_CLIENT_ID))
    }
}
