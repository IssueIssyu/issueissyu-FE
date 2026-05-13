package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.model.DemoPin
import com.issueissyu.fe.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    val userNickname = userRepository.getProfile()
        .map { it.nickname }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val userProfileImageRes = userRepository.getProfileImageRes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.issueissyu.fe.R.drawable.ic_character_default
        )

    val myPins = userRepository.getBookmarkedPins()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    //로그아웃 상태
    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState = _logoutState.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            try {
                _logoutState.value = LogoutState.Loading

                // API 호출
                delay(500)
                _logoutState.value = LogoutState.Success
            } catch (e:Exception) {
                _logoutState.value = LogoutState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun withdraw() {
        viewModelScope.launch {
            //TODO: API 호출
        }
    }

    fun resetLogoutState(){
        _logoutState.value = LogoutState.Idle
    }


    sealed class LogoutState {
        object Idle : LogoutState()
        object Loading : LogoutState()
        object Success : LogoutState()
        data class Error(val message: String) : LogoutState()
    }
}