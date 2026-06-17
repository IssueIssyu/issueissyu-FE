package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.notification.AlarmToggleState
import com.issueissyu.fe.domain.model.notification.NotificationPage
import com.issueissyu.fe.domain.model.notification.NotificationType
import com.issueissyu.fe.domain.model.notification.PushAlarmContext

interface AlarmRepository {
    suspend fun storePushToken(fcmPushToken: String): Result<Unit>

    suspend fun getAlarmList(
        size: Int = DEFAULT_PAGE_SIZE,
        cursor: String? = null,
    ): Result<NotificationPage>

    suspend fun confirmAlarm(alarmId: Long): Result<Unit>

    /**
     * 푸시 탭 confirm. FCM *AlarmId가 서버 alarm PK와 다를 수 있어
     * [type + pinId/communityId]로 목록 alarmId를 우선 매칭한다.
     */
    suspend fun confirmPushAlarm(context: PushAlarmContext): Result<Unit>

    suspend fun getAlarmToggleState(): Result<AlarmToggleState>

    suspend fun updateAlarmToggle(alarmType: NotificationType): Result<Boolean>

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
    }
}
