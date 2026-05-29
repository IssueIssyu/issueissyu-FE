package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.R
import com.issueissyu.fe.domain.repository.AuthRepository
import com.issueissyu.fe.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Pin(
    val id: String,
    val name: String,
    val imageRes: Int,
)

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    val userNickname = userRepository.getProfile()
        .map { it.nickname }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    private val _myPins = MutableStateFlow(
        listOf(
            Pin("1", "바게트씨", R.drawable.ic_report),
            Pin("2", "돌이곰", R.drawable.ic_fire),
            Pin("3", "버터떡", R.drawable.ic_edit),
            Pin("4", "감자빵", R.drawable.ic_megaphone)
        )
    )
    val myPins = _myPins.asStateFlow()

    private val _authActionState = MutableStateFlow<AuthActionState>(AuthActionState.Idle)
    val authActionState = _authActionState.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            _authActionState.value = AuthActionState.Loading
            authRepository.logout()
                .onSuccess { _authActionState.value = AuthActionState.Success }
                .onFailure { error ->
                    _authActionState.value = AuthActionState.Error(
                        error.message ?: LOGOUT_ERROR_MESSAGE,
                    )
                }
        }
    }

    fun withdraw() {
        viewModelScope.launch {
            _authActionState.value = AuthActionState.Loading
            authRepository.withdraw()
                .onSuccess { _authActionState.value = AuthActionState.Success }
                .onFailure { error ->
                    _authActionState.value = AuthActionState.Error(
                        error.message ?: WITHDRAW_ERROR_MESSAGE,
                    )
                }
        }
    }

    fun resetAuthActionState() {
        _authActionState.value = AuthActionState.Idle
    }

    sealed class AuthActionState {
        data object Idle : AuthActionState()
        data object Loading : AuthActionState()
        data object Success : AuthActionState()
        data class Error(val message: String) : AuthActionState()
    }

    companion object {
        private const val LOGOUT_ERROR_MESSAGE = "로그아웃에 실패했습니다."
        private const val WITHDRAW_ERROR_MESSAGE = "회원탈퇴에 실패했습니다."
    }
}
