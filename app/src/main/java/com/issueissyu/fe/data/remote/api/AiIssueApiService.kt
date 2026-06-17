package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.issue.IssueAiDraftResponse
import com.issueissyu.fe.data.remote.dto.response.issue.IssueReliabilityResponse
import com.issueissyu.fe.data.remote.dto.response.issue.IssueToneTypeResponse
import com.issueissyu.fe.data.remote.dto.response.pin.IssuePinEditResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinImportResponse
import com.issueissyu.fe.data.remote.dto.response.pin.RateLimitQuotaResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Query

interface AiIssueApiService {
    @GET("issues/tone-types")
    suspend fun getIssueToneTypes(): BaseResponse<List<IssueToneTypeResponse>?>

    @FormUrlEncoded
    @POST("issues/pin/ai")
    suspend fun createIssuePinAiDraft(
        @Field("title") title: String,
        @Field("content") content: String,
        @Field("tone") tone: String,
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double,
    ): BaseResponse<IssueAiDraftResponse?>

    @Multipart
    @POST("issues/pin")
    suspend fun createIssuePin(
        @Part("request") request: RequestBody,
        @Part photos: List<MultipartBody.Part>,
    ): BaseResponse<PinImportResponse?>

    @Multipart
    @PATCH("issues/pin/{pin_id}")
    suspend fun editIssuePin(
        @Path("pin_id") pinId: Long,
        @Part("request") request: RequestBody,
        @Part photos: List<MultipartBody.Part>,
    ): BaseResponse<IssuePinEditResponse?>

    //이슈 핀 수정 횟수
    @GET("issues/pin/edit/quota")
    suspend fun getIssuePinEditQuota(
        @Query("pin_id") pinId: Long,
    ): BaseResponse<RateLimitQuotaResponse?>

    // AI 글쓰기 제한 횟수
    @GET("issues/pin/ai/quota")
    suspend fun getIssueAiDraftQuota(): BaseResponse<RateLimitQuotaResponse?>

    @GET("issues/pin/{pin_id}/reliability")
    suspend fun getIssueReliability(
        @Path("pin_id") pinId: Long,
    ): BaseResponse<IssueReliabilityResponse?>
}
