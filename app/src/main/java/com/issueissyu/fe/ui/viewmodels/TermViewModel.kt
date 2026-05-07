package com.issueissyu.fe.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class TermUiState(
    val isAllAgreed: Boolean = false,
    val isServiceAgreed: Boolean = false,
    val isPrivacyAgreed: Boolean = false,
    val isLocationAgreed: Boolean = false,
    val isMarketingAgreed: Boolean = false
)

@HiltViewModel

class TermViewModel @Inject constructor(): ViewModel() {
    private val _uiState = MutableStateFlow(TermUiState())
    val uiState: StateFlow<TermUiState> = _uiState.asStateFlow()

    //전체 동의
    fun onAllAgreementChanged(checked: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isAllAgreed = checked,
                isServiceAgreed = checked,
                isLocationAgreed = checked,
                isPrivacyAgreed = checked,
                isMarketingAgreed = checked
            )
        }
    }

    //전체 동의 업데이트
    private fun updateAllAgreementsState(){
        _uiState.update { currentState ->
            currentState.copy(
                isAllAgreed = currentState.isServiceAgreed &&
                    currentState.isPrivacyAgreed &&
                    currentState.isLocationAgreed &&
                    currentState.isMarketingAgreed
            )
        }
    }

    //서비스 동의
    fun onServiceChanged(checked: Boolean) {
        _uiState.update { it.copy(isServiceAgreed = checked) }
        updateAllAgreementsState()
    }

    //개인정보 수집 동의
    fun onPrivacyChanged(checked: Boolean) {
        _uiState.update { it. copy(isPrivacyAgreed = checked)}
        updateAllAgreementsState()
    }

    //위치 기반 서비스 동의
    fun onLocationChanged(checked: Boolean){
        _uiState.update { it.copy(isLocationAgreed = checked)}
        updateAllAgreementsState()
    }

    //푸시 알림 수신 동의
    fun onMarketingChanged(checked: Boolean){
        _uiState.update { it.copy(isMarketingAgreed = checked) }
        updateAllAgreementsState()
    }
}