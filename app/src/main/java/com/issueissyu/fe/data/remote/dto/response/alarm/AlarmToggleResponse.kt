package com.issueissyu.fe.data.remote.dto.response.alarm

import com.issueissyu.fe.domain.model.notification.AlarmToggleState
import com.issueissyu.fe.domain.model.notification.NotificationType

data class AlarmToggleResponse(
    val likeAlarmActive: Boolean = false,
    val eventAlarmActive: Boolean = false,
    val hotAlarmActive: Boolean = false,
    val storeAlarmActive: Boolean = false,
)

fun AlarmToggleResponse.toAlarmToggleState(): AlarmToggleState {
    return AlarmToggleState(
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