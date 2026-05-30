package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.repository.AuthRepository
import com.issueissyu.fe.domain.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Pin(
    val id: String,
    val name: String,
    val imageUrl: String,
)

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _userNickname = MutableStateFlow("")
    val userNickname = _userNickname.asStateFlow()

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl = _profileImageUrl.asStateFlow()

    private val _myPins = MutableStateFlow<List<Pin>>(emptyList())
    val myPins = _myPins.asStateFlow()

    private val _authActionState = MutableStateFlow<AuthActionState>(AuthActionState.Idle)
    val authActionState = _authActionState.asStateFlow()

    init {
        loadCollections()
    }

    fun loadCollections() {
        viewModelScope.launch {
            collectionRepository.getCollections(checkUnlock = false)
                .onSuccess { summary ->
                    _userNickname.value = summary.nickname
                    _profileImageUrl.value = summary.profileImageUrl
                    _myPins.value = summary.bookmarkedCollections.map { item ->
                        Pin(
                            id = item.collectionId.toString(),
                            name = item.name,
                            imageUrl = item.imageUrl,
                        )
                    }
                }
        }
    }

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
