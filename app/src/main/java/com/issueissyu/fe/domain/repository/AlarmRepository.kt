package com.issueissyu.fe.domain.repository

interface AlarmRepository {
    //FCM Push Token 저장
    suspend fun storePushToken(fcmPushToken: String): Result<Unit>
}