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

data class LoginUiState(
    val userId: String = "",
    val userPw: String = "",

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(): ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    //id & pw 업데이트
    fun updateUserId(newId: String) {
        _uiState.update {it.copy(userId = newId, errorMessage = null)}
    }

    fun updatePassword(newPw: String){
        _uiState.update {it.copy(userPw = newPw, errorMessage = null)}
    }

    //로컬 로그인
    fun localLogin(){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            delay(1000)

            if(_uiState.value.userId == "test123" && _uiState.value.userPw == "0000"){    //더미
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "아이디 또는 비밀번호가 틀렸습니다"
                    )
                }
            }
        }
    }

    fun naverLogin(){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(1000)
            _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            //SDK 호출 -> 토큰 주고 -> 응답 받기
        }
    }
}