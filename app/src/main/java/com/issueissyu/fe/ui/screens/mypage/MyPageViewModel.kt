package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class Pin(
    val id: String,
    val name: String,
    val imageRes: Int,
)

@HiltViewModel
class MyPageViewModel @Inject constructor() : ViewModel() {

    private val _userNickname = MutableStateFlow("뱌삐우소로소1세")
    val userNickname = _userNickname.asStateFlow()

    private val _myPins = MutableStateFlow<List<Pin>>(
        listOf(
            Pin("1", "바게트씨", R.drawable.img_character_bread),
            Pin("2", "돌이곰", R.drawable.img_character_bear),
            Pin("3", "버터떡", R.drawable.img_character_butter),
            Pin("4", "감자빵", R.drawable.img_character_potato)
        )
    )
    val myPins = _myPins.asStateFlow()

    // 유저 정보 - API 호출? 데이터 관리?
    fun loadUserData() {
        viewModelScope.launch {
            // TODO: API 호출
            // val profile = userRepository.getUserProfile()
            // _userNickname.value = profile.nickname
            // _myPins.value = userRepository.getMyPins()
        }
    }

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

    fun resetLohoutState(){
        _logoutState.value = LogoutState.Idle
    }


    sealed class LogoutState {
        object Idle : LogoutState()
        object Loading : LogoutState()
        object Success : LogoutState()
        data class Error(val message: String) : LogoutState()
    }
}