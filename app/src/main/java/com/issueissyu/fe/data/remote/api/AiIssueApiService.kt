package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.issue.IssueAiDraftResponse
import com.issueissyu.fe.data.remote.dto.response.issue.IssueReliabilityResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinImportResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface AiIssueApiService {
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
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part("tone") tone: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part images: List<MultipartBody.Part>,
    ): BaseResponse<PinImportResponse?>

    @GET("issues/pin/{pin_id}/reliability")
    suspend fun getIssueReliability(
        @Path("pin_id") pinId: Long,
    ): BaseResponse<IssueReliabilityResponse?>
}
