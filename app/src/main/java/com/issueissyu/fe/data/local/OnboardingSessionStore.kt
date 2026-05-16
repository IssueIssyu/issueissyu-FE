package com.issueissyu.fe.data.local

import javax.inject.Inject
import javax.inject.Singleton

// 본인 인증 입력값
@Singleton
class OnboardingSessionStore @Inject constructor() {

    private var pendingNickname: String? = null
    private var pendingEmail: String? = null
    private var pendingPhone: String? = null

    fun setPendingProfile(nickname: String, email: String, phone: String) {
        pendingNickname = nickname
        pendingEmail = email
        pendingPhone = phone
    }

    fun getPendingProfile(): Triple<String, String, String>? {
        val n = pendingNickname ?: return null
        val e = pendingEmail ?: return null
        val p = pendingPhone ?: return null
        return Triple(n, e, p)
    }

    fun clearPendingProfile() {
        pendingNickname = null
        pendingEmail = null
        pendingPhone = null
    }
}
