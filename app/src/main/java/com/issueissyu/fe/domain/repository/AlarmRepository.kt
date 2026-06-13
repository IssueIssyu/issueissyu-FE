package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.notification.NotificationPage

interface AlarmRepository {
    suspend fun storePushToken(fcmPushToken: String): Result<Unit>

    suspend fun getAlarmList(
        size: Int = DEFAULT_PAGE_SIZE,
        cursor: String? = null,
    ): Result<NotificationPage>

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
    }
}
