package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.CreatePinRequest
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.Pin
import com.issueissyu.fe.domain.model.UpdatePinRequest
import com.issueissyu.fe.domain.model.MapPinMarker

interface PinRepository {
    suspend fun getPins(): List<Pin>
    suspend fun getPinById(pinId: String): Pin?
    suspend fun getIssuePins(): List<Pin>
    suspend fun getCommunityPins(): List<Pin>
    suspend fun getMyPins(): List<Pin>
    suspend fun createPin(request: CreatePinRequest): Pin
    suspend fun updatePin(pinId: String, request: UpdatePinRequest): Pin

    suspend fun getMapPinsInBounds(bounds: MapBounds): List<MapPinMarker>
}