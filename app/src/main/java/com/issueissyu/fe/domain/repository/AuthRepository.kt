package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.AuthUser
import com.issueissyu.fe.domain.model.OnboardingProfile
import com.issueissyu.fe.domain.model.TermsAgreementResult

interface AuthRepository {

    /** 로컬 회원가입 — 성공 시 토큰은 저장하지 않음(로그인 화면으로만 이동). */
    suspend fun signUpLocal(
        userName: String,
        password: String,
    ): Result<Unit>

    //네이버 로그인
    suspend fun loginNaver(
        accessToken: String,
        refreshToken: String
    ): Result<AuthUser>

    // 로컬 로그인
    suspend fun loginLocal(
        userName: String,
        password: String,
    ): Result<AuthUser>

    //토큰 재발급
    suspend fun refreshToken(): Result<Unit>

    /** 서버 세션 종료 후 로컬 토큰 삭제(실패해도 로컬은 비움). */
    suspend fun logout(): Result<Unit>

    //아이디 중복 확인
    suspend fun checkLocalUsernameAvailable(userName: String): Result<Boolean>

    //약관 동의
    suspend fun submitTermsAgreement(
        serviceTerm: Boolean,
        privacyTerm: Boolean,
        locationTerm: Boolean,
        marketingTerm: Boolean,
    ): Result<TermsAgreementResult>

    //닉네임 중복 확인
    suspend fun checkNicknameAvailable(nickname: String): Result<Boolean>

    //전화 번호 인증 번호 전송
    suspend fun sendPhoneVerificationCode(phoneDigits: String): Result<Unit>

    //전화 번호 인증
    suspend fun verifyPhoneCode(
        phoneDigits: String,
        code: String,
        isAvailableNickname: Boolean,
    ): Result<Unit>

    //로그인 연동
    suspend fun linkLogin(
        phoneDigits: String,
        socialType: String,
    ): Result<Unit>

    //온보딩 (프로필 제출)
    suspend fun onboarding(
        nickname: String,
        email: String,
        phone: String,
    ): Result<OnboardingProfile>
}
