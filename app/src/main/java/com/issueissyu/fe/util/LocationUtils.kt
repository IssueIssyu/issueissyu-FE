package com.issueissyu.fe.util

import android.location.Location
import com.issueissyu.fe.data.model.PinCoordinate
import com.naver.maps.geometry.LatLng

fun PinCoordinate.distanceTo(other: PinCoordinate): Double {
    val results = FloatArray(1)

    Location.distanceBetween(
        latitude,
        longitude,
        other.latitude,
        other.longitude,
        results
    )

    return results[0].toDouble()
}

fun PinCoordinate.distanceTo(other: LatLng): Double {
    val results = FloatArray(1)

    Location.distanceBetween(
        latitude,
        longitude,
        other.latitude,
        other.longitude,
        results
    )

    return results[0].toDouble()
}

fun LatLng.distanceTo(other: LatLng): Double {
    val results = FloatArray(1)

    Location.distanceBetween(
        latitude,
        longitude,
        other.latitude,
        other.longitude,
        results
    )

    return results[0].toDouble()
}

fun LatLng.distanceTo(other: PinCoordinate): Double {
    val results = FloatArray(1)

    Location.distanceBetween(
        latitude,
        longitude,
        other.latitude,
        other.longitude,
        results
    )

    return results[0].toDouble()
}
