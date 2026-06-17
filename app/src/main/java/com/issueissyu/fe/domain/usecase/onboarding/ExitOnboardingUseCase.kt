package com.issueissyu.fe.domain.usecase.onboarding

import com.issueissyu.fe.data.local.OnboardingSessionStore
import com.issueissyu.fe.domain.repository.AuthRepository
import javax.inject.Inject

class ExitOnboardingUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val onboardingSessionStore: OnboardingSessionStore,
) {
    suspend operator fun invoke() {
        authRepository.logout()
        onboardingSessionStore.clearPendingProfile()
    }
}
