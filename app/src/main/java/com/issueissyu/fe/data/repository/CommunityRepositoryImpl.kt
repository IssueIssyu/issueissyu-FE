package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.CommunityApi
import com.issueissyu.fe.data.remote.dto.community.toCommunityDetail
import com.issueissyu.fe.data.remote.dto.community.toCommunityFeed
import com.issueissyu.fe.domain.model.community.CommunityDetail
import com.issueissyu.fe.domain.model.community.CommunityFeed
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
        region: String?,
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
        val response = communityApi.getCommunityDetail(communityId)

        if (!response.isSuccess) {
            throw IllegalStateException(response.message.ifBlank { "게시글을 불러오지 못했습니다." })
        }

        val result = response.result
            ?: throw IllegalStateException(response.message.ifBlank { "게시글 응답이 올바르지 않습니다." })

        emit(result.toCommunityDetail())
    }

    private fun CommunityTab.toApiTab(): String? {
        return when (this) {
            CommunityTab.ALL -> "HOME"
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
