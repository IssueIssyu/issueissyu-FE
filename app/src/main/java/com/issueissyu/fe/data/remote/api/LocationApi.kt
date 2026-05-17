package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.location.LocationBaseResponse
import com.issueissyu.fe.data.remote.dto.response.location.LocationResolveResponse
import com.issueissyu.fe.data.remote.dto.response.location.LocationVerificationResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LocationApi {


    //내 위치
    @GET("api/location/user")
    suspend fun myAddress(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): BaseResponse<LocationBaseResponse>

    //동네 인증 및 등록 (최종 확인 시에만 호출)
    @POST("api/location/user/cert")
    suspend fun certifyUserLocation(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
    ): BaseResponse<LocationVerificationResponse?>
}
