package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.request.auth.RefreshTokenRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.auth.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

//토큰 갱신 전용. OkHttp Authenticator에서 HTTP status를 직접 확인
interface PlainAuthApi {
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest,
    ): Response<BaseResponse<TokenResponse>>
}
