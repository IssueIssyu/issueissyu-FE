package com.issueissyu.fe.domain.auth

/** [POST api/auth/login/link] — 이미 연동된 계정 등으로 연동을 진행할 수 없을 때 */
class AccountAlreadyLinkedException(
    message: String,
) : Exception(message)
