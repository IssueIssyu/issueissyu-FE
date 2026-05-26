package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.issue.IssueReliabilityResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface AiIssueApiService {
    @GET("issues/pin/{pin_id}/reliability")
    suspend fun getIssueReliability(
        @Path("pin_id") pinId: Long,
    ): BaseResponse<IssueReliabilityResponse?>
}
