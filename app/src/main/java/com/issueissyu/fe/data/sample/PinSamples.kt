package com.issueissyu.fe.data.sample

import com.issueissyu.fe.domain.model.pin.CommunicationPinDetail
import com.issueissyu.fe.domain.model.pin.FestivalPinDetail
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.pin.IssueResolverParticipation
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.domain.model.pin.PinEmojiReaction
import com.issueissyu.fe.domain.model.pin.PinUser
import com.issueissyu.fe.domain.model.pin.ResolutionStatus
import com.issueissyu.fe.domain.model.pin.ShopPinDetail

// 백엔드 연동 전까지만 사용하는 Pin 더미 데이터의 단일 원본.
// PinRepositoryImpl의 더미 데이터와 Compose Preview가 같은 데이터를 공유한다.
// TODO: 실제 API 연동이 끝나면 이 파일을 통째로 제거하고, PinRepositoryImpl 더미 의존도 함께 정리한다.
internal object PinSamples {

    // === 자주 참조하는 사용자 ===
    val user1 = PinUser(
        id = "user1_id",
        name = "현재 사용자",
        imageUrl = "https://example.com/user1.jpg"
    )
    val user2 = PinUser(
        id = "user2_id",
        name = "박개발",
        imageUrl = "https://example.com/user2.jpg"
    )

    // 시민해결사 시나리오 표현용 보조 사용자.
    val resolverHelper = PinUser(
        id = "user_resolver_id",
        name = "도와줄게요",
        imageUrl = null
    )

    // === 자주 참조하는 핀 ID 상수 (Preview / 테스트 / 화면 라우팅 시 활용) ===
    const val IssuePinId = "issue_pin_1"
    const val IssueInProgressPinId = "issue_pin_2"
    const val IssueResolvedPinId = "issue_pin_3_community"
    const val CommunicationPinId = "comm_pin_1"
    const val CommunicationPlainPinId = "comm_pin_2"
    const val ShopPinId = "shop_pin_1"
    const val FestivalPinId = "festival_pin_1"

    val pins: List<Pin> = listOf(
        // 이슈 핀 · BEFORE_RESOLUTION + 공감/이모지 다양성 (POST 탭 데이터 케이스 겸용)
        Pin(
            id = IssuePinId,
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
            isSympathizedByMe = true,
            emojiReactions = listOf(
                PinEmojiReaction("emoji_fire", 10, true),
                PinEmojiReaction("emoji_heart", 5),
                PinEmojiReaction("emoji_clap", 3),
                PinEmojiReaction("emoji_laugh", 2)
            ),
            communityPostId = null,
            createdAt = "2023-04-15T10:00:00Z",
            updatedAt = null,
            detail = IssuePinDetail(
                writer = user1,
                resolutionStatus = ResolutionStatus.BEFORE_RESOLUTION,
                petitionCount = 5,
                isPetitionedByMe = true,
                petitionTargetCount = 50
            )
        ),

        // 이슈 핀 · IN_PROGRESS + 시민해결사 2명 + 청원 진행 중
        // user1은 단순 참여(인증 미제출), resolverHelper는 인증 사진 제출.
        Pin(
            id = IssueInProgressPinId,
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
                resolutionStatus = ResolutionStatus.IN_PROGRESS,
                resolverParticipations = listOf(
                    IssueResolverParticipation(
                        problemSolverId = 31L,
                        user = user1,
                        joinedAt = "2023-04-14T17:00:00Z",
                        proofImageUrls = emptyList()
                    ),
                    IssueResolverParticipation(
                        problemSolverId = 32L,
                        user = resolverHelper,
                        joinedAt = "2023-04-14T15:30:00Z",
                        proofImageUrls = listOf("https://example.com/proof_inprogress.jpg"),
                        proofSubmittedAt = "2023-04-14T18:00:00Z"
                    )
                ),
                petitionCount = 38,
                isPetitionedByMe = false,
                petitionTargetCount = 50
            )
        ),

        // 이슈 핀 · RESOLVED + 작성자 인정 완료 + 커뮤니티 연동
        Pin(
            id = IssueResolvedPinId,
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
            communityPostId = "community_post_issue_1",
            createdAt = "2023-04-16T14:00:00Z",
            updatedAt = null,
            detail = IssuePinDetail(
                writer = user1,
                resolutionStatus = ResolutionStatus.RESOLVED,
                resolverParticipations = listOf(
                    IssueResolverParticipation(
                        problemSolverId = 41L,
                        user = resolverHelper,
                        joinedAt = "2023-04-16T15:00:00Z",
                        proofImageUrls = listOf("https://example.com/proof_resolved.jpg"),
                        proofSubmittedAt = "2023-04-16T16:00:00Z",
                        isConfirmedByWriter = true,
                        confirmedAt = "2023-04-16T17:00:00Z"
                    )
                ),
                resolvedBy = resolverHelper,
                resolvedAt = "2023-04-16T17:00:00Z",
                petitionCount = 50,
                isPetitionedByMe = true,
                petitionTargetCount = 50
            )
        ),

        // 소통 핀 · 커뮤니티 게시물 연동
        Pin(
            id = CommunicationPinId,
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
            detail = CommunicationPinDetail(writer = user1)
        ),

        // 소통 핀 · 이모지 없음 / 공감 안 함 (POST 탭 빈 상태 케이스 겸용)
        Pin(
            id = CommunicationPlainPinId,
            title = "주말에 같이 러닝하실 분 구해요",
            description = "여의도 공원에서 토요일 오전에 같이 뛸 분 찾습니다!",
            coordinate = PinCoordinate(latitude = 37.5280, longitude = 126.9320),
            address = "서울특별시 영등포구 여의도동",
            locationName = "여의도 공원",
            neighborhoodId = "nbh003",
            neighborhoodName = "여의도",
            imageUrls = emptyList(),
            viewCount = 90,
            sympathyCount = 0,
            isSympathizedByMe = false,
            emojiReactions = emptyList(),
            communityPostId = "community_post_2",
            createdAt = "2023-04-12T18:00:00Z",
            updatedAt = null,
            detail = CommunicationPinDetail(writer = user2)
        ),

        // 가게 핀
        Pin(
            id = ShopPinId,
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
        ),

        // 축제 핀
        Pin(
            id = FestivalPinId,
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

    fun findById(id: String): Pin = pins.firstOrNull { it.id == id }
        ?: error("PinSamples does not contain pin with id=$id")
}
