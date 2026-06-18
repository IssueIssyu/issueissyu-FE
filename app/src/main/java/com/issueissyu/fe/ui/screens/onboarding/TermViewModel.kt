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
    val isSubmitting: Boolean = false,
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
        onAgreeClick: () -> Unit,
        requestLocation: () -> Unit,
        requestNotification: () -> Unit,
    ) {
        val s = _uiState.value
        if (!s.isServiceAgreed || !s.isPrivacyAgreed || s.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }

            authRepository.submitTermsAgreement(
                serviceTerm = s.isServiceAgreed,
                privacyTerm = s.isPrivacyAgreed,
                locationTerm = s.isLocationAgreed,
                marketingTerm = s.isMarketingAgreed,
            ).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false) }
                    when {
                        s.isLocationAgreed -> requestLocation()
                        s.isMarketingAgreed -> requestNotification()
                        else -> onAgreeClick()
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitError = e.message ?: "약관 동의에 실패했습니다",
                        )
                    }
                },
            )
        }
    }

    fun onAllAgreementChanged(checked: Boolean) {
        _uiState.update {
            it.copy(
                isAllAgreed = checked,
                isServiceAgreed = checked,
                isPrivacyAgreed = checked,
                isLocationAgreed = checked,
                isMarketingAgreed = checked,
            )
        }
    }

    fun wasMarketingChecked(): Boolean = _uiState.value.isMarketingAgreed

    fun onLocationAgreementChanged(agreed: Boolean) {
        _uiState.update { it.copy(isLocationAgreed = agreed) }
        updateAllAgreementsState()
    }

    // 알림 선택 약관 체크 상태 갱신
    fun onMarketingAgreementChanged(agreed: Boolean) {
        _uiState.update { it.copy(isMarketingAgreed = agreed) }
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
