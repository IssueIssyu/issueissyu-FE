package com.issueissyu.fe.data.repository

import javax.inject.Inject
import com.issueissyu.fe.data.model.*
import com.issueissyu.fe.data.model.MapPinMarker
import com.issueissyu.fe.data.sample.PinSamples
import java.time.Instant
import java.util.UUID

class PinRepositoryImpl @Inject constructor() : PinRepository {

    // TODO: 실제 백엔드와 연결 시 PinSamples 의존을 제거하고 네트워크 호출 로직으로 대체.
    private val dummyPins: MutableList<Pin> = PinSamples.pins.toMutableList()
    private val currentUser: PinUser = PinSamples.user1

    private fun pinUserForId(userId: String): PinUser {
        return when (userId) {
            PinSamples.user1.id -> PinSamples.user1
            PinSamples.user2.id -> PinSamples.user2
            PinSamples.resolverHelper.id -> PinSamples.resolverHelper
            else -> PinUser(id = userId, name = "사용자", imageUrl = null)
        }
    }

    override suspend fun getPins(): List<Pin> {
        // TODO: 실제 백엔드 API 호출로 핀 목록을 가져오도록 구현해야 합니다.
        synchronized(dummyPins) {
            return dummyPins.toList()
        }
    }

    override suspend fun getPinById(pinId: String): Pin? {
        // TODO: 실제 백엔드 API 호출로 특정 ID의 핀을 가져오도록 구현해야 합니다.
        synchronized(dummyPins) {
            return dummyPins.find { it.id == pinId }
        }
    }

    override suspend fun getIssuePins(): List<Pin> {
        // TODO: 실제 백엔드 API 호출로 이슈 핀 목록을 가져오도록 구현해야 합니다.
        synchronized(dummyPins) {
            return dummyPins.filter { it.detail is IssuePinDetail }
        }
    }

    override suspend fun getCommunityPins(): List<Pin> {
        // TODO: 실제 백엔드 API 호출로 커뮤니티 핀 목록을 가져오도록 구현해야 합니다.
        synchronized(dummyPins) {
            return dummyPins.filter { it.communityPostId != null && (it.detail is IssuePinDetail || it.detail is CommunicationPinDetail) }
        }
    }

    override suspend fun getMyPins(): List<Pin> {
        // TODO: 실제 백엔드 API 호출로 내 핀 목록을 가져오도록 구현해야 합니다.
        synchronized(dummyPins) {
            return dummyPins.filter { pin ->
                (pin.detail as? AuthoredPinDetail)?.writer?.id == currentUser.id
            }
        }
    }

    override suspend fun createPin(request: CreatePinRequest): Pin {
        synchronized(dummyPins) {
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
    }

    override suspend fun updatePin(pinId: String, request: UpdatePinRequest): Pin {
        synchronized(dummyPins) {
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
    }

    override suspend fun getMapPinsInBounds(bounds: MapBounds): List<MapPinMarker> {
        synchronized(dummyPins) {
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

    override suspend fun toggleSympathy(pinId: String): Pin {
        synchronized(dummyPins) {
            val index = dummyPins.indexOfFirst { it.id == pinId }
            if (index == -1) {
                throw NoSuchElementException("Pin with id $pinId not found.")
            }
            val pin = dummyPins[index]
            val nextSympathized = !pin.isSympathizedByMe
            val nextCount = if (nextSympathized) {
                pin.sympathyCount + 1
            } else {
                (pin.sympathyCount - 1).coerceAtLeast(0)
            }
            val updated = pin.copy(
                isSympathizedByMe = nextSympathized,
                sympathyCount = nextCount,
                updatedAt = Instant.now().toString()
            )
            dummyPins[index] = updated
            return updated
        }
    }

    override suspend fun petitionPin(pinId: String): Pin {
        synchronized(dummyPins) {
            val index = dummyPins.indexOfFirst { it.id == pinId }
            if (index == -1) {
                throw NoSuchElementException("Pin with id $pinId not found.")
            }
            val pin = dummyPins[index]
            val issueDetail = pin.detail as? IssuePinDetail
                ?: throw IllegalArgumentException("Only issue pins support petition.")

            if (issueDetail.isPetitionedByMe) {
                return pin
            }

            val nextPetitionCount = issueDetail.petitionCount + 1

            val updatedDetail = issueDetail.copy(
                petitionCount = nextPetitionCount,
                isPetitionedByMe = true
            )
            val updated = pin.copy(
                detail = updatedDetail,
                updatedAt = Instant.now().toString()
            )
            dummyPins[index] = updated
            return updated
        }
    }

    override suspend fun joinResolver(pinId: String, currentUserId: String): Pin {
        synchronized(dummyPins) {
            val index = dummyPins.indexOfFirst { it.id == pinId }
            if (index == -1) {
                throw NoSuchElementException("Pin with id $pinId not found.")
            }
            val pin = dummyPins[index]
            val issueDetail = pin.detail as? IssuePinDetail
                ?: throw IllegalArgumentException("Only issue pins support resolver participation.")

            if (issueDetail.resolutionStatus == ResolutionStatus.RESOLVED) {
                throw IllegalStateException("Cannot join resolver on a resolved issue pin.")
            }
            if (issueDetail.writer.id == currentUserId) {
                throw IllegalStateException("Writer cannot join as a resolver.")
            }
            if (issueDetail.resolverParticipations.any { it.user.id == currentUserId }) {
                return pin
            }

            val participation = IssueResolverParticipation(
                user = pinUserForId(currentUserId),
                joinedAt = Instant.now().toString(),
                proofImageUrls = emptyList()
            )
            // 해결 전·해결 중 모두 시민해결사 참여가 있으면 진행 중으로 둔다.
            val nextStatus = ResolutionStatus.IN_PROGRESS
            val updatedDetail = issueDetail.copy(
                resolverParticipations = issueDetail.resolverParticipations + participation,
                resolutionStatus = nextStatus
            )
            val updated = pin.copy(
                detail = updatedDetail,
                updatedAt = Instant.now().toString()
            )
            dummyPins[index] = updated
            return updated
        }
    }

    override suspend fun updatePinForDemo(pinId: String): Pin {
        synchronized(dummyPins) {
            val index = dummyPins.indexOfFirst { it.id == pinId }
            if (index == -1) {
                throw NoSuchElementException("핀을 찾을 수 없습니다: $pinId")
            }

            val pin = dummyPins[index]
            if (!pin.canEditBy(currentUser.id)) {
                throw SecurityException("이 핀은 수정할 수 없습니다.")
            }

            // TODO: 실제 수정 화면 또는 PinCreateScreen edit mode로 교체.
            val updatedPin = pin.copy(
                title = pin.title.takeUnless { it.endsWith(" (수정됨)") }
                    ?.let { "$it (수정됨)" }
                    ?: pin.title,
                description = pin.description.takeUnless { it.contains("시연용으로 수정된 내용입니다.") }
                    ?.let { "$it\n\n시연용으로 수정된 내용입니다." }
                    ?: pin.description,
                updatedAt = Instant.now().toString()
            )
            dummyPins[index] = updatedPin
            return updatedPin
        }
    }

    override suspend fun deletePinForDemo(pinId: String) {
        synchronized(dummyPins) {
            val removed = dummyPins.removeIf { it.id == pinId }
            if (!removed) {
                throw NoSuchElementException("핀을 찾을 수 없습니다: $pinId")
            }
        }
    }
}
