package com.issueissyu.fe.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TermUiState(
    val isAllAgreed: Boolean = false,
    val isServiceAgreed: Boolean = false,
    val isPrivacyAgreed: Boolean = false,
    val isLocationAgreed: Boolean = false,
    val isMarketingAgreed: Boolean = false,
    val submitError: String? = null,
)

@HiltViewModel
class TermViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TermUiState())
    val uiState: StateFlow<TermUiState> = _uiState.asStateFlow()

    fun consumeSubmitError() {
        _uiState.update { it.copy(submitError = null) }
    }

    fun submitTerms(
        onSuccess: (requestLocationPermission: Boolean, requestNotificationPermission: Boolean) -> Unit,
    ) {
        val s = _uiState.value
        if (!s.isServiceAgreed || !s.isPrivacyAgreed) return

        viewModelScope.launch {
            _uiState.update { it.copy(submitError = null) }

            authRepository.submitTermsAgreement(
                serviceTerm = s.isServiceAgreed,
                privacyTerm = s.isPrivacyAgreed,
                locationTerm = s.isLocationAgreed,
                marketingTerm = s.isMarketingAgreed,
            ).fold(
                onSuccess = {
                    onSuccess(s.isLocationAgreed, s.isMarketingAgreed)
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(submitError = e.message ?: "약관 동의에 실패했습니다")
                    }
                },
            )
        }
    }

    fun onAllAgreementChanged(checked: Boolean) {
        _uiState.update {
            if (!checked) {
                it.copy(
                    isAllAgreed = false,
                    isServiceAgreed = false,
                    isPrivacyAgreed = false,
                    isLocationAgreed = false,
                    isMarketingAgreed = false,
                )
            } else {
                it.copy(
                    isAllAgreed = true,
                    isServiceAgreed = true,
                    isPrivacyAgreed = true,
                    isLocationAgreed = true,
                    isMarketingAgreed = true,
                )
            }
        }
    }

    fun onLocationChanged(checked: Boolean) {
        _uiState.update { it.copy(isLocationAgreed = checked) }
        updateAllAgreementsState()
    }

    fun onMarketingChanged(checked: Boolean) {
        _uiState.update { it.copy(isMarketingAgreed = checked) }
        updateAllAgreementsState()
    }

    private fun updateAllAgreementsState() {
        _uiState.update { currentState ->
            currentState.copy(
                isAllAgreed = currentState.isServiceAgreed &&
                    currentState.isPrivacyAgreed &&
                    currentState.isLocationAgreed &&
                    currentState.isMarketingAgreed,
            )
        }
    }

    fun onServiceChanged(checked: Boolean) {
        _uiState.update { it.copy(isServiceAgreed = checked) }
        updateAllAgreementsState()
    }

    fun onPrivacyChanged(checked: Boolean) {
        _uiState.update { it.copy(isPrivacyAgreed = checked) }
        updateAllAgreementsState()
    }
}
