package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.LocationApi
import com.issueissyu.fe.domain.repository.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationApi: LocationApi,
) : LocationRepository {

    override suspend fun locationVerification(lat: Double, lng: Double): Result<String> {
        return try {
            val response = locationApi.locationVerification(lat, lng)
            when (response.code) {
                "LOCATION_200_4" -> {
                    val address = response.result?.address?.takeIf { it.isNotBlank() }.orEmpty()
                    Result.success(address)
                }
                "LOCATION_400_1" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "좌표 형식 또는 범위가 올바르지 않습니다." },
                        ),
                    )
                "LOCATION_404_1" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "주소 조회 결과를 찾을 수 없습니다." },
                        ),
                    )
                "LOCATION_404_3" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "법정동 코드 조회 결과를 찾을 수 없습니다." },
                        ),
                    )
                "LOCATION_502_2" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "주소 변환 서비스 연결에 실패했습니다." },
                        ),
                    )
                "COMMON_500" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요." },
                        ),
                    )
                else ->
                    if (response.isSuccess) {
                        Result.success(response.result?.address?.takeIf { it.isNotBlank() }.orEmpty())
                    } else {
                        Result.failure(
                            Exception(response.message.ifBlank { "위치 인증에 실패했습니다." }),
                        )
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
