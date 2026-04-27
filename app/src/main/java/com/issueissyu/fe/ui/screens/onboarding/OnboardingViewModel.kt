package com.issueissyu.fe.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    //Repository
) : ViewModel() {
    //1. 상태
    //1.1. 로그인 상태
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    //1.2. 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    //1.3. 회원 가입 정보
    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname

    //1.4. 위치 정보
    private val _userLatitude = MutableStateFlow(0.0)
    val userLatitude: StateFlow<Double> = _userLatitude

    private val _userLongitude = MutableStateFlow(0.0)
    val userLongitude: StateFlow<Double> = _userLongitude

    private val _userAddress = MutableStateFlow("")
    val userAddress: StateFlow<String> = _userAddress

    //2. Splash
    //2.1. 로그인 상태 확인 (토큰 확인)
    suspend fun checkLoginStatus(): Boolean {
        delay(500)
        //API 연결
        return false
    }

    //3. 로그인
    //3.1. 카카오 로그인
    fun kakaoLogin() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                //카카오 SDK 호출 나중에
                delay(1000) //더미
                _isLoggedIn.value = true //무조건 성공
            } catch (e: Exception) {
                //로그인 실패
                _isLoggedIn.value = false
            } finally {
                //로딩 실패
                _isLoading.value = false
            }
        }
    }

    //4. 회원 가입
    //4.1. 닉네임
    fun updateNickname(newNickname: String) {
        _nickname.value = newNickname
    }

    //중복 확인
    suspend fun checkNicknameDuplicate(nickname: String): Boolean {
        //API 호출
        return false
    }

    //5. 동네 인증
    //5.1. 위치 인증
    //최초 등록 시 프론트 -> 좌표만 넘겨줌?
    fun verifyLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _userLatitude.value = latitude
                _userLongitude.value = longitude


                //API 호출
                //POST
            } catch (e: Exception) {
                //에러
            } finally {
                _isLoading.value = false
            }
        }
    }

    //6. 회원 가입 완료
    fun completeSignup(){
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val userInfo = mapOf(
                    "nickname" to nickname.value,
                    "latitude" to userLatitude.value,
                    "longitude" to userLongitude.value,
                    "address" to "마포구"  //더미
                )

                _isLoggedIn.value = true
            } catch (e: Exception) {
                //에러
            } finally {
                _isLoading.value = false
            }
        }
    }

}