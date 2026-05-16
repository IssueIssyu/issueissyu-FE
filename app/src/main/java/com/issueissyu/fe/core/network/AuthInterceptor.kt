package com.issueissyu.fe.core.network

import com.issueissyu.fe.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

//모든 API 요청에 자동으로 토큰을 첨부하는 인터셉터입니다!
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {

    // intercept 함수 -> 모든 요청 가로챔
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()   // 원본 요청

        // 토큰 필요한지 확인
        if (!request.shouldAttachAccessToken()) {
            return chain.proceed(request)
        }
        // 1. 없으면 무시
        val accessToken = tokenManager.getAccessToken() ?: return chain.proceed(request)

        // 2. 필요하면 헤더에 토큰 붙임
        val authenticated = request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        return chain.proceed(authenticated)
    }

    // 토큰 필요 x API
    private fun Request.shouldAttachAccessToken(): Boolean {
        val path = url.encodedPath
        if (path.endsWith("/auth/signup/local")) return false
        if (path.contains("/api/auth/check/username/")) return false
        if (path.endsWith("/auth/login/local")) return false
        if (path.endsWith("/auth/login/naver")) return false
        if (path.endsWith("/auth/refresh")) return false
        return true
    }
}
