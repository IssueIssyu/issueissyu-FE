package com.issueissyu.fe.data.remote.dto.response.auth

data class TermResponse(
    val eventAlarmActive: Boolean,
    val likeAlarmActive: Boolean,
    val hotAlarmActive: Boolean,
    val storeAlarmActive: Boolean
)