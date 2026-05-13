package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.location.LocationVerificationResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LocationApi {
    // 내 위치 조회
    // EPSG:4326 좌표
    // 핀 생성 가능 여부 확인
    // 도로명 주소 조회

    //위치 인증
    @POST("api/location/user/cert")
    suspend fun locationVerification(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
    ): BaseResponse<LocationVerificationResponse?>
}
