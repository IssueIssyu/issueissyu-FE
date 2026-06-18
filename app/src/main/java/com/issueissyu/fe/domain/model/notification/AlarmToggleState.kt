package com.issueissyu.fe.domain.model.notification

data class AlarmToggleState(
    val eventAlarmActive: Boolean,
    val likeAlarmActive: Boolean,
    val hotAlarmActive: Boolean,
    val storeAlarmActive: Boolean,
)
