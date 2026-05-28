package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.AuthApi
import com.issueissyu.fe.data.remote.api.MyPageApi
import com.issueissyu.fe.data.remote.dto.request.mypage.ChangeNickNameRequest
import com.issueissyu.fe.domain.model.User
import com.issueissyu.fe.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val myPageApi: MyPageApi,
    private val authApi: AuthApi,
) : UserRepository {
    //TODO: API 연동 시 교체 / 더미 데이터
    private val _user = MutableStateFlow(User(nickname = "뱌삐우소로소1세"))

    override fun getProfile(): Flow<User> = _user

    override suspend fun updateNickname(nickname: String) {
        val response = myPageApi.changeNickName(
            request = ChangeNickNameRequest(nickname = nickname),
        )

        when (response.code) {
            "USER_NICKNAME_200" -> {
                _user.value = _user.value.copy(nickname = nickname)
            }

            else -> {
                throw IllegalStateException(
                    response.message.ifBlank { "닉네임 변경에 실패했습니다." },
                )
            }
        }
    }

    override suspend fun updateProfileImage(imageUrl: String) {
        _user.value = _user.value.copy(profileImageUrl = imageUrl)
    }

    override suspend fun checkNicknameDuplicate(nickname: String): Boolean {
        val response = authApi.checkNickname(nickname)

        return when (response.code) {
            "NICKNAME_200" -> response.result?.isAvailableNickname == true
            "NICKNAME_409" -> false
            else -> {
                if (response.isSuccess) {
                    response.result?.isAvailableNickname == true
                } else {
                    throw IllegalStateException(
                        response.message.ifBlank { "닉네임 중복 확인에 실패했습니다." },
                    )
                }
            }
        }
    }
}