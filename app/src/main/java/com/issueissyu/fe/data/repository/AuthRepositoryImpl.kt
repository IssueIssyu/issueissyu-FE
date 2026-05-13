package com.issueissyu.fe.data.repository

import android.util.Log
import com.issueissyu.fe.BuildConfig
import com.issueissyu.fe.core.constants.AuthServerCodes
import com.issueissyu.fe.core.utils.PhoneNumberFormat
import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.data.remote.api.AuthApi
import com.issueissyu.fe.data.remote.dto.request.AuthLocalRequest
import com.issueissyu.fe.data.remote.dto.request.LoginLinkRequest
import com.issueissyu.fe.data.remote.dto.request.PhoneSendCodeRequest
import com.issueissyu.fe.data.remote.dto.request.PhoneVerifyRequest
import com.issueissyu.fe.data.remote.dto.request.RefreshTokenRequest
import com.issueissyu.fe.domain.auth.ExistingPhoneRequiresLinkException
import com.issueissyu.fe.domain.model.AuthUser
import com.issueissyu.fe.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }
    // 회원가입
    override suspend fun signUpLocal(
        userName: String,
        password: String
    ): Result<String> {
        return try {
            val request = AuthLocalRequest(userName, password)
            val response = authApi.signUpLocal(request)

            when (response.code) {
                "LOCAL_SIGNUP_200_1" -> {
                    val signedUpName = response.result!!.userName
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            TAG,
                            "signUpLocal success code=${response.code} message=${response.message} userName=$signedUpName",
                        )
                    }
                    Result.success(signedUpName)
                }

                "LOCAL_SIGNUP_409_1" ->
                    Result.failure(
                        Exception(response.message.ifBlank { "이미 가입된 아이디가 존재하여 회원가입에 실패했습니다." }),
                    )

                "LOCAL_SIGNUP_400_1" ->
                    Result.failure(
                        Exception(response.message.ifBlank { "비밀번호 형식이 올바르지 않습니다." }),
                    )

                else ->
                    Result.failure(
                        Exception(response.message.ifBlank { "회원가입에 실패했습니다." }),
                    )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //로컬 로그인
    override suspend fun loginLocal(
        userName: String,
        password: String
    ): Result<AuthUser> {
        return try {
            val request = AuthLocalRequest(userName, password)
            val response = authApi.loginLocal(request)

            when (response.code) {
                "LOCAL_LOGIN_200_1",
                "LOCAL_LOGIN_200_2",
                -> {
                    val result = response.result
                        ?: return Result.failure(
                            Exception(response.message.ifBlank { "로그인 응답이 올바르지 않습니다." }),
                        )

                    val expectedIsNew = response.code == "LOCAL_LOGIN_200_1"
                    if (result.isNew != expectedIsNew) {
                        return Result.failure(
                            Exception(response.message.ifBlank { "응답이 올바르지 않습니다." }),
                        )
                    }

                    tokenManager.saveTokens(
                        accessToken = result.accessToken,
                        refreshToken = result.refreshToken,
                        isNewUser = result.isNew,
                    )

                    val authUser = AuthUser(
                        uuid = result.user.uuid,
                        userName = result.user.userName,
                        isNew = result.isNew,
                    )

                    if (BuildConfig.DEBUG) {
                        Log.d(
                            TAG,
                            "loginLocal success code=${response.code} message=${response.message} isNew=${authUser.isNew} userName=${authUser.userName}",
                        )
                    }

                    Result.success(authUser)
                }

                "NAVER_LOGIN_401" ->
                    Result.failure(
                        Exception(response.message.ifBlank { "유효하지 않은 값이 존재합니다." }),
                    )

                "COMMON_500" ->
                    Result.failure(
                        Exception(response.message.ifBlank { "InternalServerError" }),
                    )

                else ->
                    Result.failure(
                        Exception(response.message.ifBlank { "로그인에 실패했습니다." }),
                    )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //토큰 재발급
    override suspend fun refreshToken(): Result<Unit> {
        return try {
            val currentRefreshToken = tokenManager.getRefreshToken()
                ?: return Result.failure(Exception("Refresh token not found"))

            val request = RefreshTokenRequest(currentRefreshToken)
            val response = authApi.refreshToken(request)

            if (response.isSuccess && response.result != null) {
                tokenManager.saveTokens(
                    accessToken = response.result.accessToken,
                    refreshToken = response.result.refreshToken
                )
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 로컬 가입 아이디(이메일) 중복 확인
    override suspend fun checkLocalUsernameAvailable(userName: String): Result<Boolean> {
        return try {
            val response = authApi.checkLocalUsernameAvailable(userName)
            when (response.code) {
                "USERNAME_200" -> {
                    val available = response.result?.isAvailableUsername == true
                    if (available) {
                        Result.success(true)
                    } else {
                        Result.failure(
                            Exception(response.message.ifBlank { "아이디를 사용할 수 없습니다." }),
                        )
                    }
                }

                "USERNAME_409" ->
                    Result.failure(
                        Exception(response.message.ifBlank { "이미 사용 중인 아이디입니다." }),
                    )

                "USERNAME_400" ->
                    Result.failure(
                        Exception(response.message.ifBlank { "아이디는 100자 이내이어야 합니다." }),
                    )

                else ->
                    Result.failure(
                        Exception(response.message.ifBlank { "아이디 확인에 실패했습니다." }),
                    )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //닉네임 중복 확인
    override suspend fun checkNicknameAvailable(nickname: String): Result<Boolean> {
        return try {
            val response = authApi.checkNickname(nickname)
            if (response.isSuccess && response.result != null) {
                // true = 사용 가능으로 가정 (백엔드가 반대면 여기만 반전)
                Result.success(response.result)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //전화번호 인증번호 전송
    override suspend fun sendPhoneVerificationCode(phoneDigits: String): Result<Unit> {
        return try {
            val response = authApi.sendPhoneVerificationCode(
                PhoneSendCodeRequest(phone = PhoneNumberFormat.toDashedKorean(phoneDigits)),
            )
            when (response.code) {
                AuthServerCodes.PHONE_201 -> Result.failure(ExistingPhoneRequiresLinkException())
                AuthServerCodes.PHONE_200 -> Result.success(Unit)
                AuthServerCodes.PHONE_400_2 -> Result.failure(Exception(response.message))
                else -> if (response.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //전화번호 인증
    override suspend fun verifyPhoneCode(phoneDigits: String, code: String): Result<Unit> {
        return try {
            val response = authApi.verifyPhone(
                PhoneVerifyRequest(
                    phone = PhoneNumberFormat.toDashedKorean(phoneDigits),
                    code = code,
                ),
            )
            when (response.code) {
                AuthServerCodes.PHONE_201 -> Result.failure(ExistingPhoneRequiresLinkException())
                AuthServerCodes.PHONE_200 -> Result.success(Unit)
                AuthServerCodes.PHONE_400_2 -> Result.failure(Exception(response.message))
                else -> if (response.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //로그인 연동
    override suspend fun linkLogin(
        phoneDigits: String,
        socialType: String,
    ): Result<Unit> {
        return try {
            val dashed = PhoneNumberFormat.toDashedKorean(phoneDigits)
            val response = authApi.linkLoginAccount(
                LoginLinkRequest(phone = dashed, socialType = socialType),
            )
            if (response.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
