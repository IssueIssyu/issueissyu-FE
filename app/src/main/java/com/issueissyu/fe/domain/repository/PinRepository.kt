package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.pin.PinComment
import com.issueissyu.fe.domain.model.pin.CreatePinRequest
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.UpdatePinRequest
import com.issueissyu.fe.domain.model.MapPinMarker
import com.issueissyu.fe.domain.model.pin.PinEmojiCandidate
import com.issueissyu.fe.domain.model.pin.PinEmojis
import com.issueissyu.fe.domain.model.pin.PinLike
import com.issueissyu.fe.domain.model.pin.PinPostSympathyContent

interface PinRepository {
    suspend fun getPins(): List<Pin>
    suspend fun getPinById(pinId: String): Pin?
    suspend fun getIssuePins(): List<Pin>
    suspend fun getCommunityPins(): List<Pin>
    suspend fun getMyPins(): List<Pin>
    suspend fun createPin(request: CreatePinRequest): Pin
    suspend fun updatePin(pinId: String, request: UpdatePinRequest): Pin

    suspend fun getMapPinsInBounds(bounds: MapBounds): List<MapPinMarker>

    suspend fun getPinDetailHome(pinId: Long): Result<Pin>

    suspend fun getPinDetailPost(pinId: Long): Result<PinPostSympathyContent?>

    suspend fun getPinEmojis(pinId: Long): Result<PinEmojis>

    suspend fun getEmojiCandidates(): Result<List<PinEmojiCandidate>>

    /** 바텀시트(피커): POST. [emojiId]가 null이면 반응 삭제. */
    suspend fun applyPinEmojiFromPicker(pinId: Long, emojiId: Long?): Result<Long?>

    /** 목록 칩: PUT. 동일 이모지 재요청 시 백엔드에서 토글(취소) 처리. */
    suspend fun togglePinEmojiFromList(pinId: Long, emojiId: Long): Result<Long?>

    suspend fun likePin(pinId: Long): Result<PinLike>

    suspend fun deletePin(pinId: Long): Result<Unit>

    suspend fun declarePin(pinId: Long, reasonIndex: Int): Result<Unit>

    suspend fun getPinComments(pinId: Long): Result<List<PinComment>>
    suspend fun createPinComment(pinId: Long, content: String): Result<PinComment>
    suspend fun updatePinComment(commentId: Long, content: String): Result<PinComment>
    suspend fun deletePinComment(commentId: Long): Result<Unit>

    suspend fun joinResolution(pinId: Long): Result<Unit>
    suspend fun submitResolutionProof(pinId: Long, imageUri: String): Result<Unit>
}