package com.issueissyu.fe.domain.repository

interface LocationRepository {
    // 내 위치 조회
    suspend fun myAddress(
        lat: Double,
        lng: Double,
    ): Result<String>

    //도로명 주소 조회
    suspend fun certifyUserLocation(
        lat: Double,
        lng: Double,
    ): Result<String>
}
