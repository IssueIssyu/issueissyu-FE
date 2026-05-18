package com.issueissyu.fe.data.repository

import javax.inject.Inject
import com.issueissyu.fe.domain.model.MapPinMarker
import com.issueissyu.fe.data.sample.PinSamples
import com.issueissyu.fe.domain.model.AuthoredPinDetail
import com.issueissyu.fe.domain.model.CommunicationPinDetail
import com.issueissyu.fe.domain.model.CreatePinRequest
import com.issueissyu.fe.domain.model.IssuePinDetail
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.Pin
import com.issueissyu.fe.domain.model.PinCategory
import com.issueissyu.fe.domain.model.PinDetail
import com.issueissyu.fe.domain.model.PinUser
import com.issueissyu.fe.domain.model.UpdatePinRequest
import com.issueissyu.fe.domain.repository.PinRepository
import java.time.Instant
import java.util.UUID

class PinRepositoryImpl @Inject constructor() : PinRepository {

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
}
