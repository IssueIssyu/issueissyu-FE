package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.request.alarm.StoreTokenRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.map.AlarmListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AlarmApi {
    @POST("api/alarms/push-token")
    suspend fun storeToken(
        @Body request: StoreTokenRequest
    ): BaseResponse<Unit>

    @GET("api/alarms-list")
    suspend fun getAlarmList(
        @Query("size") size: Int? = null,
        @Query("cursor") cursor: String? = null,
    ): BaseResponse<AlarmListResponse>
}