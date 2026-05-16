package com.issueissyu.fe.core.network

import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.data.remote.api.AuthApi
import com.issueissyu.fe.data.remote.dto.request.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

//
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    @Named("auth_plain") private val plainAuthApi: AuthApi,
) : Authenticator {

    //동시 토큰 갱신 -> 꼬임 방지, mutex 사용
    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // API 응답 실패
        if (response.request.url.encodedPath.endsWith("/auth/refresh")) {
            return null
        }
        // 토큰 x 요청
        if (response.request.header("Authorization") == null) {
            return null
        }
        // 무한 루프 방지
        if (responseCount(response) >= 2) {
            return null
        }

        return runBlocking {
            mutex.withLock {
                val currentToken = tokenManager.getAccessToken()
                val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

                if (currentToken != null && currentToken != failedToken) {
                    // 이미 새 토큰 있음 (방지) -> 새 토큰으로 재시도
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                }

                // refreshToken X -> 방어로직
                val refreshToken = tokenManager.getRefreshToken() ?: run {
                    tokenManager.clearTokens()
                    return@withLock null
                }

                val refreshed = runCatching {
                    val body = RefreshTokenRequest(refreshToken)
                    val apiResponse = plainAuthApi.refreshToken(body)
                    when (apiResponse.code) {
                        "REFRESH_200" -> {
                            val result = apiResponse.result ?: return@runCatching false
                            tokenManager.saveTokens(
                                accessToken = result.accessToken,
                                refreshToken = result.refreshToken,
                            )
                            true
                        }

                        // refreshToken 만료
                        "REFRESH_401" -> false

                        else ->
                            if (apiResponse.isSuccess && apiResponse.result != null) {
                                tokenManager.saveTokens(
                                    accessToken = apiResponse.result.accessToken,
                                    refreshToken = apiResponse.result.refreshToken,
                                )
                                true
                            } else {
                                false
                            }
                    }
                }.getOrElse { false }

                if (!refreshed) {
                    tokenManager.clearTokens()
                    return@withLock null
                }

                val newAccess = tokenManager.getAccessToken() ?: return@withLock null
                return@withLock response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccess")
                    .build()
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
