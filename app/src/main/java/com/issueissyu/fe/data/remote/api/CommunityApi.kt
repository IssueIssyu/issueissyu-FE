package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.community.CommunityCommentResponse
import com.issueissyu.fe.data.remote.dto.response.community.CommunityDetailResponse
import com.issueissyu.fe.data.remote.dto.response.community.CommunityFeedResponse
import com.issueissyu.fe.data.remote.dto.request.community.CommunityCommentRequest
import com.issueissyu.fe.data.remote.dto.request.community.CommunityDeclarationRequest
import com.issueissyu.fe.data.remote.dto.response.pin.PinLikeResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface CommunityApi {
    @GET("api/communities")
    suspend fun getCommunityFeed(
        @Query("tab") tab: String? = null,
        @Query("locationId") locationId: Long? = null,
        @Query("cursor") cursor: String? = null,
        @Query("size") size: Int? = null,
    ): BaseResponse<CommunityFeedResponse?>

    @GET("api/communities/{communityId}")
    suspend fun getCommunityDetail(
        @Path("communityId") communityId: Long,
        @Query("kind") kind: String? = null,
    ): BaseResponse<CommunityDetailResponse?>

    @DELETE("api/communities/{communityId}")
    suspend fun deleteCommunity(
        @Path("communityId") communityId: Long,
    ): BaseResponse<Unit?>

    @DELETE("api/communities/{communityId}/takedown")
    suspend fun takedownCommunity(
        @Path("communityId") communityId: Long,
    ): BaseResponse<Unit?>

    @GET("api/communities/{communityId}/comments")
    suspend fun getCommunityComments(
        @Path("communityId") communityId: Long,
    ): BaseResponse<List<CommunityCommentResponse>?>

    @POST("api/communities/{communityId}/comments")
    suspend fun createCommunityComment(
        @Path("communityId") communityId: Long,
        @Body request: CommunityCommentRequest,
    ): BaseResponse<CommunityCommentResponse?>

    @PATCH("api/communities/comments/{commentId}")
    suspend fun updateCommunityComment(
        @Path("commentId") commentId: Long,
        @Body request: CommunityCommentRequest,
    ): BaseResponse<CommunityCommentResponse?>

    @DELETE("api/communities/comments/{commentId}")
    suspend fun deleteCommunityComment(
        @Path("commentId") commentId: Long,
    ): BaseResponse<Unit?>

    @POST("api/communities/{communityId}/like")
    suspend fun likeCommunity(
        @Path("communityId") communityId: Long,
    ): BaseResponse<PinLikeResponse?>

    @POST("api/communities/{communityId}/declaration")
    suspend fun declareCommunity(
        @Path("communityId") communityId: Long,
        @Body request: CommunityDeclarationRequest,
    ): BaseResponse<Unit?>
}
