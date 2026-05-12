package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.data.remote.api.AuthApi
import com.issueissyu.fe.data.remote.dto.request.AuthLocalRequest
import com.issueissyu.fe.data.remote.dto.request.RefreshTokenRequest
import com.issueissyu.fe.domain.model.AuthUser
import com.issueissyu.fe.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {
    override suspend fun signUpLocal(
        userName: String,
        password: String
    ): Result<String> {
        return try {
            val request = AuthLocalRequest(userName, password)
            val response = authApi.signUpLocal(request)

            if (response.isSuccess && response.result != null) {
                Result.success(response.result.userName)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginLocal(
        userName: String,
        password: String
    ): Result<AuthUser> {
        return try {
            val request = AuthLocalRequest(userName, password)
            val response = authApi.loginLocal(request)

            if (response.isSuccess && response.result != null) {
                tokenManager.saveTokens(
                    accessToken = response.result.accessToken,
                    refreshToken = response.result.refreshToken,
                    isNewUser = response.result.isNew,
                )

                val authUser = AuthUser(
                    uuid = response.result.user.uuid,
                    userName = response.result.user.userName,
                    isNew = response.result.isNew
                )

                Result.success(authUser)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(): Result<Unit> {
        return try {
            val currentRefreshToken = tokenManager.getRefreshToken()
            ?: return Result.failure(Exception("Refresh token not found"))

            val request = RefreshTokenRequest(currentRefreshToken)
            val response = authApi.refreshToken(request)

            if (response.isSuccess && response.result != null) {
                tokenManager.saveTokens(
                    accessToken = response.result.accessToken,
                    refreshToken = response.result.refreshToken
                )
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}