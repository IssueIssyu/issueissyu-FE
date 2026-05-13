package com.issueissyu.fe.data.repository

import com.issueissyu.fe.R
import com.issueissyu.fe.data.model.DemoPin
import com.issueissyu.fe.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor() : UserRepository {
    //TODO: API 연동 시 교체 / 더미 데이터
    private val _user = MutableStateFlow(User(nickname = "뱌삐우소로소1세"))
    private val _profileImageRes = MutableStateFlow(R.drawable.ic_character_default)
    private val _bookmarkedPins = MutableStateFlow<List<DemoPin>>(emptyList())

    override fun getProfile(): Flow<User> = _user
    override fun getProfileImageRes(): Flow<Int> = _profileImageRes.asStateFlow()
    override fun getBookmarkedPins(): Flow<List<DemoPin>> = _bookmarkedPins.asStateFlow()

    override suspend fun updateNickname(nickname: String) {
        //TODO: API 호출
        _user.value = _user.value.copy(nickname = nickname)
    }

    override suspend fun updateProfileImage(imageUrl: String) {
        _user.value = _user.value.copy(profileImageUrl = imageUrl)
    }

    override suspend fun updateProfileImageRes(resId: Int) {
        _profileImageRes.value = resId
    }

    override suspend fun toggleBookmark(pin: DemoPin) {
        _bookmarkedPins.update { current ->
            if (current.any { it.id == pin.id }) {
                current.filter { it.id != pin.id }
            } else {
                current + pin
            }
        }
    }

    override suspend fun checkNicknameDuplicate(nickname: String): Boolean {
        //TODO: API 호출
        //더미 -> 항상 true
        return true
    }
}