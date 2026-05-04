package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.R
import dagger.hilt.android.lifecycle.HiltViewModel
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
            Pin("1", "바게트씨", R.drawable.img_character_butter),
            Pin("2", "움공씨", R.drawable.ic_megaphone),
            Pin("3", "버터떡", R.drawable.ic_moving),
            Pin("4", "으쓱씨", R.drawable.ic_report)
        )
    )
    val myPins = _myPins.asStateFlow()

    // 나중에 API 연동
    fun loadUserData() {
        viewModelScope.launch {
            // TODO: API 호출
            // val profile = userRepository.getUserProfile()
            // _userNickname.value = profile.nickname
            // _myPins.value = userRepository.getMyPins()
        }
    }

    fun logout() {
        // TODO: 로그아웃 로직
    }

    fun withdraw() {
        // TODO: 회원탈퇴 로직
    }
}