package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.PinApi
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojiDto
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojisResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinLikeResponse
import com.issueissyu.fe.domain.model.pin.PinEmoji
import com.issueissyu.fe.domain.model.pin.PinEmojis
import com.issueissyu.fe.domain.model.pin.PinLike
import javax.inject.Inject
import javax.inject.Singleton
import com.issueissyu.fe.domain.model.MapPinMarker
import com.issueissyu.fe.data.sample.PinSamples
import com.issueissyu.fe.domain.model.pin.AuthoredPinDetail
import com.issueissyu.fe.domain.model.pin.CommunicationPinDetail
import com.issueissyu.fe.domain.model.pin.CreatePinRequest
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinDetail
import com.issueissyu.fe.domain.model.pin.PinUser
import com.issueissyu.fe.domain.model.pin.UpdatePinRequest
import com.issueissyu.fe.domain.repository.PinRepository
import java.time.Instant
import java.util.UUID

@Singleton
class PinRepositoryImpl @Inject constructor(
    private val pinApi: PinApi,
) : PinRepository {

    // TODO: 실제 백엔드와 연결 시 PinSamples 의존을 제거하고 네트워크 호출 로직으로 대체.
    private val dummyPins: MutableList<Pin> = PinSamples.pins.toMutableList()
    private val currentUser: PinUser = PinSamples.user1

    override suspend fun getPins(): List<Pin> {
        // TODO: 실제 백엔드 API 호출로 핀 목록을 가져오도록 구현해야 합니다.
        return dummyPins.toList()
    }

    override suspend fun getPinById(pinId: String): Pin? {
        // TODO: 실제 백엔드 API 호출로 특정 ID의 핀을 가져오도록 구현해야 합니다.
        return dummyPins.find { it.id == pinId }
    }

    override suspend fun getIssuePins(): List<Pin> {
        // TODO: 실제 백엔드 API 호출로 이슈 핀 목록을 가져오도록 구현해야 합니다.
        return dummyPins.filter { it.detail is IssuePinDetail }
    }

    override suspend fun getCommunityPins(): List<Pin> {
        // TODO: 실제 백엔드 API 호출로 커뮤니티 핀 목록을 가져오도록 구현해야 합니다.
        return dummyPins.filter { it.communityPostId != null && (it.detail is IssuePinDetail || it.detail is CommunicationPinDetail) }
    }

    override suspend fun getMyPins(): List<Pin> {
        // TODO: 실제 백엔드 API 호출로 내 핀 목록을 가져오도록 구현해야 합니다.
        return dummyPins.filter { pin ->
            (pin.detail as? AuthoredPinDetail)?.writer?.id == currentUser.id
        }
    }

    override suspend fun createPin(request: CreatePinRequest): Pin {
        val newPinDetail: PinDetail = when (request.category) {
            PinCategory.ISSUE -> IssuePinDetail(writer = currentUser)
            PinCategory.COMMUNICATION -> CommunicationPinDetail(writer = currentUser)
            PinCategory.SHOP, PinCategory.FESTIVAL -> throw IllegalArgumentException("Shop and Festival pins cannot be created by users.")
        }

        val newPin = Pin(
            id = UUID.randomUUID().toString(), // 새 핀은 UUID로 생성
            title = request.title,
            description = request.description,
            coordinate = request.coordinate,
            address = request.address,
            locationName = request.locationName,
            neighborhoodId = request.neighborhoodId,
            neighborhoodName = request.neighborhoodName,
            imageUrls = request.imageUrls,
            createdAt = Instant.now().toString(),
            updatedAt = null,
            detail = newPinDetail
        )
        // TODO: 실제 백엔드 API를 호출하여 핀을 생성하고, 서버로부터 반환된 실제 Pin 객체를 사용해야 합니다.
        dummyPins.add(newPin)
        return newPin
    }

    override suspend fun updatePin(pinId: String, request: UpdatePinRequest): Pin {
        val index = dummyPins.indexOfFirst { it.id == pinId }
        if (index == -1) {
            throw NoSuchElementException("Pin with id $pinId not found.")
        }

        val existingPin = dummyPins[index]

        // 작성자가 있는 핀만 일반 사용자가 수정 가능
        val authoredDetail = existingPin.detail as? AuthoredPinDetail
            ?: throw IllegalArgumentException("This pin type cannot be updated by users.")

        // 커뮤니티 게시물인 경우 수정 불가
        if (existingPin.communityPostId != null) {
            throw IllegalArgumentException("Community posts cannot be updated.")
        }

        // 작성자만 수정 가능
        if (authoredDetail.writer.id != currentUser.id) {
            throw SecurityException("User does not have permission to edit this pin.")
        }

        val updatedPin = existingPin.copy(
            title = request.title,
            description = request.description,
            coordinate = request.coordinate,
            address = request.address,
            locationName = request.locationName,
            neighborhoodId = request.neighborhoodId,
            neighborhoodName = request.neighborhoodName,
            imageUrls = request.imageUrls,
            updatedAt = Instant.now().toString()
        )

        // TODO: 실제 백엔드 API를 호출하여 핀을 업데이트하고, 서버로부터 반환된 실제 Pin 객체를 사용해야 합니다.
        dummyPins[index] = updatedPin
        return updatedPin
    }

    override suspend fun getMapPinsInBounds(bounds: MapBounds): List<MapPinMarker> {
        return dummyPins
            .filter { pin ->
                pin.coordinate.latitude in bounds.swLat..bounds.neLat &&
                pin.coordinate.longitude in bounds.swLng..bounds.neLng
            }
            .map { pin ->
                MapPinMarker(
                    pinId = pin.id,
                    category = pin.category,
                    coordinate = pin.coordinate,
                    address = pin.address,
                    locationName = pin.locationName ?: pin.address
                )
            }
    }

    override suspend fun likePin(pinId: Long): Result<PinLike> {
        return try {
            val response = pinApi.pinLike(pinId)
            when (response.code) {
                "PIN_LIKE_200" -> {
                    val result = response.result
                        ?: return Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 공감 응답이 올바르지 않습니다." },
                            ),
                        )
                    Result.success(result.toPinLike())
                }

                "PIN_LIKE_400_1" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "이미 공감된 핀입니다." },
                        ),
                    )

                "PIN_LIKE_404" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "존재하지 않는 핀 입니다." },
                        ),
                    )

                "PIN_LIKE_500" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "핀 공감하기 중 서버 오류가 발생했습니다." },
                        ),
                    )

                else ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "핀 공감에 실패했습니다." },
                        ),
                    )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 이모지 조회
    override suspend fun getPinEmojis(pinId: Long): Result<PinEmojis> {
        return try {
            val response = pinApi.getPinEmojis(pinId)
            when (response.code) {
                "PIN_NOT_FOUND_404" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "존재하지 않는 핀입니다." },
                        ),
                    )

                "JWT_401" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "인증이 필요합니다." },
                        ),
                    )

                else -> response.toPinEmojisResult()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun PinLikeResponse.toPinLike(): PinLike {
        return PinLike(
            pinId = pinId,
            pinLikeCount = pinLikeCount,
            isLike = isLike,
        )
    }

    private fun BaseResponse<PinEmojisResponse?>.toPinEmojisResult(): Result<PinEmojis> {
        val ok = code == "PIN_EMOJIS_200" || isSuccess
        if (!ok) {
            return Result.failure(
                Exception(message.ifBlank { "핀 반응 목록 조회에 실패했습니다." }),
            )
        }
        val payload = result
            ?: return Result.failure(
                Exception(message.ifBlank { "핀 반응 목록 응답이 올바르지 않습니다." }),
            )
        return Result.success(payload.toPinEmojis())
    }

    private fun PinEmojisResponse.toPinEmojis(): PinEmojis {
        return PinEmojis(
            selectedEmojiId = selectedEmojiId,
            emojis = emojis.orEmpty().mapNotNull { it.toPinEmoji() },
        )
    }

    private fun PinEmojiDto.toPinEmoji(): PinEmoji? {
        if (emojiId == 0 || emojiImageUrl.isBlank()) return null
        return PinEmoji(
            emojiId = emojiId,
            emojiImageUrl = emojiImageUrl,
            count = count,
            isDefault = isDefault,
            isOwned = isOwned,
            productId = productId,
        )
    }

    // 핀 삭제
    override suspend fun deletePin(pinId: Long): Result<Unit> {
        return try {
            val response = pinApi.pinDelete(pinId)
            when (response.code) {
                "PIN_DELETE_200" -> Result.success(Unit)

                "PIN_DELETE_400_1" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "등업된 이슈 핀은 삭제가 불가능 합니다." },
                        ),
                    )

                "PIN_DELETE_400_2" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "존재하지 않는 핀 입니다." },
                        ),
                    )

                "PIN_DELETE_400_3" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "핀 작성자가 아니므로 삭제 권한이 없습니다." },
                        ),
                    )

                "PIN_DELETE_400_4" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "핀 삭제 API를 실행할 수 없습니다." },
                        ),
                    )

                else ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "핀 삭제에 실패했습니다." },
                        ),
                    )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
