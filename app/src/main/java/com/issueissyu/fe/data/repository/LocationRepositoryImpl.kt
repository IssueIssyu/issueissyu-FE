package com.issueissyu.fe.data.repository

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.gson.Gson
import com.issueissyu.fe.data.remote.api.LocationApi
import com.issueissyu.fe.data.remote.dto.response.location.LocationRegionGroupResponse
import com.issueissyu.fe.data.remote.dto.response.location.LocationRegionItemResponse
import com.issueissyu.fe.data.remote.dto.response.location.LocationRegionListResponse
import com.issueissyu.fe.data.remote.dto.response.location.LocationResolveResponse
import com.issueissyu.fe.data.remote.dto.response.location.UserRegionSnippetResponse
import com.issueissyu.fe.domain.model.LocationRegionGroup
import com.issueissyu.fe.domain.model.LocationRegionItem
import com.issueissyu.fe.domain.model.LocationRegions
import com.issueissyu.fe.domain.model.ResolvedLocation
import com.issueissyu.fe.domain.model.UserRegion
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.domain.repository.LocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationApi: LocationApi,
    private val gson: Gson,
    @ApplicationContext private val context: Context,
) : LocationRepository {

    override suspend fun myAddress(lat: Double, lng: Double): Result<String> {
        val fromServer = fetchMyAddressFromServer(lat, lng)
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

    private suspend fun fetchMyAddressFromServer(lat: Double, lng: Double): Result<String> {
        return try {
            val response = locationApi.getRoadAddress(lat, lng)
            when (response.code) {
                "LOCATION_200_3" -> {
                    val address = response.result?.address?.takeIf { it.isNotBlank() }
                    if (address != null) {
                        Result.success(address)
                    } else {
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "주소 조회 결과가 올바르지 않습니다." },
                            ),
                        )
                    }
                }
                "COMMON_500" ->
                    Result.failure(
                        Exception(
                            response.message.ifBlank { "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요." },
                        ),
                    )
                else ->
                    if (response.isSuccess) {
                        val address = response.result?.address?.takeIf { it.isNotBlank() }
                        if (address != null) {
                            Result.success(address)
                        } else {
                            Result.failure(
                                Exception(
                                    response.message.ifBlank { "주소 조회 결과가 올바르지 않습니다." },
                                ),
                            )
                        }
                    } else {
                        Result.failure(
                            Exception(response.message.ifBlank { "주소 조회에 실패했습니다." }),
                        )
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resolveAddressAndLocationId(
        lat: Double,
        lng: Double,
    ): Result<ResolvedLocation> {
        return try {
            val response = locationApi.resolveAddressAndLocationId(lat, lng)
            if (response.isSuccess) {
                response.result?.toResolvedLocation()
                    ?: Result.failure(Exception(response.message.ifBlank { "주소 조회 결과가 올바르지 않습니다." }))
            } else {
                Result.failure(Exception(response.message.ifBlank { "주소와 지역 정보를 조회하지 못했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkPinCreationAvailable(
        userCoordinate: PinCoordinate,
        pinCoordinate: PinCoordinate,
    ): Result<String> {
        return try {
            val response = locationApi.isUserCanPostPin(
                userLat = userCoordinate.latitude,
                userLng = userCoordinate.longitude,
                pinLat = pinCoordinate.latitude,
                pinLng = pinCoordinate.longitude,
            )
            if (response.isSuccess) {
                Result.success(response.result?.address?.takeIf { it.isNotBlank() }.orEmpty())
            } else {
                Result.failure(
                    pinCreationAvailabilityException(
                        code = response.code,
                        message = response.message,
                    ),
                )
            }
        } catch (e: HttpException) {
            val errorEnvelope = e.response()
                ?.errorBody()
                ?.string()
                ?.takeIf { it.isNotBlank() }
                ?.let { body ->
                    runCatching { gson.fromJson(body, LocationErrorEnvelope::class.java) }.getOrNull()
                }
            Result.failure(
                pinCreationAvailabilityException(
                    code = errorEnvelope?.code,
                    message = errorEnvelope?.message,
                    httpStatus = e.code(),
                ),
            )
        } catch (e: IOException) {
            Result.failure(Exception("네트워크 연결을 확인한 뒤 다시 시도해주세요.", e))
        } catch (e: Exception) {
            Result.failure(Exception("핀 생성 가능 여부를 확인하지 못했습니다. 잠시 후 다시 시도해주세요.", e))
        }
    }

    private fun pinCreationAvailabilityException(
        code: String?,
        message: String?,
        httpStatus: Int? = null,
    ): Exception {
        val fallbackMessage = when {
            code == "LOCATION_400_1" || httpStatus == 400 ->
                "선택한 위치 정보가 올바르지 않습니다. 다른 위치를 선택해주세요."
            httpStatus == 401 ->
                "로그인 정보를 확인한 뒤 다시 시도해주세요."
            httpStatus == 403 ->
                "현재 위치와 선택한 위치가 너무 멉니다. 가까운 위치를 선택해주세요."
            code == "LOCATION_404_1" || code == "LOCATION_404_3" || httpStatus == 404 ->
                "선택한 위치의 주소를 찾을 수 없습니다. 다른 위치를 선택해주세요."
            code == "LOCATION_502_2" || httpStatus == 502 ->
                "위치 확인 서비스 연결에 실패했습니다. 잠시 후 다시 시도해주세요."
            code == "COMMON_500" || httpStatus?.let { it >= 500 } == true ->
                "일시적인 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            else ->
                "핀 생성 가능 여부를 확인하지 못했습니다. 잠시 후 다시 시도해주세요."
        }
        return Exception(message?.takeIf { it.isNotBlank() } ?: fallbackMessage)
    }

    private data class LocationErrorEnvelope(
        val code: String = "",
        val message: String = "",
    )

    override suspend fun getRegionList(): Result<LocationRegions> {
        return try {
            val response = locationApi.getRegionList()
            if (response.isSuccess || response.code == "LOCATION_LIST_204") {
                Result.success(response.result?.toLocationRegions() ?: LocationRegions(null, emptyList()))
            } else {
                Result.failure(Exception(response.message.ifBlank { "지역 목록을 조회하지 못했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRegionName(locationId: Long): Result<String> {
        return try {
            val response = locationApi.getRegionName(locationId)
            if (response.isSuccess) {
                val region = response.result?.region?.takeIf { it.isNotBlank() }
                    ?: return Result.failure(
                        Exception(response.message.ifBlank { "지역구 이름을 찾을 수 없습니다." }),
                    )
                Result.success(region)
            } else {
                Result.failure(Exception(response.message.ifBlank { "지역구 이름을 조회하지 못했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserLocation(): Result<String> {
        return try {
            val response = locationApi.getUserLocation()
            if (response.isSuccess) {
                val address = response.result?.address?.takeIf { it.isNotBlank() }
                    ?: return Result.failure(
                        Exception(response.message.ifBlank { "인증된 동네를 찾을 수 없습니다." }),
                    )
                Result.success(address)
            } else {
                Result.failure(Exception(response.message.ifBlank { "인증된 동네를 조회하지 못했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun LocationResolveResponse.toResolvedLocation(): Result<ResolvedLocation> {
        val resolvedLocationId = locationId
            ?: return Result.failure(Exception("지역 ID를 찾을 수 없습니다."))
        val resolvedAddress = address?.takeIf { it.isNotBlank() }
            ?: return Result.failure(Exception("주소 조회 결과를 찾을 수 없습니다."))

        return Result.success(
            ResolvedLocation(
                locationId = resolvedLocationId,
                address = resolvedAddress,
            ),
        )
    }

    private fun LocationRegionListResponse.toLocationRegions(): LocationRegions {
        return LocationRegions(
            userRegion = user?.toUserRegion(),
            groups = locations.orEmpty().mapNotNull { it.toLocationRegionGroup() },
        )
    }

    private fun UserRegionSnippetResponse.toUserRegion(): UserRegion? {
        val id = userLocationId ?: return null
        val name = userLocation?.takeIf { it.isNotBlank() } ?: return null
        return UserRegion(locationId = id, location = name)
    }

    private fun LocationRegionGroupResponse.toLocationRegionGroup(): LocationRegionGroup? {
        val name = superLocation?.takeIf { it.isNotBlank() } ?: return null
        return LocationRegionGroup(
            superLocation = name,
            subLocations = subLocation.orEmpty().mapNotNull { it.toLocationRegionItem() },
        )
    }

    private fun LocationRegionItemResponse.toLocationRegionItem(): LocationRegionItem? {
        val id = locationId ?: return null
        val name = location?.takeIf { it.isNotBlank() } ?: return null
        return LocationRegionItem(locationId = id, location = name)
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
                                continuation.resume(list)
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
