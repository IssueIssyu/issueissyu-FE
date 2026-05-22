package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.request.pin.PinDeclarationRequest
import com.issueissyu.fe.data.remote.dto.request.pin.ApplyPinEmojiRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinDetailHomeResponse
import com.issueissyu.fe.data.remote.dto.response.pin.ApplyPinEmojiResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinDetailPostResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojisResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojiDto
import com.issueissyu.fe.data.remote.dto.response.pin.PinLikeResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

interface PinApi {

    //핀 상세 홈
    @GET("api/pins/{pinId}/home")
    suspend fun pinHome(
        @Path("pinId") pinId: Long,
    ): BaseResponse<PinDetailHomeResponse?>

    //핀 상세 포스트
    @GET("api/pins/{pinId}/post")
    suspend fun pinPost(
        @Path("pinId") pinId: Long,
    ): BaseResponse<PinDetailPostResponse?>

    //핀 삭제
    @DELETE("api/pins/{pinId}/delete")
    suspend fun pinDelete(
        @Path("pinId") pinId: Long
    ): BaseResponse<Unit?>

    //핀 공감
    @POST("api/pins/{pinId}/like")
    suspend fun pinLike(
        @Path("pinId") pinId: Long,
    ): BaseResponse<PinLikeResponse?>

    //이모지 조회
    @GET("api/pins/{pinId}/emojis")
    suspend fun getPinEmojis(
        @Path("pinId") pinId: Long,
    ): BaseResponse<PinEmojisResponse?>

    // 이모지 피커 목록 조회
    @GET("api/emojis/candidates")
    suspend fun getEmojiCandidates(): BaseResponse<List<PinEmojiDto>?>

    // 내 핀 반응 토글
    @PUT("api/pins/{pinId}/emojis/me")
    suspend fun applyPinEmoji(
        @Path("pinId") pinId: Long,
        @Body request: ApplyPinEmojiRequest,
    ): BaseResponse<ApplyPinEmojiResponse?>

    //핀 신고
    @POST("api/pins/{pinId}/declarations")
    suspend fun pinDeclare(
        @Path("pinId") pinId: Long,
        @Body request: PinDeclarationRequest
    ): BaseResponse<Unit>
}