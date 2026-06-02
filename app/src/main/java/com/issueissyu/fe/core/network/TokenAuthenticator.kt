package com.issueissyu.fe.core.network

import com.issueissyu.fe.core.auth.SessionManager
import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.data.remote.api.AuthApi
import com.issueissyu.fe.data.remote.dto.request.auth.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager,
    @Named("auth_plain") private val plainAuthApi: AuthApi,
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.endsWith("/auth/refresh")) {
            return null
        }
        if (response.request.header("Authorization") == null) {
            return null
        }
        if (responseCount(response) >= 2) {
            return null
        }

        return runBlocking {
            mutex.withLock {
                val currentToken = tokenManager.getAccessToken()
                val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

                if (currentToken != null && currentToken != failedToken) {
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                }

                val refreshToken = tokenManager.getRefreshToken() ?: run {
                    sessionManager.expireSession()
                    return@withLock null
                }

                when (runRefreshAccessToken(refreshToken)) {
                    RefreshOutcome.Success -> {
                        val newAccess = tokenManager.getAccessToken() ?: return@withLock null
                        response.request.newBuilder()
                            .header("Authorization", "Bearer $newAccess")
                            .build()
                    }
                    RefreshOutcome.InvalidToken -> {
                        sessionManager.expireSession(SessionManager.DEFAULT_SESSION_EXPIRED_MESSAGE)
                        null
                    }
                    RefreshOutcome.StorageUnavailable -> null
                    RefreshOutcome.NetworkError -> null
                    RefreshOutcome.Failed -> null
                }
            }
        }
    }

    private suspend fun runRefreshAccessToken(refreshToken: String): RefreshOutcome {
        return try {
            val apiResponse = plainAuthApi.refreshToken(RefreshTokenRequest(refreshToken))
            when (apiResponse.code) {
                "REFRESH_200" -> {
                    val result = apiResponse.result ?: return RefreshOutcome.Failed
                    persistRefreshedTokens(result.accessToken, result.refreshToken)
                }
                "REFRESH_401" -> RefreshOutcome.InvalidToken
                else ->
                    if (apiResponse.isSuccess && apiResponse.result != null) {
                        persistRefreshedTokens(
                            apiResponse.result.accessToken,
                            apiResponse.result.refreshToken,
                        )
                    } else {
                        RefreshOutcome.Failed
                    }
            }
        } catch (_: IOException) {
            RefreshOutcome.NetworkError
        } catch (_: Exception) {
            RefreshOutcome.Failed
        }
    }

    private fun persistRefreshedTokens(
        accessToken: String,
        refreshToken: String,
    ): RefreshOutcome {
        if (!tokenManager.saveTokens(
                accessToken = accessToken,
                refreshToken = refreshToken,
            )
        ) {
            sessionManager.expireSession(SessionManager.STORAGE_UNAVAILABLE_MESSAGE)
            return RefreshOutcome.StorageUnavailable
        }
        return RefreshOutcome.Success
    }

    private enum class RefreshOutcome {
        Success,
        InvalidToken,
        StorageUnavailable,
        NetworkError,
        Failed,
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
