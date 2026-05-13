package com.issueissyu.fe.data.local

import com.issueissyu.fe.domain.model.OnboardingProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingSessionStore @Inject constructor() {

    private var pendingNickname: String? = null
    private var pendingEmail: String? = null
    private var pendingPhone: String? = null

    var lastCompletedProfile: OnboardingProfile? = null
        private set

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

    fun setCompletedProfile(profile: OnboardingProfile) {
        lastCompletedProfile = profile
    }

    fun clearCompletedProfile() {
        lastCompletedProfile = null
    }
}
