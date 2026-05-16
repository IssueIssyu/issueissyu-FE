package com.issueissyu.fe.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.local.OnboardingSessionStore
import com.issueissyu.fe.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val userId: String = "",
    val userPw: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val onboardingSessionStore: OnboardingSessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<LoginEffect>(extraBufferCapacity = 64)
    val effects: SharedFlow<LoginEffect> = _effects.asSharedFlow()

    fun updateUserId(newId: String) {
        _uiState.update { it.copy(userId = newId, errorMessage = null) }
    }

    fun updatePassword(newPw: String) {
        _uiState.update { it.copy(userPw = newPw, errorMessage = null) }
    }

    fun reportNaverLoginError(message: String) {
        _uiState.update {
            it.copy(isLoading = false, errorMessage = message.ifBlank { "네이버 로그인에 실패했습니다" })
        }
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

            try {
                val result = authRepository.loginLocal(userName = id, password = pw)
                val user = result.getOrNull()
                if (user == null) {
                    val err = result.exceptionOrNull()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err?.message ?: "로그인에 실패했습니다",
                        )
                    }
                    return@launch
                }

                onboardingSessionStore.setSocialType("LOCAL")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        userId = id,
                        userPw = "",
                    )
                }
                // 별도 launch에 emit하면 화면 전환 직후 ViewModel 정리로 emit 코루틴이 취소될 수 있음 → 같은 코루틴에서 emit
                _effects.emit(
                    if (user.isNew) LoginEffect.NavigateToTerms else LoginEffect.NavigateToMain,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "로그인에 실패했습니다",
                    )
                }
            }
        }
    }

    fun naverLogin(accessToken: String, refreshToken: String) {
        viewModelScope.launch {
            if (accessToken.isBlank()) {
                _uiState.update { it.copy(errorMessage = "네이버 액세스 토큰이 없습니다") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = authRepository.loginNaver(
                    accessToken = accessToken,
                    refreshToken = refreshToken.ifBlank { "" },
                )
                val user = result.getOrNull()
                if (user == null) {
                    val err = result.exceptionOrNull()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err?.message ?: "네이버 로그인에 실패했습니다",
                        )
                    }
                    return@launch
                }

                onboardingSessionStore.setSocialType("NAVER")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                    )
                }
                _effects.emit(
                    if (user.isNew) LoginEffect.NavigateToTerms else LoginEffect.NavigateToMain,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "네이버 로그인에 실패했습니다",
                    )
                }
            }
        }
    }
}
