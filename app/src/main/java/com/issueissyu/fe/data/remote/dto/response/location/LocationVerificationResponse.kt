package com.issueissyu.fe.data.remote.dto.response.location

data class LocationVerificationResponse(
    val address: String? = null,
)

data class LocationResolveResponse(
    val locationId: Long? = null,
    val address: String? = null,
)
