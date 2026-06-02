package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.request.alarm.StoreTokenRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AlarmApi {
    @POST("api/alarms/push-token")
    suspend fun storeToken(
        @Body request: StoreTokenRequest
    ): BaseResponse<Unit>
}