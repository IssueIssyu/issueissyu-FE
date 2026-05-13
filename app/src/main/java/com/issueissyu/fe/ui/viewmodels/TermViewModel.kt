package com.issueissyu.fe.ui.viewmodels

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

    fun submitTerms(onSuccess: () -> Unit) {
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
                    onSuccess()
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
        if (!checked) {
            _uiState.update {
                it.copy(
                    isAllAgreed = false,
                    isServiceAgreed = false,
                    isPrivacyAgreed = false,
                    isLocationAgreed = false,
                    isMarketingAgreed = false,
                )
            }
            return
        }

        // 전체 동의 on: 필수는 즉시 체크, 선택은 권한 승인 결과에 따라 별도로 반영
        _uiState.update { s ->
            s.copy(
                isServiceAgreed = true,
                isPrivacyAgreed = true,
            )
        }
        updateAllAgreementsState()
    }

    /** 위치 선택 약관 체크 상태 갱신 (권한 승인 결과로만 true). */
    fun onLocationAgreementChanged(agreed: Boolean) {
        _uiState.update { it.copy(isLocationAgreed = agreed) }
        updateAllAgreementsState()
    }

    /** 알림 선택 약관 체크 상태 갱신 (권한 승인 결과로만 true). */
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
