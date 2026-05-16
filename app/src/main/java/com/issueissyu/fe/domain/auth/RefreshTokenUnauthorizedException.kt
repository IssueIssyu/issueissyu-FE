package com.issueissyu.fe.domain.auth

/** [POST auth/refresh] 응답 code가 REFRESH_401 등으로 갱신 불가일 때 */
class RefreshTokenUnauthorizedException(
    message: String,
) : Exception(message)
