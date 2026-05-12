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
        if (path.contains("/auth/login", ignoreCase = true)) return false
        if (path.contains("/auth/signup", ignoreCase = true)) return false
        if (path.contains("/auth/refresh", ignoreCase = true)) return false
        return true
    }
}
