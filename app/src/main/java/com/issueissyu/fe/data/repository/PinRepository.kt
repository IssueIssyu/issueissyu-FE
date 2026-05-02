package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.model.CreatePinRequest
import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.data.model.UpdatePinRequest

interface PinRepository {
    suspend fun getPins(): List<Pin>
    suspend fun getPinById(pinId: String): Pin?
    suspend fun getIssuePins(): List<Pin>
    suspend fun getCommunityPins(): List<Pin>
    suspend fun getMyPins(): List<Pin>
    suspend fun createPin(request: CreatePinRequest): Pin
    suspend fun updatePin(pinId: String, request: UpdatePinRequest): Pin
}