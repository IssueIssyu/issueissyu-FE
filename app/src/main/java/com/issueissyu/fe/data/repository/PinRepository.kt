package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.model.CreatePinRequest
import com.issueissyu.fe.data.model.MapBounds
import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.data.model.UpdatePinRequest
import com.issueissyu.fe.data.model.MapPinMarker

interface PinRepository {
    suspend fun getPins(): List<Pin>
    suspend fun getPinById(pinId: String): Pin?
    suspend fun getIssuePins(): List<Pin>
    suspend fun getCommunityPins(): List<Pin>
    suspend fun getMyPins(): List<Pin>
    suspend fun createPin(request: CreatePinRequest): Pin
    suspend fun updatePin(pinId: String, request: UpdatePinRequest): Pin

    suspend fun getMapPinsInBounds(bounds: MapBounds): List<MapPinMarker>

    /** 시연용: `dummyPins`의 공감 여부·카운트를 토글하고 갱신된 핀을 반환합니다. */
    suspend fun toggleSympathy(pinId: String): Pin

    /** 시연용: 이슈 핀 청원(동의) 1회 반영. 이미 청원한 경우 핀을 그대로 반환합니다. */
    suspend fun petitionPin(pinId: String): Pin

    /** 시연용: 이슈 핀에 시민해결사로 참여. 작성자·해결 완료·중복 참여는 변경 없이 반환하거나 예외를 던집니다. */
    suspend fun joinResolver(pinId: String, currentUserId: String): Pin
}