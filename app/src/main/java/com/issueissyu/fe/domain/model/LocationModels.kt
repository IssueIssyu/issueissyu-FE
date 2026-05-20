package com.issueissyu.fe.domain.model

data class ResolvedLocation(
    val locationId: Long,
    val address: String,
)

data class LocationRegions(
    val userRegion: UserRegion?,
    val groups: List<LocationRegionGroup>,
)

data class UserRegion(
    val locationId: Long,
    val location: String,
)

data class LocationRegionGroup(
    val superLocation: String,
    val subLocations: List<LocationRegionItem>,
)

data class LocationRegionItem(
    val locationId: Long,
    val location: String,
)
