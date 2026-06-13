package com.issueissyu.fe.domain.model.notification

data class AlarmToggleState(
    val likeAlarmActive: Boolean,
    val eventAlarmActive: Boolean,
    val hotAlarmActive: Boolean,
    val storeAlarmActive: Boolean,
)
