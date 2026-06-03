package com.issueissyu.fe.data.repository

import com.issueissyu.fe.core.network.ApiErrorMapper
import com.issueissyu.fe.data.remote.api.AuthApi
import com.issueissyu.fe.data.remote.api.MyPageApi
import com.issueissyu.fe.data.remote.dto.mypage.toMyIssuePage
import com.issueissyu.fe.data.remote.dto.request.mypage.ChangeNickNameRequest
import com.issueissyu.fe.domain.model.User
import com.issueissyu.fe.domain.model.mypage.MyIssuePage
import com.issueissyu.fe.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val myPageApi: MyPageApi,
    private val authApi: AuthApi,
    private val apiErrorMapper: ApiErrorMapper,
) : UserRepository {

    private fun <T> failureFrom(e: Exception, fallback: String): Result<T> =
        Result.failure(apiErrorMapper.toException(e, fallback))
    //TODO: API 연동 시 교체 / 더미 데이터
    private val _user = MutableStateFlow(User(nickname = "뱌삐우소로소1세"))

    override fun getProfile(): Flow<User> = _user

    override suspend fun updateNickname(nickname: String) {
        try {
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
        } catch (e: Exception) {
            throw apiErrorMapper.toException(e, "닉네임 변경에 실패했습니다.")
        }
    }

    //동네 변경
    override suspend fun updateUserAddress(lat: Double, lng: Double): Result<String> {
        return try {
            val response = myPageApi.changeAddress(lat = lat, lng = lng)
            when (response.code) {
                "LOCATION_200_4" -> {
                    Result.success(response.result?.address?.takeIf { it.isNotBlank() }.orEmpty())
                }

                "LOCATION_400_1" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "잘못된 위치 요청입니다." },
                        ),
                    )

                "LOCATION_404_1" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "주소 결과를 찾을 수 없습니다." },
                        ),
                    )

                "LOCATION_404_3" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "법정동 코드 결과를 찾을 수 없습니다." },
                        ),
                    )

                "LOCATION_502_2" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "주소 API 호출에 실패했습니다." },
                        ),
                    )

                "USER_404_1" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "존재하지 않는 회원입니다." },
                        ),
                    )

                "COMMON_500" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "서버 에러" },
                        ),
                    )

                "LOCATION_400_2" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "동네 변경은 한 달에 1회 가능합니다." },
                        ),
                    )

                else ->
                    if (response.isSuccess) {
                        Result.success(response.result?.address?.takeIf { it.isNotBlank() }.orEmpty())
                    } else {
                        Result.failure(
                            Exception(response.message.ifBlank { "동네 변경에 실패했습니다." }),
                        )
                    }
            }
        } catch (e: Exception) {
            failureFrom(e, "동네 변경에 실패했습니다.")
        }
    }

    override suspend fun updateProfileImage(imageUrl: String) {
        _user.value = _user.value.copy(profileImageUrl = imageUrl)
    }

    override suspend fun checkNicknameDuplicate(nickname: String): Boolean {
        try {
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
        } catch (e: Exception) {
            throw apiErrorMapper.toException(e, "닉네임 중복 확인에 실패했습니다.")
        }
    }

    override suspend fun getMyIssues(size: Int, cursor: String?): Result<MyIssuePage> {
        return try {
            val response = myPageApi.getMyIssue(size = size, cursor = cursor)
            if (response.isSuccess) {
                val body = response.result
                    ?: return Result.failure(
                        Exception(response.message.ifBlank { "내 이슈 응답이 올바르지 않습니다." }),
                    )
                Result.success(body.toMyIssuePage())
            } else {
                when (response.code) {
                    "USER_PIN_400_1" -> Result.failure(
                        Exception(response.message.ifBlank { "조회 불가능한 사이즈 입니다." }),
                    )
                    "USER_PIN_400_2" -> Result.failure(
                        Exception(response.message.ifBlank { "조회 불가능한 cursor 입니다." }),
                    )
                    else -> Result.failure(
                        Exception(response.message.ifBlank { "내 이슈 조회에 실패했습니다." }),
                    )
                }
            }
        } catch (e: Exception) {
            failureFrom(e, "내 이슈 조회에 실패했습니다.")
        }
    }
}