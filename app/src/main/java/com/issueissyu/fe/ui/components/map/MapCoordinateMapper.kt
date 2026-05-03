package com.issueissyu.fe.ui.components.map

import com.issueissyu.fe.data.model.PinCoordinate
import com.naver.maps.geometry.LatLng

fun PinCoordinate.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun LatLng.toPinCoordinate(): PinCoordinate {
    return PinCoordinate(latitude, longitude)
}