package com.issueissyu.fe.domain.repository

interface LocationRepository {
    // 내 위치 조회

    // EPSG:4326 좌표
    //지도 이동 시 -> post보다 get으로 정보만 가져오기
    //근데 이를 위한 api가 없는듯하여 추후 수정 예정 (지금은 다른 api연결해둠)
    suspend fun resolveAddressPreview(
        lat: Double,
        lng: Double,
    ): Result<String>


    // 핀 생성 가능 여부 확인

    //도로명 주소 조회
    suspend fun certifyUserLocation(
        lat: Double,
        lng: Double,
    ): Result<String>
}
