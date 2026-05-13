package com.issueissyu.fe.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val userId: String = "",
    val userPw: String = "",

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val navigateWithIsNew: Boolean? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateUserId(newId: String) {
        _uiState.update { it.copy(userId = newId, errorMessage = null) }
    }

    fun updatePassword(newPw: String) {
        _uiState.update { it.copy(userPw = newPw, errorMessage = null) }
    }

    fun consumeLoginNavigation() {
        _uiState.update { it.copy(navigateWithIsNew = null) }
    }

    fun localLogin() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val id = _uiState.value.userId.trim()
            val pw = _uiState.value.userPw

            if (id.isEmpty() || pw.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "아이디와 비밀번호를 입력해주세요",
                    )
                }
                return@launch
            }

            val result = authRepository.loginLocal(
                userName = id,
                password = pw,
            )

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            userId = id,
                            userPw = "",
                            navigateWithIsNew = user.isNew,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "로그인에 실패했습니다",
                        )
                    }
                },
            )
        }
    }

    //TODO: 네이버 SDK 연동 후 AuthRepository 및 동일한 navigate 규칙 적용
    fun naverLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(1000)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    navigateWithIsNew = false,
                )
            }
        }
    }
}
