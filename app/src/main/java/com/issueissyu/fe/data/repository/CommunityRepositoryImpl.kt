package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.CommunityApi
import com.issueissyu.fe.data.remote.dto.community.toCommunityFeed
import com.issueissyu.fe.domain.model.community.CommunityDetail
import com.issueissyu.fe.domain.model.community.CommunityFeed
import com.issueissyu.fe.domain.model.community.CommunityItemKind
import com.issueissyu.fe.domain.model.community.CommunityTab
import com.issueissyu.fe.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CommunityRepositoryImpl @Inject constructor(
    private val communityApi: CommunityApi,
) : CommunityRepository {

    override fun getCommunityFeed(
        tab: CommunityTab,
        region: String,
        cursor: String?,
        size: Int
    ): Flow<CommunityFeed> = flow {
        val response = communityApi.getCommunityFeed(
            tab = tab.toApiTab(),
            region = region,
            cursor = cursor,
            size = size,
        )

        if (!response.isSuccess) {
            throw IllegalStateException(response.message.ifBlank { "커뮤니티 소식을 불러오지 못했습니다." })
        }

        val result = response.result
            ?: throw IllegalStateException(response.message.ifBlank { "커뮤니티 피드 응답이 올바르지 않습니다." })

        val feed = result.toCommunityFeed()
        emit(feed.withHotPreview(tab))
    }

    override fun getCommunityDetail(
        communityId: Long
    ): Flow<CommunityDetail> = flow {
        val kind = when ((communityId % 7).toInt()) {
            0 -> CommunityItemKind.ISSUE
            1 -> CommunityItemKind.COMMUNICATION
            2 -> CommunityItemKind.STORE
            3 -> CommunityItemKind.FESTIVAL
            4 -> CommunityItemKind.POLICY
            5 -> CommunityItemKind.CONTEST
            else -> CommunityItemKind.CARDNEWS
        }

        emit(
            CommunityDetail(
                communityId = communityId,
                pinId = if (
                    kind == CommunityItemKind.ISSUE ||
                    kind == CommunityItemKind.COMMUNICATION ||
                    kind == CommunityItemKind.STORE ||
                    kind == CommunityItemKind.FESTIVAL
                ) {
                    communityId * 10
                } else {
                    null
                },
                kind = kind,
                title = when (kind) {
                    CommunityItemKind.ISSUE -> "우리 동네 새로운 공원 조성 소식"
                    CommunityItemKind.COMMUNICATION -> "성산동 근처 맛있는 카페 추천해주세요!"
                    CommunityItemKind.STORE -> "맛있는 빵집 오픈 1주년 전품목 할인 이벤트"
                    CommunityItemKind.FESTIVAL -> "2026 마포구 봄꽃 축제 안내"
                    CommunityItemKind.POLICY -> "청년 월세 지원 사업 신청 안내"
                    CommunityItemKind.CONTEST -> "제 1회 마포구 숏폼 영상 공모전"
                    else -> "이번 주 마포구 주요 소식 TOP 3"
                },
                content = """
                    이것은 커뮤니티 게시글 상세 내용입니다. 
                    실제 서비스에서는 백엔드에서 받아온 풍부한 내용이 표시됩니다.
                    
                    줄바꿈이 포함된 여러 줄의 텍스트를 테스트하기 위한 본문입니다.
                    가독성을 위해 줄 간격이 여유 있게 설정되어야 합니다.
                    
                    AI 신뢰도 점수와 사유, 그리고 각종 반응 영역이 하단에 배치됩니다.
                """.trimIndent(),
                imageUrls = if (kind == CommunityItemKind.COMMUNICATION) emptyList() else listOf(
                    "https://picsum.photos/800/600?random=${communityId}_1",
                    "https://picsum.photos/800/600?random=${communityId}_2",
                    "https://picsum.photos/800/600?random=${communityId}_3"
                ),
                writerNickname = if (kind == CommunityItemKind.ISSUE || kind == CommunityItemKind.COMMUNICATION) {
                    "이슈알리미"
                } else {
                    null
                },
                writerProfileUrl = if (kind == CommunityItemKind.ISSUE || kind == CommunityItemKind.COMMUNICATION) {
                    "https://picsum.photos/200/200?random=$communityId"
                } else {
                    null
                },
                address = "서울시 마포구 성산동 123-45",
                viewCount = 1234,
                likeCount = 56,
                createdAt = "2026-05-16T12:00:00.000Z",
                updatedAt = null,
                isReported = (communityId % 3 == 0L), // 더미: 3의 배수 ID는 신고됨 상태
                isPetitioned = false,
                isProblemSolver = false,
                isMine = (communityId % 2 == 0L), // 더미: 짝수 ID는 본인 글
                reliabilityScore = when (kind) {
                    CommunityItemKind.ISSUE -> when (communityId % 3) {
                        0L -> 33
                        1L -> 65
                        else -> 85
                    }
                    else -> null
                },
                reliabilityReason = when (kind) {
                    CommunityItemKind.ISSUE -> if (communityId % 4 != 0L) "출처가 불분명하며 허위 정보일 가능성이 있습니다." else null
                    else -> null
                },
                issueStatusText = if (kind == CommunityItemKind.ISSUE) "진행중" else null,
                discount = if (kind == CommunityItemKind.STORE) "전 품목 20% 할인" else null,
                eventStartTime = if (kind == CommunityItemKind.STORE || kind == CommunityItemKind.FESTIVAL) "2026-05-14T00:00:00.000Z" else null,
                eventEndTime = if (kind == CommunityItemKind.STORE || kind == CommunityItemKind.FESTIVAL) "2026-05-20T00:00:00.000Z" else null,
                petitionCount = if (kind == CommunityItemKind.ISSUE) 450 else 0,
                petitionTargetCount = if (kind == CommunityItemKind.ISSUE) 1000 else null,
                isPetitionedByMe = false
            )
        )
    }

    private fun CommunityTab.toApiTab(): String {
        return when (this) {
            CommunityTab.ALL -> "ALL"
            CommunityTab.HOT -> "HOT"
            CommunityTab.ISSUE -> "ISSUE"
            CommunityTab.COMMUNICATION -> "COMMUNICATION"
            CommunityTab.STORE -> "STORE"
            CommunityTab.FESTIVAL -> "FESTIVAL"
            CommunityTab.POLICY -> "POLICY"
            CommunityTab.CONTEST -> "CONTEST"
            CommunityTab.CARDNEWS -> "CARDNEWS"
        }
    }

    private fun CommunityFeed.withHotPreview(tab: CommunityTab): CommunityFeed {
        if (tab != CommunityTab.ALL) return this

        return copy(
            items = items.mapIndexed { index, item ->
                item.copy(isHot = index < HOT_PREVIEW_COUNT)
            }
        )
    }

    private companion object {
        const val HOT_PREVIEW_COUNT = 3
    }
}
