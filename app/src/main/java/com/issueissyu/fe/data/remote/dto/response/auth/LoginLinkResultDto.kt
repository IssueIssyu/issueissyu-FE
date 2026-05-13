package com.issueissyu.fe.data.remote.dto.response.auth
data class LoginLinkResultDto(
    val uuid: String? = null,
    val nickname: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val availableNickname: Boolean? = null,
    val socialType: String? = null,
    val eventAlarmActive: Boolean? = null,
    val likeAlarmActive: Boolean? = null,
    val hotAlarmActive: Boolean? = null,
    val storeAlarmActive: Boolean? = null,
    val isAvailableNickname: Boolean? = null,
    val userPhoneVerified: Boolean? = null,
)
