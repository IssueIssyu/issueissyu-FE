package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.AlarmApi
import com.issueissyu.fe.data.remote.dto.request.alarm.StoreTokenRequest
import com.issueissyu.fe.data.remote.dto.response.map.toNotificationPage
import com.issueissyu.fe.domain.model.notification.NotificationPage
import com.issueissyu.fe.domain.repository.AlarmRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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

    override suspend fun getAlarmList(
        size: Int?,
        cursor: String?,
    ): Result<NotificationPage> = try {
        val response = alarmApi.getAlarmList(size = size, cursor = cursor)
        when (response.code) {
            "ALARM_LIST_200" -> {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "알림 목록 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toNotificationPage())
            }
            "ALARM_LIST_400_1" -> Result.failure(
                alarmListException(
                    message = response.message,
                    fallback = "조회 불가능한 사이즈 입니다.",
                ),
            )
            "ALARM_LIST_400_2" -> Result.failure(
                alarmListException(
                    message = response.message,
                    fallback = "조회 불가능한 cursor 입니다.",
                ),
            )
            else -> Result.failure(
                alarmListException(
                    message = response.message,
                    fallback = "알림 목록 조회에 실패했습니다.",
                ),
            )
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun alarmListException(
        message: String,
        fallback: String,
    ): Exception {
        return Exception(message.takeIf { it.isNotBlank() } ?: fallback)
    }
}
