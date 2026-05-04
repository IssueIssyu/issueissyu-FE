package com.issueissyu.fe.data.repository

import javax.inject.Inject
import com.issueissyu.fe.data.model.*
import com.issueissyu.fe.data.model.MapPinMarker
import java.time.Instant
import java.util.UUID

class PinRepositoryImpl @Inject constructor() : PinRepository {

    // TODO: 실제 백엔드와 연결 시 이 더미 데이터 대신 네트워크 호출 로직으로 대체해야 합니다.
    private val dummyPins = mutableListOf<Pin>()
    private val currentUser = PinUser(id = "user1_id", name = "현재 사용자", imageUrl = "https://example.com/user1.jpg")

    init {
        // 더미 데이터 초기화
        val user1 = currentUser // currentUser와 user1 정보 통일
        val user2 = PinUser(id = "user2_id", name = "박개발", imageUrl = "https://example.com/user2.jpg")

        // IssuePin 더미 데이터
        dummyPins.add(
            Pin(
                id = "issue_pin_1", // 고정 ID
                title = "공원 벤치 파손",
                description = "어린이 공원 내 벤치가 파손되어 위험합니다. 빠른 조치 부탁드립니다.",
                coordinate = PinCoordinate(latitude = 37.5665, longitude = 126.9780),
                address = "서울특별시 중구 세종대로 110",
                locationName = "서울시청 인근 공원",
                neighborhoodId = "nbh001",
                neighborhoodName = "명동",
                imageUrls = listOf("https://example.com/issue_bench1.jpg"),
                viewCount = 120,
                sympathyCount = 30,
                emojiReactions = listOf(PinEmojiReaction("emoji_fire", 10, true)),
                communityPostId = null,
                createdAt = "2023-04-15T10:00:00Z",
                updatedAt = null,
                detail = IssuePinDetail(
                    writer = user1,
                    resolutionStatus = ResolutionStatus.BEFORE_RESOLUTION,
                    petitionCount = 5,
                    isPetitionedByMe = true
                )
            )
        )
        dummyPins.add(
            Pin(
                id = "issue_pin_2", // 고정 ID
                title = "길거리 쓰레기 무단 투기",
                description = "매일 아침 출근길에 쓰레기가 방치되어 있습니다. 미관을 해치고 위생상 좋지 않습니다.",
                coordinate = PinCoordinate(latitude = 37.5700, longitude = 126.9800),
                address = "서울특별시 종로구 종로 1",
                locationName = "종로타워 앞",
                neighborhoodId = "nbh002",
                neighborhoodName = "종로",
                imageUrls = emptyList(),
                viewCount = 80,
                sympathyCount = 15,
                emojiReactions = listOf(PinEmojiReaction("emoji_angry", 5)),
                communityPostId = null,
                createdAt = "2023-04-14T15:30:00Z",
                updatedAt = "2023-04-14T16:00:00Z",
                detail = IssuePinDetail(
                    writer = user2,
                    resolutionStatus = ResolutionStatus.IN_PROGRESS
                )
            )
        )
        // 커뮤니티에 올라간 IssuePin 더미 데이터
        dummyPins.add(
            Pin(
                id = "issue_pin_3_community", // 고정 ID
                title = "횡단보도 안전 시설물 파손 (커뮤니티 연동)",
                description = "어린이 보호 구역 내 횡단보도 안전 시설물이 파손되어 아이들에게 위험합니다.",
                coordinate = PinCoordinate(latitude = 37.5750, longitude = 126.9700),
                address = "서울특별시 종로구 사직로 161",
                locationName = "경복궁역 인근",
                neighborhoodId = "nbh002",
                neighborhoodName = "종로",
                imageUrls = listOf("https://example.com/issue_crosswalk1.jpg"),
                viewCount = 150,
                sympathyCount = 40,
                emojiReactions = listOf(PinEmojiReaction("emoji_sad", 12)),
                communityPostId = "community_post_issue_1", // 커뮤니티 게시물 ID
                createdAt = "2023-04-16T14:00:00Z",
                updatedAt = null,
                detail = IssuePinDetail(
                    writer = user1,
                    resolutionStatus = ResolutionStatus.RESOLVED,
                    petitionCount = 8,
                    isPetitionedByMe = false
                )
            )
        )

        // CommunicationPin 더미 데이터
        dummyPins.add(
            Pin(
                id = "comm_pin_1", // 고정 ID
                title = "동네 맛집 추천",
                description = "저희 동네에 새로 생긴 파스타집 정말 맛있어요! 여러분께 추천합니다.",
                coordinate = PinCoordinate(latitude = 37.5600, longitude = 126.9900),
                address = "서울특별시 중구 을지로 66",
                locationName = "을지로3가",
                neighborhoodId = "nbh001",
                neighborhoodName = "명동",
                imageUrls = listOf("https://example.com/pasta.jpg"),
                viewCount = 200,
                sympathyCount = 50,
                emojiReactions = listOf(PinEmojiReaction("emoji_heart", 40, true)),
                communityPostId = "community_post_1",
                createdAt = "2023-04-13T11:00:00Z",
                updatedAt = null,
                detail = CommunicationPinDetail(
                    writer = user1
                )
            )
        )
        dummyPins.add(
            Pin(
                id = "comm_pin_2", // 고정 ID
                title = "주말에 같이 러닝하실 분 구해요",
                description = "여의도 공원에서 토요일 오전에 같이 뛸 분 찾습니다!",
                coordinate = PinCoordinate(latitude = 37.5280, longitude = 126.9320),
                address = "서울특별시 영등포구 여의도동",
                locationName = "여의도 공원",
                neighborhoodId = "nbh003",
                neighborhoodName = "여의도",
                imageUrls = emptyList(),
                viewCount = 90,
                sympathyCount = 20,
                emojiReactions = emptyList(),
                communityPostId = "community_post_2",
                createdAt = "2023-04-12T18:00:00Z",
                updatedAt = null,
                detail = CommunicationPinDetail(
                    writer = user2
                )
            )
        )

        // ShopPin 더미 데이터
        dummyPins.add(
            Pin(
                id = "shop_pin_1", // 고정 ID
                title = "새로운 편집샵 오픈",
                description = "성수동에 유니크한 패션 아이템을 파는 편집샵이 오픈했습니다.",
                coordinate = PinCoordinate(latitude = 37.5445, longitude = 127.0560),
                address = "서울특별시 성동구 성수동2가",
                locationName = "성수동 카페거리",
                neighborhoodId = "nbh004",
                neighborhoodName = "성수",
                imageUrls = listOf("https://example.com/shop1.jpg"),
                viewCount = 300,
                sympathyCount = 80,
                emojiReactions = listOf(PinEmojiReaction("emoji_star", 20)),
                communityPostId = null,
                createdAt = "2023-04-10T09:00:00Z",
                updatedAt = null,
                detail = ShopPinDetail(
                    keywords = listOf("편집샵", "패션", "성수동")
                )
            )
        )

        // FestivalPin 더미 데이터
        dummyPins.add(
            Pin(
                id = "festival_pin_1", // 고정 ID
                title = "봄꽃 축제",
                description = "여의도에서 아름다운 봄꽃 축제가 열립니다. 많은 참여 바랍니다.",
                coordinate = PinCoordinate(latitude = 37.5300, longitude = 126.9350),
                address = "서울특별시 영등포구 여의도동",
                locationName = "여의도 한강공원",
                neighborhoodId = "nbh003",
                neighborhoodName = "여의도",
                imageUrls = listOf("https://example.com/festival1.jpg"),
                viewCount = 500,
                sympathyCount = 100,
                emojiReactions = listOf(PinEmojiReaction("emoji_flower", 50)),
                communityPostId = null,
                createdAt = "2023-04-05T10:00:00Z",
                updatedAt = null,
                detail = FestivalPinDetail(
                    keywords = listOf("봄꽃", "축제", "한강"),
                    startDate = "2023-04-20T09:00:00Z",
                    endDate = "2023-04-25T20:00:00Z"
                )
            )
        )
    }

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

    override suspend fun getMapPinsInBounds(
        swLat: Double,
        swLng: Double,
        neLat: Double,
        neLng: Double
    ): List<MapPinMarker> {
        return dummyPins
            .filter { pin ->
                pin.coordinate.latitude in swLat..neLat &&
                pin.coordinate.longitude in swLng..neLng
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