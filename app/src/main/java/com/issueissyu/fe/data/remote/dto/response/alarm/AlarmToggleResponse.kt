package com.issueissyu.fe.data.remote.dto.response.alarm

import com.issueissyu.fe.domain.model.TermsAgreementResult
import com.issueissyu.fe.domain.model.notification.NotificationType

data class AlarmToggleResponse(
    val likeAlarmActive: Boolean,
    val eventAlarmActive: Boolean,
    val hotAlarmActive: Boolean,
    val storeAlarmActive: Boolean,
)

fun AlarmToggleResponse.toTermsAgreementResult(): TermsAgreementResult {
    return TermsAgreementResult(
        eventAlarmActive = eventAlarmActive,
        likeAlarmActive = likeAlarmActive,
        hotAlarmActive = hotAlarmActive,
        storeAlarmActive = storeAlarmActive,
    )
}

fun AlarmToggleResponse.activeFor(type: NotificationType): Boolean {
    return when (type) {
        NotificationType.LIKE -> likeAlarmActive
        NotificationType.EVENT -> eventAlarmActive
        NotificationType.HOT -> hotAlarmActive
        NotificationType.STORE -> storeAlarmActive
    }
}