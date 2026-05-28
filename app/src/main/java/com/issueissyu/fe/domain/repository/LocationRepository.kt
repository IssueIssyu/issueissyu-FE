package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.LocationRegions
import com.issueissyu.fe.domain.model.ResolvedLocation
import com.issueissyu.fe.domain.model.pin.PinCoordinate

interface LocationRepository {
    // 내 위치 조회
    suspend fun myAddress(
        lat: Double,
        lng: Double,
    ): Result<String>

    // 좌표 → 도로명 주소 및 locationId 조회
    suspend fun resolveAddressAndLocationId(
        lat: Double,
        lng: Double,
    ): Result<ResolvedLocation>

    // 핀 생성 가능 여부 확인
    suspend fun checkPinCreationAvailable(
        userCoordinate: PinCoordinate,
        pinCoordinate: PinCoordinate,
    ): Result<String>

    // 지역구 목록 조회
    suspend fun getRegionList(): Result<LocationRegions>

    // locationId 기준 도-시-군구 지역명 조회
    suspend fun getRegionName(locationId: Long): Result<String>

    // 인증된 사용자 동네 조회
    suspend fun getUserLocation(): Result<String>

    //도로명 주소 조회
    suspend fun certifyUserLocation(
        lat: Double,
        lng: Double,
    ): Result<String>
}
