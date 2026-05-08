package com.issueissyu.fe.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserVerificationUiState(
    val nickname: String ="",
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

    val isSignupEnabled: Boolean = false
)

@HiltViewModel
class UserVerificationViewModel @Inject constructor(): ViewModel() {
    private val _uiState = MutableStateFlow(UserVerificationUiState())
    val uiState: StateFlow<UserVerificationUiState> = _uiState.asStateFlow()

    //조건-길이
    companion object {
        const val MIN_NICKNAME_LENGTH = 2
        const val MAX_NIXKNAME_LENGTH = 10
        const val PHONE_NUMBER_LENGTH = 11
        const val VERIFIED_CODE = "123456"
    }

    //회원가입 상태 업데이트
    private fun updateSignupEnabled() {
        val state = _uiState.value
        val isEmailValid = state.emailId.isNotBlank() && when(state.emailDomain){
            "선택" -> false
            "직접 입력" -> state.customDomain.isNotBlank()
            else -> true
        }

        _uiState.update {
            it.copy(
                isSignupEnabled = state.isNicknameChecked
                        && isEmailValid
                        && state.isCodeVerified
            )
        }
    }

    //닉네임
    fun onNicknameChange(nickname: String){
        _uiState.update{
            it.copy(
                nickname = nickname,
                isNicknameChecked = false,
                nicknameError = null
            )
        }
        updateSignupEnabled()
    }


    fun checkNicknameDuplicate(){
        val nickname = _uiState.value.nickname

        when {
            //1. 닉네임 입력 x
            nickname.isEmpty() -> {
                _uiState.update { it.copy(nicknameError = "닉네임을 입력해주세요")
                }
                return
            }
            //2. 닉네임 길이
            nickname.length < MIN_NICKNAME_LENGTH || nickname.length > MAX_NIXKNAME_LENGTH -> {
                _uiState.update {
                    it.copy(nicknameError = "닉네임은 ${MIN_NICKNAME_LENGTH}~${MAX_NIXKNAME_LENGTH}자 내로 입력해주세요")
                }
                return
            }
            //3. 닉네임 문자 (특수기호 제한)
            !nickname.matches(Regex("^[가-힣a-zA-Z0-9]+$")) -> {
                _uiState.update { it.copy(nicknameError = "닉네임은 한글, 영문, 숫자만 사용 가능합니다")
                }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingNickName = true) }

            try {
                //중복 확인 API

                delay(500)
                _uiState.update {
                    it.copy(
                        isNicknameChecked = true,
                        nicknameError = null,
                        isCheckingNickName = false
                    )
                }
                updateSignupEnabled()
            } catch(e: Exception) {
                _uiState.update {
                    it.copy(
                        nicknameError = "오류가 발생했습니다",
                        isCheckingNickName = false
                    )
                }
            }
        }
    }

    //이메일
    fun onEmailIdChange(emailId: String){
        _uiState.update { it.copy(emailId = emailId)}
        updateSignupEnabled()
    }

    //이메일 도메인 선택
    fun onEmailDomainChange(domain: String) {
        _uiState.update {
            it.copy(
                emailDomain = domain,
                showDomainDropdown = false  //누르면 활성화
            )
        }
        updateSignupEnabled()
    }

    fun onCustomDomainChange(customDomain: String){
        _uiState.update { it.copy(customDomain = customDomain) }
        updateSignupEnabled()
    }

    fun toggleDomainDropdown(){
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
        val filtered = phoneNumber.filter { it.isDigit() }

        _uiState.update {
            it.copy(
                phoneNumber = filtered,
                phoneError = null,
                isVerificationCodeSent = false,
                isCodeVerified = false,
                verificationCode = ""
            )
        }
    }

    fun sendVerificationCode() {
        val phoneNumber = _uiState.value.phoneNumber
        //전화번호 입력 실패 -> "올바른 전화번호 입력해주세요

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingCode = true)}

            try{
                //인증 번호 API 호출
                delay(500)
                _uiState.update {
                    it.copy(
                        isVerificationCodeSent = true,
                        phoneError = null,
                        isSendingCode = false
                    )
                }
            } catch(e: Exception) {
                _uiState.update {
                    it.copy(
                        phoneError = "오류가 발생했습니다",
                        isSendingCode = false
                    )
                }
            }
        }
    }

    fun onVerificationCodeChange(code: String){
        _uiState.update {
            it.copy(
                verificationCode = code,
                isCodeVerified = false,
                codeError = null
            )
        }
    }

    fun verifyCode(){
        val code = _uiState.value.verificationCode

        if(code.isEmpty()){
            _uiState.update { it.copy(codeError = "인증번호를 입력해주세요")}
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingCode = true)}

            try {
                //API 호출

                delay(500)

                if (code == VERIFIED_CODE) {
                    _uiState.update {
                        it.copy(
                            isCodeVerified = true,
                            codeError = null,
                            isVerifyingCode = false
                        )
                    }
                    updateSignupEnabled()
                } else {
                    _uiState.update {
                        it.copy(
                            isCodeVerified = false,
                            codeError = "인증번호가 일치하지 않습니다",
                            isVerifyingCode = false
                        )
                    }
                    updateSignupEnabled()
                }
            } catch(e: Exception){
                _uiState.update {
                    it.copy(
                        codeError = "오류가 발생했습니다",
                        isVerifyingCode = false
                    )
                }
            }
        }
    }
}