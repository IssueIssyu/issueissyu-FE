package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.community.CommunityDetailResponse
import com.issueissyu.fe.data.remote.dto.response.community.CommunityFeedResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityApi {
    @GET("api/communities")
    suspend fun getCommunityFeed(
        @Query("tab") tab: String = "ALL",
        @Query("region") region: String,
        @Query("cursor") cursor: String? = null,
        @Query("size") size: Int? = null,
    ): BaseResponse<CommunityFeedResponse?>

    @GET("api/communities/{communityId}")
    suspend fun getCommunityDetail(
        @Path("communityId") communityId: Long,
    ): BaseResponse<CommunityDetailResponse?>
}
