package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.R
import com.issueissyu.fe.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val userRepository: UserRepository
) : ViewModel() {
    val userNickname = userRepository.getProfile()
        .map { it.nickname }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    private val _myPins = MutableStateFlow<List<Pin>>(
        listOf(
            Pin("1", "바게트씨", R.drawable.ic_report),
            Pin("2", "돌이곰", R.drawable.ic_fire),
            Pin("3", "버터떡", R.drawable.ic_edit),
            Pin("4", "감자빵", R.drawable.ic_megaphone)
        )
    )
    val myPins = _myPins.asStateFlow()

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