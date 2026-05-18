package com.issueissyu.fe.ui.components.map

import com.issueissyu.fe.domain.model.PinCoordinate
import com.naver.maps.geometry.LatLng

fun PinCoordinate.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

@Suppress("unused")
fun LatLng.toPinCoordinate(): PinCoordinate {
    return PinCoordinate(latitude, longitude)
}