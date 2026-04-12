package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.IssueDto
import retrofit2.http.GET

interface IssueApiService {
    @GET("issues")
    suspend fun getIssues(): List<IssueDto>
}
