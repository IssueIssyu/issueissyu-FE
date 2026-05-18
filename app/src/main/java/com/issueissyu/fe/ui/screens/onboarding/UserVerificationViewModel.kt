package com.issueissyu.fe.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.local.OnboardingSessionStore
import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.domain.auth.AccountAlreadyLinkedException
import com.issueissyu.fe.domain.auth.ExistingPhoneRequiresLinkException
import com.issueissyu.fe.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserVerificationUiState(
    val nickname: String = "",
    val isNicknameChecked: Boolean = false,
    val nicknameError: String? = null,
    val isCheckingNickName: Boolean = false,

    val emailId: String = "",
    val emailDomain: String = "선택",
    val customDomain: String = "",
    val showDomainDropdown: Boolean = false,

    val phoneNumber: String = "",
    val phoneError: String? = null,
    val isVerificationCodeSent: Boolean = false,
    val isSendingCode: Boolean = false,

    val verificationCode: String = "",
    val isCodeVerified: Boolean = false,
    val codeError: String? = null,
    val isVerifyingCode: Boolean = false,

    val isSignupEnabled: Boolean = false,

    val showAccountLinkDialog: Boolean = false,
    val isLinkingAccount: Boolean = false,
    val accountLinkError: String? = null,

    val showLocalPhoneRegisteredDialog: Boolean = false,

    val showAlreadyLinkedDialog: Boolean = false,
    val alreadyLinkedMessage: String? = null,

    val showLinkCompletedDialog: Boolean = false,
)

@HiltViewModel
class UserVerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val onboardingSessionStore: OnboardingSessionStore,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserVerificationUiState())
    val uiState: StateFlow<UserVerificationUiState> = _uiState.asStateFlow()

    companion object {
        const val MIN_NICKNAME_LENGTH = 2
        const val MAX_NIXKNAME_LENGTH = 10
        const val PHONE_NUMBER_LENGTH = 13
        const val VerificationCodeLength = 6
    }

    private fun updateSignupEnabled() {
        val state = _uiState.value
        val isEmailValid = state.emailId.isNotBlank() && when (state.emailDomain) {
            "선택" -> false
            "직접 입력" -> state.customDomain.isNotBlank()
            else -> true
        }

        _uiState.update {
            it.copy(
                isSignupEnabled = state.isNicknameChecked
                    && isEmailValid
                    && state.isCodeVerified,
            )
        }
    }

    private fun sessionSocialTypeForUi(): String =
        tokenManager.getLoginSocialType()?.takeIf { it.isNotBlank() } ?: "LOCAL"

    fun dismissLocalPhoneRegisteredDialog() {
        _uiState.update {
            it.copy(showLocalPhoneRegisteredDialog = false)
        }
    }

    fun onLocalPhoneRegisteredGoToLogin(onNavigateToLogin: () -> Unit) {
        viewModelScope.launch {
            exitToLogin()
            _uiState.update { it.copy(showLocalPhoneRegisteredDialog = false) }
            onNavigateToLogin()
        }
    }

    fun dismissAlreadyLinkedDialog() {
        _uiState.update {
            it.copy(
                showAlreadyLinkedDialog = false,
                alreadyLinkedMessage = null,
            )
        }
    }

    fun onAlreadyLinkedGoToLogin(onNavigateToLogin: () -> Unit) {
        viewModelScope.launch {
            exitToLogin()
            _uiState.update {
                it.copy(
                    showAlreadyLinkedDialog = false,
                    alreadyLinkedMessage = null,
                    showAccountLinkDialog = false,
                    accountLinkError = null,
                )
            }
            onNavigateToLogin()
        }
    }

    fun onNicknameChange(nickname: String) {
        _uiState.update {
            it.copy(
                nickname = nickname,
                isNicknameChecked = false,
                nicknameError = null,
            )
        }
        updateSignupEnabled()
    }

    fun checkNicknameDuplicate() {
        val nickname = _uiState.value.nickname

        when {
            nickname.isEmpty() -> {
                _uiState.update { it.copy(nicknameError = "닉네임을 입력해주세요") }
                return
            }
            nickname.length < MIN_NICKNAME_LENGTH || nickname.length > MAX_NIXKNAME_LENGTH -> {
                _uiState.update {
                    it.copy(nicknameError = "닉네임은 ${MIN_NICKNAME_LENGTH}~${MAX_NIXKNAME_LENGTH}자 내로 입력해주세요")
                }
                return
            }
            !nickname.matches(Regex("^[가-힣a-zA-Z0-9]+$")) -> {
                _uiState.update { it.copy(nicknameError = "닉네임은 한글, 영문, 숫자만 사용 가능합니다") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingNickName = true) }

            authRepository.checkNicknameAvailable(nickname).fold(
                onSuccess = { available ->
                    _uiState.update {
                        it.copy(
                            isCheckingNickName = false,
                            isNicknameChecked = available,
                            nicknameError = if (available) null else "이미 사용 중인 닉네임입니다",
                        )
                    }
                    updateSignupEnabled()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            nicknameError = e.message ?: "오류가 발생했습니다",
                            isCheckingNickName = false,
                        )
                    }
                },
            )
        }
    }

    fun onEmailIdChange(emailId: String) {
        _uiState.update { it.copy(emailId = emailId) }
        updateSignupEnabled()
    }

    fun onEmailDomainChange(domain: String) {
        _uiState.update {
            it.copy(
                emailDomain = domain,
                showDomainDropdown = false,
            )
        }
        updateSignupEnabled()
    }

    fun onCustomDomainChange(customDomain: String) {
        _uiState.update { it.copy(customDomain = customDomain) }
        updateSignupEnabled()
    }

    fun toggleDomainDropdown() {
        _uiState.update { it.copy(showDomainDropdown = !it.showDomainDropdown) }
    }

    fun getFullEmail(): String {
        val state = _uiState.value
        return if (state.emailDomain == "직접 입력") {
            "${state.emailId}@${state.customDomain}"
        } else {
            "${state.emailId}@${state.emailDomain}"
        }
    }

    fun onPhoneNumberChange(phoneNumber: String) {
        val sanitized = phoneNumber
            .filter { it.isDigit() || it == '-' }
            .take(PHONE_NUMBER_LENGTH)

        _uiState.update {
            it.copy(
                phoneNumber = sanitized,
                phoneError = null,
                isVerificationCodeSent = false,
                isCodeVerified = false,
                verificationCode = "",
                showAccountLinkDialog = false,
                accountLinkError = null,
                showLocalPhoneRegisteredDialog = false,
                showAlreadyLinkedDialog = false,
                alreadyLinkedMessage = null,
                showLinkCompletedDialog = false,
            )
        }
        updateSignupEnabled()
    }

    fun sendVerificationCode() {
        val phoneNumber = _uiState.value.phoneNumber

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingCode = true, phoneError = null) }

            authRepository.sendPhoneVerificationCode(phoneNumber).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isVerificationCodeSent = true,
                            phoneError = null,
                            isSendingCode = false,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            phoneError = e.message ?: "오류가 발생했습니다",
                            isSendingCode = false,
                        )
                    }
                },
            )
        }
    }

    fun onVerificationCodeChange(code: String) {
        _uiState.update {
            it.copy(
                verificationCode = code,
                isCodeVerified = false,
                codeError = null,
            )
        }
    }


    fun verifyCode() {
        if (_uiState.value.isVerifyingCode) return

        val code = _uiState.value.verificationCode
        val phone = _uiState.value.phoneNumber

        if (!_uiState.value.isVerificationCodeSent) {
            _uiState.update { it.copy(codeError = "먼저 인증번호를 받아주세요") }
            return
        }

        if (code.length != VerificationCodeLength) {
            _uiState.update { it.copy(codeError = "인증번호 6자리를 입력해주세요") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingCode = true) }

            authRepository.verifyPhoneCode(
                phoneDigits = phone,
                code = code,
                isAvailableNickname = _uiState.value.isNicknameChecked,
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isCodeVerified = true,
                            codeError = null,
                            isVerifyingCode = false,
                        )
                    }
                    updateSignupEnabled()
                },
                onFailure = { e ->
                    if (e is ExistingPhoneRequiresLinkException) {
                        val isLocalSession = sessionSocialTypeForUi().equals("LOCAL", ignoreCase = true)
                        _uiState.update {
                            it.copy(
                                isCodeVerified = false,
                                isVerifyingCode = false,
                                showAccountLinkDialog = !isLocalSession,
                                showLocalPhoneRegisteredDialog = isLocalSession,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isCodeVerified = false,
                                codeError = e.message ?: "인증에 실패했습니다",
                                isVerifyingCode = false,
                            )
                        }
                    }
                    updateSignupEnabled()
                },
            )
        }
    }

    fun dismissAccountLinkDialog() {
        _uiState.update {
            it.copy(
                showAccountLinkDialog = false,
                accountLinkError = null,
            )
        }
    }

    fun confirmAccountLink() {
        val phone = _uiState.value.phoneNumber
        val socialType = tokenManager.getLoginSocialType()?.takeIf { it.isNotBlank() } ?: "LOCAL"
        viewModelScope.launch {
            _uiState.update { it.copy(isLinkingAccount = true, accountLinkError = null) }
            authRepository.linkLogin(phone, socialType).fold(
                onSuccess = {
                    onboardingSessionStore.clearPendingProfile()
                    _uiState.update {
                        it.copy(
                            showAccountLinkDialog = false,
                            isLinkingAccount = false,
                            accountLinkError = null,
                            showLinkCompletedDialog = true,
                        )
                    }
                },
                onFailure = { e ->
                    when (e) {
                        is AccountAlreadyLinkedException -> {
                            _uiState.update {
                                it.copy(
                                    isLinkingAccount = false,
                                    showAccountLinkDialog = false,
                                    accountLinkError = null,
                                    showAlreadyLinkedDialog = true,
                                    alreadyLinkedMessage = e.message,
                                )
                            }
                        }
                        else -> {
                            _uiState.update {
                                it.copy(
                                    isLinkingAccount = false,
                                    accountLinkError = e.message ?: "연동에 실패했습니다",
                                )
                            }
                        }
                    }
                },
            )
        }
    }

    fun onLinkCompletedAcknowledged(onNavigateToLogin: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(showLinkCompletedDialog = false) }
            exitToLogin()
            onNavigateToLogin()
        }
    }

    private suspend fun exitToLogin() {
        authRepository.logout()
        onboardingSessionStore.clearPendingProfile()
    }

    fun onSignupClick(onComplete: (nickname: String, email: String, phoneNumber: String) -> Unit) {
        if (!_uiState.value.isSignupEnabled) return
        val s = _uiState.value
        val email = getFullEmail()
        onboardingSessionStore.setPendingProfile(
            nickname = s.nickname,
            email = email,
            phone = s.phoneNumber,
        )
        onComplete(s.nickname, email, s.phoneNumber)
    }
}
