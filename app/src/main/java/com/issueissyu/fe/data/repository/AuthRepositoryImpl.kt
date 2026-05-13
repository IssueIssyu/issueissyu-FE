package com.issueissyu.fe.data.repository

import android.util.Log
import com.issueissyu.fe.BuildConfig
import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.data.remote.api.AuthApi
import com.issueissyu.fe.data.remote.dto.request.AuthLocalRequest
import com.issueissyu.fe.data.remote.dto.request.LoginLinkRequest
import com.issueissyu.fe.data.remote.dto.request.auth.OnboardingRequest
import com.issueissyu.fe.data.remote.dto.request.PhoneSendCodeRequest
import com.issueissyu.fe.data.remote.dto.request.PhoneVerifyRequest
import com.issueissyu.fe.data.remote.dto.request.RefreshTokenRequest
import com.issueissyu.fe.data.remote.dto.request.auth.TermRequest
import com.issueissyu.fe.data.remote.dto.response.auth.OnboardingResponse
import com.issueissyu.fe.domain.auth.ExistingPhoneRequiresLinkException
import com.issueissyu.fe.domain.model.AuthUser
import com.issueissyu.fe.domain.model.OnboardingProfile
import com.issueissyu.fe.domain.model.TermsAgreementResult
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

    private fun safeMessage(rawMessage: String?, fallback: String): String {
        return rawMessage?.takeIf { it.isNotBlank() } ?: fallback
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
                        Exception(
                            safeMessage(
                                response.message as String?,
                                "이미 가입된 아이디가 존재하여 회원가입에 실패했습니다.",
                            ),
                        ),
                    )

                "LOCAL_SIGNUP_400_1" ->
                    Result.failure(
                        Exception(
                            safeMessage(
                                response.message as String?,
                                "비밀번호 형식이 올바르지 않습니다.",
                            ),
                        ),
                    )

                else ->
                    Result.failure(
                        Exception(safeMessage(response.message as String?, "회원가입에 실패했습니다.")),
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
                            Exception(
                                safeMessage(response.message as String?, "로그인 응답이 올바르지 않습니다."),
                            ),
                        )

                    val expectedIsNew = response.code == "LOCAL_LOGIN_200_1"
                    if (result.isNew != expectedIsNew) {
                        return Result.failure(
                            Exception(safeMessage(response.message as String?, "응답이 올바르지 않습니다.")),
                        )
                    }

                    val tempUuidRaw: String? = result.user.tempUuid
                    val uuidRaw: String? = result.user.uuid
                    val userNameRaw: String? = result.user.userName

                    val resolvedTempUuid = tempUuidRaw?.takeIf { it.isNotBlank() }
                        ?: return Result.failure(
                            Exception(
                                safeMessage(
                                    response.message as String?,
                                    "로그인 응답(tempUuid)이 올바르지 않습니다.",
                                ),
                            ),
                        )
                    val resolvedUuid = uuidRaw?.takeIf { it.isNotBlank() } ?: resolvedTempUuid
                    val resolvedUserName = userNameRaw?.takeIf { it.isNotBlank() } ?: userName

                    tokenManager.saveTokens(
                        accessToken = result.accessToken,
                        refreshToken = result.refreshToken,
                        isNewUser = result.isNew,
                        tempUuid = resolvedTempUuid,
                    )

                    val authUser = AuthUser(
                        uuid = resolvedUuid,
                        userName = resolvedUserName,
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
                        Exception(safeMessage(response.message as String?, "유효하지 않은 값이 존재합니다.")),
                    )

                "COMMON_500" ->
                    Result.failure(
                        Exception(safeMessage(response.message as String?, "InternalServerError")),
                    )

                else ->
                    Result.failure(
                        Exception(safeMessage(response.message as String?, "로그인에 실패했습니다.")),
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

            when (response.code) {
                "REFRESH_200" -> {
                    val result = response.result
                        ?: return Result.failure(
                            Exception(
                                response.message.ifBlank { "토큰 재발급 응답이 올바르지 않습니다." },
                            ),
                        )
                    tokenManager.saveTokens(
                        accessToken = result.accessToken,
                        refreshToken = result.refreshToken,
                    )
                    Result.success(Unit)
                }

                "REFRESH_401" ->
                    Result.failure(
                        Exception(response.message.ifBlank { "유효하지 않은 토큰입니다." }),
                    )

                else ->
                    if (response.isSuccess && response.result != null) {
                        tokenManager.saveTokens(
                            accessToken = response.result.accessToken,
                            refreshToken = response.result.refreshToken,
                        )
                        Result.success(Unit)
                    } else {
                        Result.failure(
                            Exception(response.message.ifBlank { "토큰 재발급에 실패했습니다." }),
                        )
                    }
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

    override suspend fun submitTermsAgreement(
        serviceTerm: Boolean,
        privacyTerm: Boolean,
        locationTerm: Boolean,
        marketingTerm: Boolean,
    ): Result<TermsAgreementResult> {
        return try {
            val response = authApi.termAgree(
                TermRequest(
                    serviceTerm = serviceTerm,
                    privacyTerm = privacyTerm,
                    locationTerm = locationTerm,
                    marketingTerm = marketingTerm,
                ),
            )
            when (response.code) {
                "TERM_200" -> {
                    val r = response.result
                        ?: return Result.failure(
                            Exception(response.message.ifBlank { "약관 동의 응답이 올바르지 않습니다." }),
                        )
                    Result.success(
                        TermsAgreementResult(
                            eventAlarmActive = r.eventAlarmActive,
                            likeAlarmActive = r.likeAlarmActive,
                            hotAlarmActive = r.hotAlarmActive,
                            storeAlarmActive = r.storeAlarmActive,
                        ),
                    )
                }
                "TERM_400" ->
                    Result.failure(
                        Exception(response.message.ifBlank { "필수 약관에 동의해주세요." }),
                    )
                else ->
                    if (response.isSuccess && response.result != null) {
                        val r = response.result
                        Result.success(
                            TermsAgreementResult(
                                eventAlarmActive = r.eventAlarmActive,
                                likeAlarmActive = r.likeAlarmActive,
                                hotAlarmActive = r.hotAlarmActive,
                                storeAlarmActive = r.storeAlarmActive,
                            ),
                        )
                    } else {
                        Result.failure(
                            Exception(response.message.ifBlank { "약관 동의에 실패했습니다." }),
                        )
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //닉네임 중복 확인
    override suspend fun checkNicknameAvailable(nickname: String): Result<Boolean> {
        return try {
            val response = authApi.checkNickname(nickname)
            when (response.code) {
                "NICKNAME_200" -> {
                    val available = response.result?.isAvailableNickname == true
                    if (available) {
                        Result.success(true)
                    } else {
                        Result.failure(
                            Exception(response.message.ifBlank { "사용할 수 없는 닉네임입니다." }),
                        )
                    }
                }

                "NICKNAME_409" ->
                    Result.failure(
                        Exception(response.message.ifBlank { "이미 사용 중인 닉네임입니다." }),
                    )

                "NICKNAME_400" ->
                    Result.failure(
                        Exception(response.message.ifBlank { "닉네임 형식이 올바르지 않습니다." }),
                    )

                else ->
                    if (response.isSuccess && response.result?.isAvailableNickname == true) {
                        Result.success(true)
                    } else {
                        Result.failure(
                            Exception(response.message.ifBlank { "닉네임 확인에 실패했습니다." }),
                        )
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //전화번호 인증번호 전송 (api/auth/phone/send — 응답 코드 PHONE_SEND_*)
    override suspend fun sendPhoneVerificationCode(phoneDigits: String): Result<Unit> {
        return try {
            val response = authApi.sendPhoneVerificationCode(
                PhoneSendCodeRequest(phone = toDashedPhone(phoneDigits)),
            )
            when (response.code) {
                "PHONE_SEND_200" -> Result.success(Unit)
                "PHONE_SEND_400_1" -> Result.failure(Exception(response.message))
                "PHONE_SEND_400_2" -> Result.failure(Exception(response.message))
                else ->
                    if (response.isSuccess) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(response.message))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //전화번호 인증 (api/auth/phone — 응답 코드 PHONE_*)
    override suspend fun verifyPhoneCode(
        phoneDigits: String,
        code: String,
        isAvailableNickname: Boolean,
    ): Result<Unit> {
        return try {
            val response = authApi.verifyPhone(
                PhoneVerifyRequest(
                    phone = toDashedPhone(phoneDigits),
                    code = code,
                    isAvailableNickname = isAvailableNickname,
                ),
            )
            when (response.code) {
                "PHONE_200" -> Result.success(Unit)
                "PHONE_201" -> Result.failure(ExistingPhoneRequiresLinkException())
                "PHONE_400_2",
                -> Result.failure(Exception(response.message))
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

    //로그인 연동 (api/auth/login/link — 응답 코드 LOGIN_LINK_*)
    override suspend fun linkLogin(
        phoneDigits: String,
        socialType: String,
    ): Result<Unit> {
        return try {
            val response = authApi.linkLoginAccount(
                LoginLinkRequest(
                    phone = toDashedPhone(phoneDigits),
                    socialType = socialType,
                ),
            )
            when (response.code) {
                "LOGIN_LINK_200" -> Result.success(Unit)
                "LOGIN_LINK_400" ->
                    Result.failure(
                        Exception(response.message.ifBlank { "로그인 연동에 실패했습니다." }),
                    )
                else ->
                    if (response.isSuccess) {
                        Result.success(Unit)
                    } else {
                        Result.failure(
                            Exception(response.message.ifBlank { "로그인 연동에 실패했습니다." }),
                        )
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //온보딩 (응답 코드 ONBOAREDING_* — 스펙 철자 그대로)
    override suspend fun onboarding(
        nickname: String,
        email: String,
        phone: String,
    ): Result<OnboardingProfile> {
        return try {
            val response = authApi.submitOnboarding(
                OnboardingRequest(
                    nickname = nickname,
                    email = email,
                    phone = toDashedPhone(phone),
                ),
            )
            when (response.code) {
                "ONBOAREDING_200" -> {
                    val r = response.result
                        ?: return Result.failure(
                            Exception(
                                response.message.ifBlank { "온보딩 응답이 올바르지 않습니다." },
                            ),
                        )
                    Result.success(r.toDomain())
                }
                "ONBOAREDING_400" ->
                    Result.failure(
                        Exception(response.message.ifBlank { "온보딩에 실패했습니다." }),
                    )
                else ->
                    if (response.isSuccess && response.result != null) {
                        Result.success(response.result!!.toDomain())
                    } else {
                        Result.failure(
                            Exception(response.message.ifBlank { "온보딩에 실패했습니다." }),
                        )
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun OnboardingResponse.toDomain() = OnboardingProfile(
        uuid = uuid,
        socialType = socialType,
        userCustomCollectionId = userCustomCollectionId,
        customCollectionId = customCollectionId,
        customCollectionName = customCollectionName,
        customCollectionUrl = customCollectionUrl,
    )

    private fun toDashedPhone(rawPhone: String): String {
        val digits = rawPhone.filter { it.isDigit() }
        return if (digits.length == 11) {
            "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7, 11)}"
        } else {
            digits
        }
    }
}
