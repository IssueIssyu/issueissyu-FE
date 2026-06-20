package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.MapNotice
import com.issueissyu.fe.domain.model.MapPinQueryResult
import com.issueissyu.fe.domain.model.PatchNotePage
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinCategory

interface MapRepository {
    suspend fun getMapPinsInBounds(
        bounds: MapBounds,
        zoomLevel: Int,
        category: PinCategory? = null,
    ): Result<MapPinQueryResult>

    suspend fun getPinCard(pinId: String): Result<Pin>

    suspend fun getPatchNotes(
        locationId: Long? = null,
        size: Int? = null,
        cursor: String? = null,
    ): Result<PatchNotePage>

    suspend fun getMapNotices(): Result<List<MapNotice>>
}
