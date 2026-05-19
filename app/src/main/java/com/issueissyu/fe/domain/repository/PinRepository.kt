package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.pin.CreatePinRequest
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.UpdatePinRequest
import com.issueissyu.fe.domain.model.MapPinMarker
import com.issueissyu.fe.domain.model.pin.PinEmojiCandidate
import com.issueissyu.fe.domain.model.pin.PinEmojis
import com.issueissyu.fe.domain.model.pin.PinLike

interface PinRepository {
    suspend fun getPins(): List<Pin>
    suspend fun getPinById(pinId: String): Pin?
    suspend fun getIssuePins(): List<Pin>
    suspend fun getCommunityPins(): List<Pin>
    suspend fun getMyPins(): List<Pin>
    suspend fun createPin(request: CreatePinRequest): Pin
    suspend fun updatePin(pinId: String, request: UpdatePinRequest): Pin

    suspend fun getMapPinsInBounds(bounds: MapBounds): List<MapPinMarker>

    suspend fun getPinEmojis(pinId: Long): Result<PinEmojis>

    suspend fun getEmojiCandidates(): Result<List<PinEmojiCandidate>>

    suspend fun applyPinEmoji(pinId: Long, emojiId: Int): Result<Int?>

    suspend fun likePin(pinId: Long): Result<PinLike>

    suspend fun deletePin(pinId: Long): Result<Unit>
}
