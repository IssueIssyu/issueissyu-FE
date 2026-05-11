package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.request.AuthLocalRequest
import com.issueissyu.fe.data.remote.dto.request.RefreshTokenRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.LoginResponse
import com.issueissyu.fe.data.remote.dto.response.SignUpResponse
import com.issueissyu.fe.data.remote.dto.response.TokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    // 회원가입
    @POST("/auth/signup/local")
    suspend fun signUpLocal(
        @Body request: AuthLocalRequest
    ): BaseResponse<SignUpResponse>

    //로그인
    @POST("/auth/login/local")
    suspend fun loginLocal(
        @Body request: AuthLocalRequest
    ): BaseResponse<LoginResponse>

    //토큰 재발급
    @POST("/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): BaseResponse<TokenResponse>
}