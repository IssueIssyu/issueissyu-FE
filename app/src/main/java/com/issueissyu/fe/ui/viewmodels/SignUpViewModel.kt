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

data class SignUpUiState(
    val userId: String = "",
    val userPw: String = "",
    val userPwConfirm: String = "",

    val isIdChecked: Boolean = false,
    val isIdChecking: Boolean = false,
    val isSubmitting: Boolean = false,

    val userIdError: String? = null,
    val userPwError: String? = null,
    val userPwConfirmError: String? = null,

    val signUpSuccess: Boolean = false,
){
    val canSubmit: Boolean
        get() = userId.isNotEmpty() &&
                userPw.isNotEmpty() &&
                userPwConfirm.isNotEmpty() &&
                userIdError == null &&
                userPwError == null &&
                userPwConfirmError == null &&
                isIdChecked &&
                !isSubmitting
}

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    //상태 업데이트
    private fun updateState (update: SignUpUiState. () -> SignUpUiState){
        _uiState.update { it.update() }
    }

    //아이디
    fun updateUserId(newId: String) {
        updateState {
            copy(
                userId = newId,
                isIdChecked = false,
                userIdError = validateUserId(newId)
            )
        }
    }

    //비밀번호
    fun updateUserPw(newPw: String){
        updateState {
            copy(
                userPw = newPw,
                userPwError = validateUserPw(newPw),
                userPwConfirmError =
                    if(userPwConfirm.isNotEmpty()) {
                        validateUserPwConfirm(newPw, userPwConfirm)
                    } else {
                        userPwConfirmError
                    }
            )
        }
    }

    //비밀번호 확인
    fun updateUserPwConfirm(newPwConfirm: String){
        updateState {
            copy(
                userPwConfirm = newPwConfirm,
                userPwConfirmError = validateUserPwConfirm(userPw, newPwConfirm)
            )
        }
    }

    //아이디 중복 확인
    fun checkIdDuplicate(){
        //id 오류 시, 중복 확인 작동 x
        if(_uiState.value.userIdError != null) return

        viewModelScope.launch {
            updateState{  copy(isIdChecking = true) }

            delay(1000)     //더미

            updateState {
                copy(
                    isIdChecking = false,
                    isIdChecked = true,
                    userIdError = null
                )
            }
        }
    }

    //회원가입 완료
    fun signUp(){
        //중복 제출 방지
        if (_uiState.value.isSubmitting) return

        viewModelScope.launch {
            updateState { copy(isSubmitting = true) }

            delay(2000)     //더미

            updateState {
                copy(
                    isSubmitting = false,
                    signUpSuccess = true
                )
            }
        }
    }

    //검증
    private fun validateUserId(id: String): String? {
        return when {
            id.isEmpty() -> null
            id.length < 4 -> "4자 이상 입력해주세요"
            !id.matches(Regex("^[a-z][a-z0-9]*\$")) -> "영문 소문자로 시작해야 합니다"
            else -> null
        }
    }

    private fun validateUserPw(pw:String): String?{
        return when{
            pw.isEmpty() -> null
            pw.length < 6 -> "6자 이상 입력해주세요"
            !validatePassword(pw)-> "2가지 이상 조합이 필요합니다."
            else -> null
        }
    }

    private fun validatePassword(pw: String): Boolean {
        val hasEnglish = pw.any { it in 'a'..'z' || it in 'A'..'Z' }
        val hasDigit = pw.any { it.isDigit() }
        val hasSpecial = pw.any { it in "!@#$%^&*()_+-=[]{};\':\"\\|,.<>/?" }

        val count = listOf(hasEnglish, hasDigit, hasSpecial).count { it }

        return count >= 2
    }

    private fun validateUserPwConfirm(pw: String, pwConfirm:String): String?{
        return when{
            pwConfirm.isEmpty() -> null
            pwConfirm != pw -> "비밀번호 불일치"
            else -> null
        }
    }

    fun consumeSignUpSuccess() {
        //다음 화면으로 이동 시 false로 초기화
        //재사용 방지
        updateState { copy(signUpSuccess = false) }
    }
}