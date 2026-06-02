package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.AlarmApi
import com.issueissyu.fe.data.remote.dto.request.alarm.StoreTokenRequest
import com.issueissyu.fe.domain.repository.AlarmRepository
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val alarmApi: AlarmApi
) : AlarmRepository {

    //FCM Push Token 저장
    override suspend fun storePushToken(fcmPushToken: String): Result<Unit> {
        return try {
            val response = alarmApi.storeToken(StoreTokenRequest(fcmPushToken))
            if (response.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}