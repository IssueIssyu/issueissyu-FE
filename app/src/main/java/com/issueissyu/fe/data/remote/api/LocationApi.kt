package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.location.LocationBaseResponse
import com.issueissyu.fe.data.remote.dto.response.location.LocationRegionListResponse
import com.issueissyu.fe.data.remote.dto.response.location.LocationResolveResponse
import com.issueissyu.fe.data.remote.dto.response.location.LocationVerificationResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LocationApi {


    //좌표 → 도로명 주소 조회
    @GET("api/location/address")
    suspend fun getRoadAddress(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): BaseResponse<LocationBaseResponse>

    //좌표 → 도로명 주소 및 location_id
    @GET("api/location/resolve")
    suspend fun resolveAddressAndLocationId(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
    ): BaseResponse<LocationResolveResponse?>

    //핀 생성 가능 여부 확인
    @GET("api/location/pin/available")
    suspend fun isUserCanPostPin(
        @Query("userLat") userLat: Double,
        @Query("userLng") userLng: Double,
        @Query("pinLat") pinLat: Double,
        @Query("pinLng") pinLng: Double,
    ): BaseResponse<LocationBaseResponse?>

    //지역구 목록 조회
    @GET("api/location/regions")
    suspend fun getRegionList(): BaseResponse<LocationRegionListResponse?>

    //인증된 사용자 동네 조회
    @GET("api/location/user")
    suspend fun getUserLocation(): BaseResponse<LocationVerificationResponse?>

    //동네 인증 및 등록 (최종 확인 시에만 호출)
    @POST("api/location/user/cert")
    suspend fun certifyUserLocation(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
    ): BaseResponse<LocationVerificationResponse?>
}
