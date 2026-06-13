package com.issueissyu.fe.data.remote.dto.response.alarm

import com.issueissyu.fe.domain.model.notification.AlarmToggleState

data class AlarmToggleResponse(
    val likeAlarmActive: Boolean,
    val eventAlarmActive: Boolean,
    val hotAlarmActive: Boolean,
    val storeAlarmActive: Boolean,
)

fun AlarmToggleResponse.toAlarmToggleState(): AlarmToggleState {
    return AlarmToggleState(
        likeAlarmActive = likeAlarmActive,
        eventAlarmActive = eventAlarmActive,
        hotAlarmActive = hotAlarmActive,
        storeAlarmActive = storeAlarmActive,
    )
}