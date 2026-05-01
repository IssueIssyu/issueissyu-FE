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

    //회원가입 오류
    val userIdError: String? = null,
    val userPwError: String? = null,
    val userPwConfirmError: String? = null,

    //회원가입 성공
    val signUpSuccess: Boolean = false,

    val canSubmit: Boolean = false
)

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    //아이디
    fun updateUserId(newId: String) {
        _uiState.update {
            val newState = it.copy(
                userId = newId,
                isIdChecked = false,
                userIdError = validateUserId(newId)
            )

            newState.copy(
                canSubmit = calculateCanSubmit(newState)
            )
        }
    }

    //비밀번호
    fun updateUserPw(newPw: String){
        _uiState.update {
            val newSate = it.copy(
                userPw = newPw,
                userPwError = validateUserPw(newPw),
                userPwConfirmError =
                    if(it.userPwConfirm.isNotEmpty()) {
                        validateUserPwConfirm(newPw, it.userPwConfirm)
                    } else {
                        it.userPwConfirmError
                    }
            )

            newSate.copy(
                canSubmit = calculateCanSubmit(newSate)
            )
        }
    }
    //비밀번호 확인
    fun updateUserPwConfirm(newPwConfirm: String){
        _uiState.update {
            val newState = it.copy(
                userPwConfirm = newPwConfirm,
                userPwConfirmError = validateUserPwConfirm(it.userPw, newPwConfirm)
            )

            newState.copy(
                canSubmit = calculateCanSubmit(newState)
            )
        }
    }

    //아이디 중복 확인
    fun checkIdDuplicate(){
        if(_uiState.value.userIdError != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isIdChecking = true) }

            delay(1000)     //더미

            _uiState.update {
                val newState = it.copy(
                    isIdChecking = false,
                    isIdChecked = true,
                    userIdError = null
                )

                newState.copy(
                    canSubmit = calculateCanSubmit(newState)
                )
            }
        }
    }

    //회원가입 완료
    fun signUp(){
        if (_uiState.value.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            delay(2000)     //더미

            _uiState.update {
                it.copy(
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

    fun validatePassword(pw: String): Boolean {
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
        _uiState.update { it.copy(signUpSuccess = false) }
    }

    fun calculateCanSubmit(state: SignUpUiState): Boolean {
        return state.userId.isNotEmpty() &&
                state.userPw.isNotEmpty() &&
                state.userPwConfirm.isNotEmpty() &&
                state.userIdError == null &&
                state.userPwError == null &&
                state.userPwConfirmError == null &&
                state.isIdChecked &&
                !state.isSubmitting
    }
}