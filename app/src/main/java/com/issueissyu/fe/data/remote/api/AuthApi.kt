package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.request.AuthLocalRequest
import com.issueissyu.fe.data.remote.dto.request.LoginLinkRequest
import com.issueissyu.fe.data.remote.dto.request.PhoneSendCodeRequest
import com.issueissyu.fe.data.remote.dto.request.PhoneVerifyRequest
import com.issueissyu.fe.data.remote.dto.request.RefreshTokenRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.LoginLinkResultDto
import com.issueissyu.fe.data.remote.dto.response.LoginResponse
import com.issueissyu.fe.data.remote.dto.response.SignUpResponse
import com.issueissyu.fe.data.remote.dto.response.TokenResponse
import com.issueissyu.fe.data.remote.dto.response.UsernameAvailabilityDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {

    //회원 탈퇴

    //닉네임 중복 확인
    @GET("api/auth/check/nickname/{nickname}")
    suspend fun checkNickname(
        @Path("nickname") nickname: String,
    ): BaseResponse<Boolean>

    //아이디 중복 확인
    @GET("api/auth/check/username/{username}")
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
    //로컬 로그인
    @POST("auth/login/local")
    suspend fun loginLocal(
        @Body request: AuthLocalRequest,
    ): BaseResponse<LoginResponse>

    //약관 동의

    //전화 번호 인증
    @POST("api/auth/phone")
    suspend fun verifyPhone(
        @Body request: PhoneVerifyRequest,
    ): BaseResponse<Map<String, Any>?>

    //전화 번호 인증 번호 전송
    @POST("api/auth/phone/send")
    suspend fun sendPhoneVerificationCode(
        @Body request: PhoneSendCodeRequest,
    ): BaseResponse<Map<String, Any>?>

    //온보딩

    //로그 아웃
    @POST("api/auth/login/link")
    suspend fun linkLoginAccount(
        @Body request: LoginLinkRequest,
    ): BaseResponse<LoginLinkResultDto>
}
