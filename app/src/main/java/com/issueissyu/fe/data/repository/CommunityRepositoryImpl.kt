package com.issueissyu.fe.data.repository

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
                title = "[${kind.name}] ${region}의 고정 소식 $index",
                content = "이것은 ${tab.displayName} 탭의 ${index}번째 게시글 상세 내용입니다.",
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
                viewCount = index * 10,
                likeCount = index * 5,
                eventStartTime = if (kind == CommunityItemKind.FESTIVAL) {
                    "2026-05-14T00:00:00.000Z"
                } else {
                    null
                },
                eventEndTime = if (kind == CommunityItemKind.FESTIVAL) {
                    "2026-05-20T00:00:00.000Z"
                } else {
                    null
                },
                discount = if (kind == CommunityItemKind.STORE) {
                    "${(index % 5 + 1) * 10}%"
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
