package com.issueissyu.fe.core.network

import okhttp3.Request

fun Request.requiresAccessToken(): Boolean {
    val path = url.encodedPath
    if (path.endsWith("/auth/signup/local")) return false
    if (path.contains("/auth/check/username/")) return false
    if (path.endsWith("/auth/login/local")) return false
    if (path.endsWith("/auth/login/naver")) return false
    if (path.endsWith("/auth/refresh")) return false
    return true
}

fun Request.accessTokenOrNull(): String? =
    header("Authorization")?.removePrefix("Bearer ")?.takeIf { it.isNotBlank() }
