package com.issueissyu.fe.core.network

import com.issueissyu.fe.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!request.shouldAttachAccessToken()) {
            return chain.proceed(request)
        }
        val accessToken = tokenManager.getAccessToken() ?: return chain.proceed(request)
        val authenticated = request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        return chain.proceed(authenticated)
    }

    private fun Request.shouldAttachAccessToken(): Boolean {
        val path = url.encodedPath
        if (path.endsWith("/auth/signup/local")) return false
        // 회원가입 전 아이디(username) 중복 확인은 비로그인 호출
        if (path.contains("/api/auth/check/username/")) return false
        if (path.endsWith("/auth/login/local")) return false
        if (path.endsWith("/auth/login/naver")) return false
        if (path.endsWith("/auth/refresh")) return false
        return true
    }
}
