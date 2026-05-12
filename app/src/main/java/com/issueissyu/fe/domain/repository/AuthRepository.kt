package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.AuthUser

interface AuthRepository {

    // 회원가입
    // 반환 -> userName만
    suspend fun signUpLocal(
        userName: String,
        password: String
    ): Result<String>

    // 로그인
    // 반환 -> 전체
    suspend fun loginLocal(
        userName: String,
        password: String
    ): Result<AuthUser>

    // 토큰 재발급
    suspend fun refreshToken(): Result<Unit>
}