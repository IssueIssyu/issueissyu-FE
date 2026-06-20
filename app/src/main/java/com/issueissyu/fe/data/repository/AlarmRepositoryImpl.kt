package com.issueissyu.fe.data.repository

import com.google.gson.Gson
import com.issueissyu.fe.data.remote.api.AlarmApi
import com.issueissyu.fe.data.remote.dto.request.alarm.StoreTokenRequest
import com.issueissyu.fe.data.remote.dto.response.alarm.activeFor
import com.issueissyu.fe.data.remote.dto.response.alarm.toNotificationPage
import com.issueissyu.fe.data.remote.dto.response.alarm.toAlarmToggleState
import com.issueissyu.fe.domain.model.notification.AlarmToggleState
import com.issueissyu.fe.domain.model.notification.Notification
import com.issueissyu.fe.domain.model.notification.NotificationPage
import com.issueissyu.fe.domain.model.notification.NotificationType
import com.issueissyu.fe.domain.model.notification.PushAlarmContext
import com.issueissyu.fe.domain.repository.AlarmRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepositoryImpl @Inject constructor(
    private val alarmApi: AlarmApi,
    private val gson: Gson,
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
        size: Int,
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

    override suspend fun confirmAlarm(alarmId: Long): Result<Unit> = try {
        val response = alarmApi.confirmAlarm(alarmId)
        when (response.code) {
            "ALARM_CONFIRM_200" -> Result.success(Unit)
            "ALARM_CONFIRM_400" -> Result.failure(
                toConfirmAlarmException(
                    code = response.code,
                    message = response.message,
                    fallbackMessage = "존재하지 않는 알람입니다.",
                ),
            )
            else -> Result.failure(
                toConfirmAlarmException(
                    code = response.code,
                    message = response.message,
                ),
            )
        }
    } catch (e: HttpException) {
        val errorEnvelope = e.response()?.errorBody()?.string()
            ?.takeIf { it.isNotBlank() }
            ?.let { body ->
                runCatching {
                    gson.fromJson(body, AlarmErrorEnvelope::class.java)
                }.getOrNull()
            }

        Result.failure(
            toConfirmAlarmException(
                code = errorEnvelope?.code,
                message = errorEnvelope?.message ?: e.message(),
                httpStatus = e.code(),
            ),
        )
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun confirmPushAlarm(context: PushAlarmContext): Result<Unit> {
        val alarmId = resolveAlarmIdFromList(context) ?: context.fcmAlarmId
            ?: return Result.failure(Exception("확인할 알람을 찾을 수 없습니다."))
        return confirmAlarm(alarmId)
    }

    override suspend fun getAlarmToggleState(): Result<AlarmToggleState> = try {
        val response = alarmApi.getAlarmToggle()
        when (response.code) {
            "ALARM_STATE_200" -> {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "알림 설정 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toAlarmToggleState())
            }
            else -> if (response.isSuccess && response.result != null) {
                Result.success(response.result.toAlarmToggleState())
            } else {
                Result.failure(
                    Exception(
                        response.message.takeIf { it.isNotBlank() } ?: "알림 설정을 불러오지 못했습니다.",
                    ),
                )
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateAlarmToggle(
        alarmType: NotificationType,
    ): Result<Boolean> = try {
        val response = alarmApi.updateAlarmToggle(alarmType.name)
        when (response.code) {
            "USER_ALARM_200_1",
            "USER_ALARM_200_2",
            "USER_ALARM_200_3",
            "USER_ALARM_200_4",
            -> {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "알림 설정 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.activeFor(alarmType))
            }
            "USER_ALARM_400" -> Result.failure(
                Exception(
                    response.message.takeIf { it.isNotBlank() } ?: "존재하지 않는 알람 설정입니다.",
                ),
            )
            else -> if (response.isSuccess && response.result != null) {
                Result.success(response.result.activeFor(alarmType))
            } else {
                Result.failure(
                    Exception(
                        response.message.takeIf { it.isNotBlank() } ?: "알림 설정 변경에 실패했습니다.",
                    ),
                )
            }
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

    private suspend fun resolveAlarmIdFromList(context: PushAlarmContext): Long? {
        val type = NotificationType.fromServer(context.pushType) ?: return null
        val page = getAlarmList(size = PUSH_ALARM_LOOKUP_PAGE_SIZE).getOrNull() ?: return null
        val candidates = page.items.filter { notification ->
            matchesPushAlarm(notification, type, context)
        }
        return candidates.firstOrNull { it.isUnread }?.alarmId
            ?: candidates.firstOrNull()?.alarmId
    }

    private fun matchesPushAlarm(
        notification: Notification,
        type: NotificationType,
        context: PushAlarmContext,
    ): Boolean {
        if (notification.type != type) return false
        return when (type) {
            NotificationType.LIKE -> {
                context.pinId != null && notification.pinId == context.pinId
            }
            NotificationType.EVENT,
            NotificationType.HOT,
            NotificationType.STORE,
            -> {
                context.communityId != null && notification.communityId == context.communityId
            }
        }
    }

    private fun toConfirmAlarmException(
        code: String?,
        message: String?,
        fallbackMessage: String = "알람 확인에 실패했습니다.",
        httpStatus: Int? = null,
    ): Exception {
        val body = message?.takeIf { it.isNotBlank() } ?: fallbackMessage
        val prefix = when {
            !code.isNullOrBlank() -> "[$code]"
            httpStatus != null -> "[HTTP $httpStatus]"
            else -> null
        }
        val display = prefix?.let { "$it $body" } ?: body
        return Exception(display)
    }

    private data class AlarmErrorEnvelope(
        val code: String?,
        val message: String?,
    )

    companion object {
        private const val PUSH_ALARM_LOOKUP_PAGE_SIZE = 50
    }
}
