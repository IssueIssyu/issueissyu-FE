package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.request.alarm.StoreTokenRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.alarm.AlarmListResponse
import com.issueissyu.fe.data.remote.dto.response.alarm.ConfirmAlarmResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
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
    ): BaseResponse<AlarmListResponse?>

    @PATCH("api/alarms/{alarmId}/confirm")
    suspend fun confirmAlarm(
        @Path("alarmId") alarmId: Long
    ): BaseResponse<ConfirmAlarmResponse?>
}