package com.issueissyu.fe.core.network

import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.data.remote.api.AuthApi
import com.issueissyu.fe.data.remote.dto.request.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    @Named("auth_plain") private val plainAuthApi: AuthApi,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.contains("/auth/refresh", ignoreCase = true)) {
            return null
        }
        if (response.request.header("Authorization") == null) {
            return null
        }
        if (responseCount(response) >= 2) {
            return null
        }

        val refreshToken = tokenManager.getRefreshToken() ?: run {
            tokenManager.clearTokens()
            return null
        }

        val refreshed = runBlocking {
            runCatching {
                val body = RefreshTokenRequest(refreshToken)
                val apiResponse = plainAuthApi.refreshToken(body)
                if (apiResponse.isSuccess && apiResponse.result != null) {
                    tokenManager.saveTokens(
                        accessToken = apiResponse.result.accessToken,
                        refreshToken = apiResponse.result.refreshToken,
                    )
                    true
                } else {
                    false
                }
            }.getOrElse { false }
        }

        if (!refreshed) {
            tokenManager.clearTokens()
            return null
        }

        val newAccess = tokenManager.getAccessToken() ?: return null
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .build()
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
