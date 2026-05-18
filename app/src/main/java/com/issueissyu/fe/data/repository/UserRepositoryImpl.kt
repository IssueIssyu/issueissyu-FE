package com.issueissyu.fe.data.repository

import com.issueissyu.fe.domain.model.User
import com.issueissyu.fe.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor() : UserRepository {
    //TODO: API 연동 시 교체 / 더미 데이터
    private val _user = MutableStateFlow(User(nickname = "뱌삐우소로소1세"))

    override fun getProfile(): Flow<User> = _user

    override suspend fun updateNickname(nickname: String) {
        //TODO: API 호출
        _user.value = _user.value.copy(nickname = nickname)
    }

    override suspend fun updateProfileImage(imageUrl: String) {
        _user.value = _user.value.copy(profileImageUrl = imageUrl)
    }

    override suspend fun checkNicknameDuplicate(nickname: String): Boolean {
        //TODO: API 호출
        //더미 -> 항상 true
        return true
    }
}