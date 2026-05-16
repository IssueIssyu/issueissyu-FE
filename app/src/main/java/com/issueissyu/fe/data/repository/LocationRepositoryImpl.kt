package com.issueissyu.fe.data.repository

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.issueissyu.fe.data.remote.api.LocationApi
import com.issueissyu.fe.domain.repository.LocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationApi: LocationApi,
    @ApplicationContext private val context: Context,
) : LocationRepository {

    override suspend fun resolveAddressPreview(lat: Double, lng: Double): Result<String> {
        val fromServer = resolveAddressFromServer(lat, lng)
        if (fromServer.isSuccess) {
            return fromServer
        }
        return reverseGeocodeOnDevice(lat, lng)
    }

    override suspend fun certifyUserLocation(lat: Double, lng: Double): Result<String> {
        return try {
            val response = locationApi.certifyUserLocation(lat, lng)
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

    private suspend fun resolveAddressFromServer(lat: Double, lng: Double): Result<String> {
        return try {
            val response = locationApi.resolveAddress(lat, lng)
            val address = response.result?.address?.takeIf { it.isNotBlank() }
            if (response.isSuccess && address != null) {
                Result.success(address)
            } else {
                Result.failure(
                    Exception(response.message.ifBlank { "주소 조회에 실패했습니다." }),
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun reverseGeocodeOnDevice(lat: Double, lng: Double): Result<String> {
        return withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) {
                return@withContext Result.failure(
                    Exception("이 기기에서는 주소 미리보기를 사용할 수 없습니다."),
                )
            }

            runCatching {
                val geocoder = Geocoder(context, Locale.KOREA)
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(lat, lng, 1) { list ->
                            if (continuation.isActive) {
                                continuation.resume(list ?: emptyList())
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lng, 1) ?: emptyList()
                }

                val line = addresses.firstOrNull()?.let { address ->
                    address.getAddressLine(0)?.takeIf { it.isNotBlank() }
                        ?: listOfNotNull(
                            address.adminArea,
                            address.subAdminArea,
                            address.thoroughfare,
                            address.subThoroughfare,
                        ).filter { it.isNotBlank() }.joinToString(" ").takeIf { it.isNotBlank() }
                }

                line?.let { Result.success(it) }
                    ?: Result.failure(Exception("주소 조회 결과를 찾을 수 없습니다."))
            }.getOrElse { Result.failure(it) }
        }
    }
}
