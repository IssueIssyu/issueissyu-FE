package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.AuthUser

interface AuthRepository {

    //회원 가입
    suspend fun signUpLocal(
        userName: String,
        password: String,
    ): Result<String>

    //로그인
    suspend fun loginLocal(
        userName: String,
        password: String,
    ): Result<AuthUser>

    //토큰 재발급
    suspend fun refreshToken(): Result<Unit>

    //닉네임 중복 확인
    suspend fun checkNicknameAvailable(nickname: String): Result<Boolean>

    /** 로컬 가입용 아이디(이메일) 사용 가능 여부. `true` = 사용 가능으로 매핑 (백엔드 반대면 Impl만 수정). */
    suspend fun checkLocalUsernameAvailable(userName: String): Result<Boolean>

    //전화 번호 인증 번호 전송
    suspend fun sendPhoneVerificationCode(phoneDigits: String): Result<Unit>

    //전화 번호 인증
    suspend fun verifyPhoneCode(phoneDigits: String, code: String): Result<Unit>

    //로그인 연동
    suspend fun linkLogin(
        phoneDigits: String,
        socialType: String = "LOCAL",
    ): Result<Unit>
}
