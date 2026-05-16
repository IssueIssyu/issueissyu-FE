package com.issueissyu.fe.data.repository

import com.issueissyu.fe.domain.model.community.CommunityDetail
import com.issueissyu.fe.domain.model.community.CommunityFeed
import com.issueissyu.fe.domain.model.community.CommunityFeedItem
import com.issueissyu.fe.domain.model.community.CommunityItemKind
import com.issueissyu.fe.domain.model.community.CommunityTab
import com.issueissyu.fe.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CommunityRepositoryImpl @Inject constructor() : CommunityRepository {

    override fun getCommunityFeed(
        tab: CommunityTab,
        region: String,
        cursor: String?,
        size: Int
    ): Flow<CommunityFeed> = flow {
        // 더미 데이터 생성 (고정 데이터 위주)
        val sampleKinds = listOf(
            CommunityItemKind.ISSUE,
            CommunityItemKind.STORE,
            CommunityItemKind.FESTIVAL,
            CommunityItemKind.POLICY,
            CommunityItemKind.CONTEST,
            CommunityItemKind.CARDNEWS,
            CommunityItemKind.COMMUNICATION
        )

        val targetKind = tab.toItemKindOrNull()

        val dummyItems = List(size) { index ->
            val communityId = (index + 1).toLong()
            val kind = targetKind ?: sampleKinds[index % sampleKinds.size]

            val eventStartTime = if (
                kind == CommunityItemKind.FESTIVAL ||
                kind == CommunityItemKind.STORE ||
                kind == CommunityItemKind.CONTEST
            ) {
                "2026-05-14T00:00:00.000Z"
            } else {
                null
            }

            val eventEndTime = if (eventStartTime != null) {
                "2026-05-20T00:00:00.000Z"
            } else {
                null
            }

            val content = when (kind) {
                CommunityItemKind.STORE -> "신메뉴 출시 기념 할인 이벤트 진행 중! 지금 방문하시면 혜택을 드립니다."
                CommunityItemKind.FESTIVAL -> "이번 주말 마포아트센터에서 열리는 지역 축제입니다. 가족과 함께 오세요."
                CommunityItemKind.POLICY -> "2026년 마포구 청년 월세 지원 정책 안내입니다. 대상자를 확인하세요."
                CommunityItemKind.CONTEST -> "제 3회 지역 주민 대상 아이디어 공모전. 총 상금 500만원!"
                CommunityItemKind.CARDNEWS -> "한눈에 보는 마포구 생활 정보 카드뉴스"
                else -> "이것은 ${tab.displayName} 탭의 ${index}번째 게시글 상세 내용입니다."
            }

            CommunityFeedItem(
                communityId = communityId,
                pinId = when (kind) {
                    CommunityItemKind.ISSUE,
                    CommunityItemKind.COMMUNICATION,
                    CommunityItemKind.STORE,
                    CommunityItemKind.FESTIVAL -> communityId * 10
                    else -> null
                },
                kind = kind,
                title = when (kind) {
                    CommunityItemKind.STORE -> "[할인] ${region} 맛집 오픈 이벤트"
                    CommunityItemKind.FESTIVAL -> "[축제] 2026 ${region} 봄꽃 축제"
                    CommunityItemKind.POLICY -> "[정책] ${region} 청년 지원 사업"
                    CommunityItemKind.CONTEST -> "[공모전] ${region} 캐릭터 디자인 공모"
                    CommunityItemKind.CARDNEWS -> "[카드뉴스] ${region} 주간 소식"
                    else -> "[${kind.name}] ${region}의 고정 소식 $index"
                },
                content = content,
                thumbnailUrl = if (index % 3 == 0) {
                    "https://picsum.photos/400/300?random=$index"
                } else {
                    null
                },
                writerNickname = when (kind) {
                    CommunityItemKind.ISSUE,
                    CommunityItemKind.COMMUNICATION -> "작성자 $index"
                    else -> null
                },
                writerProfileUrl = null,
                address = "${region} 어느 길 $index",
                viewCount = index * 10 + 5,
                likeCount = index * 5 + 2,
                eventStartTime = eventStartTime,
                eventEndTime = eventEndTime,
                discount = if (kind == CommunityItemKind.STORE) {
                    "${(index % 3 + 1) * 10}% 할인"
                } else {
                    null
                },
                isHot = tab == CommunityTab.HOT || index < 3
            )
        }

        emit(
            CommunityFeed(
                items = dummyItems,
                region = region,
                nextCursor = if (dummyItems.size == size) "next_cursor_${tab.name}" else null,
                hasNext = dummyItems.size == size
            )
        )
    }

    override fun getCommunityDetail(
        communityId: Long
    ): Flow<CommunityDetail> = flow {
        val kind = when ((communityId % 4).toInt()) {
            0 -> CommunityItemKind.ISSUE
            1 -> CommunityItemKind.STORE
            2 -> CommunityItemKind.FESTIVAL
            else -> CommunityItemKind.POLICY
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
                    CommunityItemKind.STORE -> "마포구 맛집 오픈 이벤트"
                    CommunityItemKind.FESTIVAL -> "2026 마포구 봄꽃 축제"
                    CommunityItemKind.POLICY -> "마포구 청년 지원 정책"
                    else -> "우리 동네 새로운 이슈"
                },
                content = "커뮤니티 상세 화면 더미 본문입니다. 실제 API 연결 전까지 화면 구조 확인용으로 사용합니다.",
                imageUrls = listOf(
                    "https://picsum.photos/600/400?random=$communityId"
                ),
                writerNickname = if (kind == CommunityItemKind.ISSUE || kind == CommunityItemKind.COMMUNICATION) {
                    "작성자 $communityId"
                } else {
                    null
                },
                writerProfileUrl = null,
                address = "서울 마포구 어느 길 $communityId",
                viewCount = communityId.toInt() * 10,
                likeCount = communityId.toInt() * 3,
                createdAt = "2026-05-14T00:00:00.000Z",
                updatedAt = null,
                isReported = false,
                isPetitioned = false,
                isProblemSolver = false,
                isMine = false
            )
        )
    }

    private fun CommunityTab.toItemKindOrNull(): CommunityItemKind? {
        return when (this) {
            CommunityTab.ISSUE -> CommunityItemKind.ISSUE
            CommunityTab.COMMUNICATION -> CommunityItemKind.COMMUNICATION
            CommunityTab.STORE -> CommunityItemKind.STORE
            CommunityTab.FESTIVAL -> CommunityItemKind.FESTIVAL
            CommunityTab.POLICY -> CommunityItemKind.POLICY
            CommunityTab.CONTEST -> CommunityItemKind.CONTEST
            CommunityTab.CARDNEWS -> CommunityItemKind.CARDNEWS
            CommunityTab.ALL,
            CommunityTab.HOT -> null
        }
    }
}
