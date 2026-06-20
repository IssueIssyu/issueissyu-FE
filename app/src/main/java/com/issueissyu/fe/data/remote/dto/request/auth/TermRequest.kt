package com.issueissyu.fe.data.remote.dto.request.auth

data class TermRequest(
    val serviceTerm: Boolean,
    val privacyTerm: Boolean,
    val locationTerm: Boolean,
    val marketingTerm: Boolean
)