package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileChangeViewModel @Inject constructor() : ViewModel() {

    // 현재 닉네임 (MyPageViewModel과 동일)
    private val _currentNickname = MutableStateFlow("뱌삐우소로소1세")
    val currentNickname = _currentNickname.asStateFlow()

    // 입력 중인 닉네임
    private val _inputNickname = MutableStateFlow("")
    val inputNickname = _inputNickname.asStateFlow()

    // 중복 확인 상태
    private val _isNicknameAvailable = MutableStateFlow<Boolean?>(null)
    val isNicknameAvailable = _isNicknameAvailable.asStateFlow()

    private val _isCheckingNickname = MutableStateFlow(false)
    val isCheckingNickname = _isCheckingNickname.asStateFlow()

    // 완료 버튼 활성화 여부
    private val _isCompleteEnabled = MutableStateFlow(false)
    val isCompleteEnabled = _isCompleteEnabled.asStateFlow()

    // 중복 확인 버튼 활성화 여부
    private val _isCheckButtonEnabled = MutableStateFlow(false)
    val isCheckButtonEnabled = _isCheckButtonEnabled.asStateFlow()

    fun onNicknameChange(nickname: String) {
        _inputNickname.value = nickname
        _isNicknameAvailable.value = null // 입력 변경되면 중복 확인 초기화
        updateCompleteButtonState()
    }

    fun checkNicknameDuplicate() {
        if (_inputNickname.value.isBlank()) return

        viewModelScope.launch {
            _isCheckingNickname.value = true

            try {
                // TODO: API 호출


                delay(500)
                _isNicknameAvailable.value = true

            } catch (e: Exception) {
                _isNicknameAvailable.value = false
            } finally {
                _isCheckingNickname.value = false
                updateCompleteButtonState()
            }
        }
    }

    private fun updateCompleteButtonState() {
        // 중복 확인 버튼: 입력값이 있고, 기존 닉네임과 다를 때
        _isCheckButtonEnabled.value =
            _inputNickname.value.isNotBlank() &&
                    _inputNickname.value != _currentNickname.value

        // 완료 버튼: 입력값이 있고, 중복확인 완료, 기존과 다를 때
        _isCompleteEnabled.value =
            _inputNickname.value.isNotBlank() &&
                    _isNicknameAvailable.value == true &&
                    _inputNickname.value != _currentNickname.value
    }

    fun updateProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // TODO: API 호출

                delay(300)
                _currentNickname.value = _inputNickname.value
                onSuccess()

            } catch (e: Exception) {
            }
        }
    }
}