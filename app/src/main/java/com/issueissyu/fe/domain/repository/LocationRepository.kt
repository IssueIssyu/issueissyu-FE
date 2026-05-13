package com.issueissyu.fe.domain.repository

interface LocationRepository {
    // 내 위치 조회
    // EPSG:4326 좌표
    // 핀 생성 가능 여부 확인
    // 도로명 주소 조회

    suspend fun locationVerification(
        lat: Double,
        lng: Double,
    ): Result<String>

}
