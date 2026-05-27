package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.issue.IssueAiDraftResponse
import com.issueissyu.fe.data.remote.dto.response.issue.IssueReliabilityResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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

    @GET("issues/pin/{pin_id}/reliability")
    suspend fun getIssueReliability(
        @Path("pin_id") pinId: Long,
    ): BaseResponse<IssueReliabilityResponse?>
}
