package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.request.alarm.StoreTokenRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.alarm.AlarmListResponse
import com.issueissyu.fe.data.remote.dto.response.alarm.AlarmToggleResponse
import com.issueissyu.fe.data.remote.dto.response.alarm.ConfirmAlarmResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AlarmApi {
    //알람 푸시 토큰
    @POST("api/alarms/push-token")
    suspend fun storeToken(
        @Body request: StoreTokenRequest
    ): BaseResponse<Unit>

    //지도 - 알람 리스트 조회
    @GET("api/alarms-list")
    suspend fun getAlarmList(
        @Query("size") size: Int? = null,
        @Query("cursor") cursor: String? = null,
    ): BaseResponse<AlarmListResponse?>

    //알람(백그라운드) - 조회 여부
    @PATCH("api/alarms/{alarmId}/confirm")
    suspend fun confirmAlarm(
        @Path("alarmId") alarmId: Long
    ): BaseResponse<ConfirmAlarmResponse?>

    //마이페이지 - 알람 토클 상태 조회
    @GET("api/users/me/alarms/state")
    suspend fun getAlarmToggle(): BaseResponse<AlarmToggleResponse?>
}