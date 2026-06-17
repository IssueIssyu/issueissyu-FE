package com.issueissyu.fe.data.repository

import android.util.Log
import com.issueissyu.fe.BuildConfig
import com.issueissyu.fe.core.auth.SessionManager
import com.issueissyu.fe.core.network.ApiErrorMapper
import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.data.remote.api.AuthApi
import com.issueissyu.fe.data.remote.dto.request.auth.LoginLinkRequest
import com.issueissyu.fe.data.remote.dto.request.auth.OnboardingRequest
import com.issueissyu.fe.data.remote.dto.request.auth.PhoneSendCodeRequest
import com.issueissyu.fe.data.remote.dto.request.auth.PhoneVerifyRequest
import com.issueissyu.fe.data.remote.dto.request.auth.RefreshTokenRequest
import com.issueissyu.fe.data.remote.dto.request.auth.AuthLocalRequest
import com.issueissyu.fe.data.remote.dto.request.auth.NaverLoginRequest
import com.issueissyu.fe.data.remote.dto.request.auth.TermRequest
import com.issueissyu.fe.data.remote.dto.response.auth.OnboardingResponse
import com.issueissyu.fe.domain.auth.ExistingPhoneRequiresLinkException
import com.issueissyu.fe.domain.auth.RefreshTokenUnauthorizedException
import com.issueissyu.fe.domain.model.auth.AuthUser
import com.issueissyu.fe.domain.model.auth.OnboardingProfile
import com.issueissyu.fe.domain.model.notification.AlarmToggleState
import com.issueissyu.fe.domain.repository.AuthRepository
import com.issueissyu.fe.domain.repository.UserCollectionsStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager,
    private val userCollectionsStore: UserCollectionsStore,
    private val apiErrorMapper: ApiErrorMapper,
) : AuthRepository {

    private fun clearUserSession() {
        sessionManager.clearSession()
        userCollectionsStore.clear()
    }

    companion object {
        private const val TAG = "AuthRepository"
    }

    private fun safeMessage(rawMessage: String?, fallback: String): String {
        return rawMessage?.takeIf { it.isNotBlank() } ?: fallback
    }

    private suspend fun <T> failureFrom(e: Exception, fallback: String): Result<T> =
        Result.failure(apiErrorMapper.toException(e, fallback))

    private fun persistAuthTokens(
        accessToken: String,
        refreshToken: String,
        isNewUser: Boolean? = null,
        loginSocialType: String? = null,
        userUuid: String? = null,
        userName: String? = null,
    ): Result<Unit> {
        if (!tokenManager.saveTokens(
                accessToken = accessToken,
                refreshToken = refreshToken,
                isNewUser = isNewUser,
                loginSocialType = loginSocialType,
                userUuid = userUuid,
                userName = userName,
            )
        ) {
            return Result.failure(Exception(SessionManager.STORAGE_UNAVAILABLE_MESSAGE))
        }
        sessionManager.markAuthenticated()
        return Result.success(Unit)
    }
    // 회원가입
    override suspend fun signUpLocal(
        userName: String,
        password: String
    ): Result<Unit> {
        return try {
            val request = AuthLocalRequest(userName, password)
            val response = authApi.signUpLocal(request)

            when (response.code) {
                "LOCAL_SIGNUP_200_1" -> {
                    val result = response.result
                        ?: return Result.failure(
                            Exception(
                                safeMessage(
                                    response.message as String?,
                                    "회원가입 응답이 올바르지 않습니다.",
                                ),
                            ),
                        )
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            TAG,
                            "signUpLocal success code=${response.code} message=${response.message} userName=${result.userName}",
                        )
                    }
                    Result.success(Unit)
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
            failureFrom(e, "회원가입에 실패했습니다.")
        }
    }

    //네이버 앱 로그인
    override suspend fun loginNaver(
        accessToken: String,
        refreshToken: String
    ): Result<AuthUser> {
        return try {
            val request = NaverLoginRequest(
                accessToken = accessToken,
                refreshToken = refreshToken,
            )
            val response = authApi.loginNaver(request)

            when (response.code) {
                "NAVER_LOGIN_200_1",
                "NAVER_LOGIN_200_2",
                -> {
                    val result = response.result
                        ?: return Result.failure(
                            Exception(
                                safeMessage(response.message as String?, "네이버 로그인 응답이 올바르지 않습니다."),
                            ),
                        )

                    val tempUuidRaw: String? = result.user.tempUuid
                    val uuidRaw: String? = result.user.uuid
                    val userNameRaw: String? = result.user.userName

                    val resolvedTempUuid = tempUuidRaw?.takeIf { it.isNotBlank() }
                    if (result.isNew && resolvedTempUuid == null) {
                        return Result.failure(
                            Exception(
                                safeMessage(
                                    response.message as String?,
                                    "신규 회원 응답(tempUuid)이 올바르지 않습니다.",
                                ),
                            ),
                        )
                    }
                    val resolvedUuid = uuidRaw?.takeIf { it.isNotBlank() }
                        ?: resolvedTempUuid
                        ?: return Result.failure(
                            Exception(
                                safeMessage(
                                    response.message as String?,
                                    "로그인 응답(사용자 식별자)이 올바르지 않습니다.",
                                ),
                            ),
                        )
                    val resolvedUserName = userNameRaw?.takeIf { it.isNotBlank() } ?: "NAVER_USER"

                    persistAuthTokens(
                        accessToken = result.accessToken,
                        refreshToken = result.refreshToken,
                        isNewUser = result.isNew,
                        loginSocialType = result.socialType.takeIf { it.isNotBlank() },
                        userUuid = resolvedUuid,
                        userName = resolvedUserName,
                    ).map {
                        AuthUser(
                            uuid = resolvedUuid,
                            userName = resolvedUserName,
                            isNew = result.isNew,
                        )
                    }
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
                        Exception(safeMessage(response.message as String?, "네이버 로그인에 실패했습니다.")),
                    )
            }
        } catch (e: Exception) {
            failureFrom(e, "네이버 로그인에 실패했습니다.")
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
                    if (result.isNew && resolvedTempUuid == null) {
                        return Result.failure(
                            Exception(
                                safeMessage(
                                    response.message as String?,
                                    "신규 회원 응답(tempUuid)이 올바르지 않습니다.",
                                ),
                            ),
                        )
                    }
                    val resolvedUuid = uuidRaw?.takeIf { it.isNotBlank() }
                        ?: resolvedTempUuid
                        ?: return Result.failure(
                            Exception(
                                safeMessage(
                                    response.message as String?,
                                    "로그인 응답(사용자 식별자)이 올바르지 않습니다.",
                                ),
                            ),
                        )
                    val resolvedUserName = userNameRaw?.takeIf { it.isNotBlank() } ?: userName

                    persistAuthTokens(
                        accessToken = result.accessToken,
                        refreshToken = result.refreshToken,
                        isNewUser = result.isNew,
                        loginSocialType = result.socialType.takeIf { it.isNotBlank() },
                        userUuid = resolvedUuid,
                        userName = resolvedUserName,
                    ).map {
                        AuthUser(
                            uuid = resolvedUuid,
                            userName = resolvedUserName,
                            isNew = result.isNew,
                        ).also { authUser ->
                            if (BuildConfig.DEBUG) {
                                Log.d(
                                    TAG,
                                    "loginLocal success code=${response.code} message=${response.message} isNew=${authUser.isNew} userName=${authUser.userName}",
                                )
                            }
                        }
                    }
                }

                "LOCAL_LOGIN_400_1" ->
                    Result.failure(
                        Exception(safeMessage(response.message as String?, "유효하지 않은 값이 존재합니다.")),
                    )

                "LOCAL_LOGIN_401" ->
                    Result.failure(
                        Exception(
                            safeMessage(response.message as String?, "아이디 또는 비밀번호가 올바르지 않습니다."),
                        ),
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
            failureFrom(e, "로그인에 실패했습니다.")
        }
    }

    //토큰 재발급
    override suspend fun refreshToken(): Result<Unit> {
        return try {
            val currentRefreshToken = tokenManager.getRefreshToken()
                ?: return Result.failure(Exception("로그인 정보를 확인한 뒤 다시 시도해주세요."))

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
                    persistAuthTokens(
                        accessToken = result.accessToken,
                        refreshToken = result.refreshToken,
                    )
                }

                "REFRESH_401" ->
                    Result.failure(
                        RefreshTokenUnauthorizedException(
                            response.message.ifBlank { "유효하지 않은 토큰입니다." },
                        ),
                    )

                else ->
                    if (response.isSuccess && response.result != null) {
                        persistAuthTokens(
                            accessToken = response.result.accessToken,
                            refreshToken = response.result.refreshToken,
                        )
                    } else {
                        val msg = response.message.ifBlank { "토큰 재발급에 실패했습니다." }
                        if (response.code == "REFRESH_401") {
                            Result.failure(RefreshTokenUnauthorizedException(msg))
                        } else {
                            Result.failure(Exception(msg))
                        }
                    }
            }
        } catch (e: Exception) {
            failureFrom(e, "토큰 재발급에 실패했습니다.")
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            sessionManager.prepareLogout()
            val response = authApi.logout()
            when (response.code) {
                "LOGOUT_200" -> {
                    clearUserSession()
                    Result.success(Unit)
                }
                "LOGOUT_401" -> {
                    clearUserSession()
                    Result.success(Unit)
                }
                else ->
                    if (response.isSuccess) {
                        clearUserSession()
                        Result.success(Unit)
                    } else {
                        Result.failure(
                            Exception(
                                safeMessage(response.message, "로그아웃에 실패했습니다."),
                            ),
                        )
                    }
            }
        } catch (e: Exception) {
            Result.failure(
                Exception(safeMessage(e.message, "로그아웃에 실패했습니다.")),
            )
        }
    }

    override suspend fun withdraw(): Result<Unit> {
        return try {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "withdraw: DELETE api/auth/signout")
            }
            val response = authApi.deleteSignOut()
            if (response.isSuccess) {
                clearUserSession()
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(safeMessage(response.message, "회원탈퇴에 실패했습니다.")),
                )
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "withdraw failed", e)
            }
            failureFrom(e, "회원탈퇴에 실패했습니다.")
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
            failureFrom(e, "아이디 확인에 실패했습니다.")
        }
    }

    override suspend fun submitTermsAgreement(
        serviceTerm: Boolean,
        privacyTerm: Boolean,
        locationTerm: Boolean,
        marketingTerm: Boolean,
    ): Result<AlarmToggleState> {
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
                        AlarmToggleState(
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
                            AlarmToggleState(
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
            failureFrom(e, "약관 동의에 실패했습니다.")
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
            failureFrom(e, "닉네임 확인에 실패했습니다.")
        }
    }

    //전화번호 인증번호 전송
    override suspend fun sendPhoneVerificationCode(phoneDigits: String): Result<Unit> {
        return try {
            val response = authApi.sendPhoneVerificationCode(
                PhoneSendCodeRequest(phone = toDashedPhone(phoneDigits)),
            )
            when (response.code) {
                "PHONE_SEND_200" -> Result.success(Unit)
                "PHONE_SEND_400_1" ->
                    Result.failure(
                        Exception(
                            safeMessage(response.message, "전화번호 형식이 올바르지 않습니다."),
                        ),
                    )
                "PHONE_SEND_400_2" ->
                    Result.failure(
                        Exception(
                            safeMessage(response.message, "인증번호 전송에 실패했습니다."),
                        ),
                    )
                else ->
                    if (response.isSuccess) {
                        Result.success(Unit)
                    } else {
                        Result.failure(
                            Exception(
                                safeMessage(response.message, "인증번호 전송에 실패했습니다."),
                            ),
                        )
                    }
            }
        } catch (e: Exception) {
            failureFrom(e, "인증번호 전송에 실패했습니다.")
        }
    }

    //전화번호 인증
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
                "PHONE_400_2" ->
                    Result.failure(
                        Exception(
                            safeMessage(response.message, "인증번호가 올바르지 않습니다."),
                        ),
                    )
                else -> if (response.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(
                        Exception(
                            safeMessage(response.message, "전화번호 인증에 실패했습니다."),
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            failureFrom(e, "전화번호 인증에 실패했습니다.")
        }
    }

    //로그인 연동
    override suspend fun linkLogin(
        phoneDigits: String,
        socialType: String,
    ): Result<Unit> {
        return try {
            val socialForRequest = tokenManager.getLoginSocialType()?.takeIf { it.isNotBlank() }
                ?: socialType
            val response = authApi.linkLoginAccount(
                LoginLinkRequest(
                    phone = toDashedPhone(phoneDigits),
                    socialType = socialForRequest,
                ),
            )

            fun persistLinkedSession(): Result<Unit> {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "로그인 연동 응답이 올바르지 않습니다." },
                        ),
                    )
                val resolvedUuid = result.uuid?.takeIf { it.isNotBlank() }
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "연동 응답(uuid)이 올바르지 않습니다." },
                        ),
                    )
                tokenManager.saveCurrentUser(userUuid = resolvedUuid)
                sessionManager.markAuthenticated()
                // 연동 직후 스플래시가 온보딩/메인으로 튀지 않도록 신규 회원 플래그만 해제
                tokenManager.clearNewUserFlag()
                return Result.success(Unit)
            }

            val failMessage = response.message.ifBlank { "로그인 연동에 실패했습니다." }
            when (response.code) {
                "LOGIN_LINK_200" -> persistLinkedSession()
                "LOGIN_LINK_400_1" ->
                    Result.failure(
                        Exception(
                            safeMessage(
                                response.message,
                                "해당 전화번호로 가입된 기존 계정이 없습니다.",
                            ),
                        ),
                    )
                "LOGIN_LINK_400_2" ->
                    Result.failure(
                        Exception(
                            safeMessage(
                                response.message,
                                "연동 대상이 현재 계정과 동일합니다.",
                            ),
                        ),
                    )
                "LOGIN_LINK_400_3" ->
                    Result.failure(
                        Exception(
                            safeMessage(
                                response.message,
                                "해당 소셜 타입은 이미 연동되어 있습니다.",
                            ),
                        ),
                    )
                "LOGIN_LINK_400" -> Result.failure(Exception(failMessage))
                else ->
                    if (response.isSuccess) {
                        persistLinkedSession()
                    } else {
                        Result.failure(Exception(failMessage))
                    }
            }
        } catch (e: Exception) {
            failureFrom(e, "로그인 연동에 실패했습니다.")
        }
    }

    //온보딩
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
                    tokenManager.saveCurrentUser(
                        userUuid = r.uuid,
                        userName = nickname,
                    )
                    sessionManager.markAuthenticated()
                    tokenManager.clearNewUserFlag()
                    Result.success(r.toDomain())
                }
                "ONBOAREDING_400" ->
                    Result.failure(
                        Exception(response.message.ifBlank { "온보딩에 실패했습니다." }),
                    )
                else -> {
                    val result = response.result
                    if (response.isSuccess && result != null) {
                        tokenManager.saveCurrentUser(
                            userUuid = result.uuid,
                            userName = nickname,
                        )
                        sessionManager.markAuthenticated()
                        tokenManager.clearNewUserFlag()
                        Result.success(result.toDomain())
                    } else {
                        Result.failure(
                            Exception(response.message.ifBlank { "온보딩에 실패했습니다." }),
                        )
                    }
                }
            }
        } catch (e: Exception) {
            failureFrom(e, "온보딩에 실패했습니다.")
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
        return when (digits.length) {
            11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7, 11)}"
            10 -> "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6, 10)}"
            else -> digits
        }
    }
}
