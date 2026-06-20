package com.issueissyu.fe.data.remote.dto.response.location

data class LocationVerificationResponse(
    val address: String? = null,
)

data class LocationResolveResponse(
    val locationId: Long? = null,
    val address: String? = null,
)

data class LocationRegionResponse(
    val region: String? = null,
)

data class LocationRegionListResponse(
    val user: UserRegionSnippetResponse? = null,
    val locations: List<LocationRegionGroupResponse>? = null,
)

data class UserRegionSnippetResponse(
    val userLocationId: Long? = null,
    val userLocation: String? = null,
)

data class LocationRegionGroupResponse(
    val superLocation: String? = null,
    val subLocation: List<LocationRegionItemResponse>? = null,
)

data class LocationRegionItemResponse(
    val locationId: Long? = null,
    val location: String? = null,
)
