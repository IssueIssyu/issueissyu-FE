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
) {
    val canSubmit: Boolean
        get() = userId.isNotBlank() &&
            isIdChecked &&
            !isIdChecking &&
            userIdError == null &&
            userPw.isNotBlank() &&
            SignUpUiState.validateUserPwStatic(userPw) == null &&
            userPwError == null &&
            userPwConfirm.isNotBlank() &&
            userPw == userPwConfirm &&
            userPwConfirmError == null &&
            !isSubmitting

    companion object {
        /** 비밀번호: 8~20자, 영문(대소문 무관)·숫자·특수문자 각 1자 이상 */
        fun validateUserPwStatic(pw: String): String? {
            if (pw.isEmpty()) return null
            if (pw.length < 8 || pw.length > 20) return "8~20자로 입력해주세요"
            val hasEng = pw.any { it in 'a'..'z' || it in 'A'..'Z' }
            val hasDigit = pw.any { it.isDigit() }
            val hasSpecial = pw.any { ch ->
                ch !in 'a'..'z' && ch !in 'A'..'Z' && ch !in '0'..'9'
            }
            return if (hasEng && hasDigit && hasSpecial) {
                null
            } else {
                "영문·숫자·특수문자를 각각 1자 이상 포함해주세요"
            }
        }
    }
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private fun updateState(update: SignUpUiState.() -> SignUpUiState) {
        _uiState.update { it.update() }
    }

    fun updateUserId(newId: String) {
        updateState {
            copy(
                userId = newId,
                isIdChecked = false,
                userIdError = null,
            )
        }
    }

    fun updateUserPw(newPw: String) {
        updateState {
            copy(
                userPw = newPw,
                userPwError = SignUpUiState.validateUserPwStatic(newPw),
                userPwConfirmError =
                    if (userPwConfirm.isNotEmpty()) {
                        validateUserPwConfirm(newPw, userPwConfirm)
                    } else {
                        userPwConfirmError
                    },
            )
        }
    }

    fun updateUserPwConfirm(newPwConfirm: String) {
        updateState {
            copy(
                userPwConfirm = newPwConfirm,
                userPwConfirmError = validateUserPwConfirm(userPw, newPwConfirm),
            )
        }
    }

    /**
     * [AuthRepository.checkLocalUsernameAvailable] — 성공 시 [isIdChecked] true, 아이디 수정 시 [updateUserId]에서 false로 초기화.
     */
    fun checkIdDuplicate() {
        val id = _uiState.value.userId.trim()
        if (id.isEmpty()) {
            updateState { copy(userIdError = "아이디를 입력해주세요") }
            return
        }

        viewModelScope.launch {
            updateState { copy(isIdChecking = true, userIdError = null) }

            authRepository.checkLocalUsernameAvailable(id).fold(
                onSuccess = { available ->
                    updateState {
                        copy(
                            isIdChecking = false,
                            isIdChecked = available,
                            userIdError = if (available) {
                                null
                            } else {
                                "이미 사용 중인 아이디입니다"
                            },
                        )
                    }
                },
                onFailure = { e ->
                    updateState {
                        copy(
                            isIdChecking = false,
                            isIdChecked = false,
                            userIdError = e.message ?: "중복 확인에 실패했습니다",
                        )
                    }
                },
            )
        }
    }

    fun signUp() {
        if (_uiState.value.isSubmitting || !_uiState.value.canSubmit) return

        viewModelScope.launch {
            updateState {
                copy(
                    isSubmitting = true,
                    userIdError = null,
                    userPwError = null,
                    userPwConfirmError = null,
                )
            }

            val id = _uiState.value.userId.trim()
            val pw = _uiState.value.userPw

            val signUpResult = authRepository.signUpLocal(id, pw)
            signUpResult.fold(
                onSuccess = {
                    val loginResult = authRepository.loginLocal(id, pw)
                    loginResult.fold(
                        onSuccess = {
                            updateState {
                                copy(
                                    isSubmitting = false,
                                    signUpSuccess = true,
                                )
                            }
                        },
                        onFailure = { e ->
                            updateState {
                                copy(
                                    isSubmitting = false,
                                    userPwError = e.message ?: "자동 로그인에 실패했습니다",
                                )
                            }
                        },
                    )
                },
                onFailure = { e ->
                    val msg = e.message ?: "회원가입에 실패했습니다"
                    val treatAsPw = msg.contains("비밀번호") || msg.contains("형식") || msg.contains("유효")
                    updateState {
                        copy(
                            isSubmitting = false,
                            userIdError = if (treatAsPw) null else msg,
                            userPwError = if (treatAsPw) msg else null,
                        )
                    }
                },
            )
        }
    }

    private fun validateUserPwConfirm(pw: String, pwConfirm: String): String? {
        return when {
            pwConfirm.isEmpty() -> null
            pwConfirm != pw -> "비밀번호가 일치하지 않습니다"
            else -> null
        }
    }

    fun consumeSignUpSuccess() {
        updateState { copy(signUpSuccess = false) }
    }
}
