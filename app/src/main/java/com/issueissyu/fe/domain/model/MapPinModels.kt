package com.issueissyu.fe.domain.model

import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinCoordinate

data class MapPinMarker(
    val pinId: String,
    val category: PinCategory,
    val coordinate: PinCoordinate,
    val address: String,
    val locationName: String
)

data class MapPinCluster(
    val clusterId: String,
    val coordinate: PinCoordinate,
    val pinCount: Int,
    val pins: List<MapPinMarker>,
)

data class MapPinQueryResult(
    val pins: List<MapPinMarker>,
    val clusters: List<MapPinCluster>,
)
