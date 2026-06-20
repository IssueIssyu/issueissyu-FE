package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.request.auth.OnboardingRequest
import com.issueissyu.fe.data.remote.dto.request.auth.LoginLinkRequest
import com.issueissyu.fe.data.remote.dto.request.auth.PhoneSendCodeRequest
import com.issueissyu.fe.data.remote.dto.request.auth.PhoneVerifyRequest
import com.issueissyu.fe.data.remote.dto.request.auth.RefreshTokenRequest
import com.issueissyu.fe.data.remote.dto.request.auth.AuthLocalRequest
import com.issueissyu.fe.data.remote.dto.request.auth.NaverLoginRequest
import com.issueissyu.fe.data.remote.dto.request.auth.TermRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.auth.LoginLinkResultDto
import com.issueissyu.fe.data.remote.dto.response.auth.OnboardingResponse
import com.issueissyu.fe.data.remote.dto.response.auth.LoginResponse
import com.issueissyu.fe.data.remote.dto.response.auth.NicknameAvailabilityResponse
import com.issueissyu.fe.data.remote.dto.response.auth.PhoneAuthEmptyResult
import com.issueissyu.fe.data.remote.dto.response.auth.SignUpResponse
import com.issueissyu.fe.data.remote.dto.response.auth.TermResponse
import com.issueissyu.fe.data.remote.dto.response.auth.TokenResponse
import com.issueissyu.fe.data.remote.dto.response.auth.UsernameAvailabilityDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {

    //회원 탈퇴
    @DELETE("api/auth/signout")
    suspend fun deleteSignOut(): BaseResponse<PhoneAuthEmptyResult?>

    //닉네임 중복 확인
    @GET("api/auth/check/nickname/{nickname}")
    suspend fun checkNickname(
        @Path("nickname") nickname: String,
    ): BaseResponse<NicknameAvailabilityResponse>

    //아이디 중복 확인
    @GET("auth/check/username/{username}")
    suspend fun checkLocalUsernameAvailable(
        @Path("username") userName: String,
    ): BaseResponse<UsernameAvailabilityDto>

    //로컬 회원 가입
    @POST("auth/signup/local")
    suspend fun signUpLocal(
        @Body request: AuthLocalRequest,
    ): BaseResponse<SignUpResponse>

    //토큰 재발급
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest,
    ): BaseResponse<TokenResponse>

    //네이버 앱 로그인
    @POST("auth/login/naver")
    suspend fun loginNaver(
        @Body request: NaverLoginRequest,
    ): BaseResponse<LoginResponse>

    //로컬 로그인
    @POST("auth/login/local")
    suspend fun loginLocal(
        @Body request: AuthLocalRequest,
    ): BaseResponse<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(): BaseResponse<PhoneAuthEmptyResult?>

    //약관 동의
    @POST("api/auth/term")
    suspend fun termAgree(
        @Body request: TermRequest,
    ): BaseResponse<TermResponse?>

    //전화 번호 인증
    @POST("api/auth/phone")
    suspend fun verifyPhone(
        @Body request: PhoneVerifyRequest,
    ): BaseResponse<PhoneAuthEmptyResult?>

    //전화 번호 인증 번호 전송
    @POST("api/auth/phone/send")
    suspend fun sendPhoneVerificationCode(
        @Body request: PhoneSendCodeRequest,
    ): BaseResponse<PhoneAuthEmptyResult?>

    //온보딩 (프로필)
    @POST("api/auth/onboarding")
    suspend fun submitOnboarding(
        @Body request: OnboardingRequest,
    ): BaseResponse<OnboardingResponse?>

    // 로그인 연동
    @POST("api/auth/login/link")
    suspend fun linkLoginAccount(
        @Body request: LoginLinkRequest,
    ): BaseResponse<LoginLinkResultDto>
}
