package com.issueissyu.fe.domain.model

data class MapPinMarker(
    val pinId: String,
    val category: PinCategory,
    val coordinate: PinCoordinate,
    val address: String,
    val locationName: String
)