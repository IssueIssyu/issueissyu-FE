package com.issueissyu.fe.data.repository

import com.issueissyu.fe.core.network.ApiErrorMapper
import com.issueissyu.fe.core.text.decodePinContentNewlines
import com.issueissyu.fe.data.remote.api.MapApi
import com.issueissyu.fe.data.remote.dto.response.map.MapNoticeItemResponse
import com.issueissyu.fe.data.remote.dto.response.map.MapPinCardResponse
import com.issueissyu.fe.data.remote.dto.response.map.MapPinClusterResponse
import com.issueissyu.fe.data.remote.dto.response.map.MapPinItemResponse
import com.issueissyu.fe.data.remote.dto.response.map.PatchNotePinItemResponse
import com.issueissyu.fe.data.remote.dto.response.map.PatchNoteResponse
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.MapNotice
import com.issueissyu.fe.domain.model.MapPinCluster
import com.issueissyu.fe.domain.model.MapPinMarker
import com.issueissyu.fe.domain.model.MapPinQueryResult
import com.issueissyu.fe.domain.model.PatchNote
import com.issueissyu.fe.domain.model.PatchNotePage
import com.issueissyu.fe.domain.model.pin.CommunicationPinDetail
import com.issueissyu.fe.domain.model.pin.FestivalPinDetail
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.domain.model.pin.PinDetail
import com.issueissyu.fe.domain.model.pin.PinUser
import com.issueissyu.fe.domain.model.pin.ResolutionStatus
import com.issueissyu.fe.domain.model.pin.ShopPinDetail
import com.issueissyu.fe.domain.repository.MapRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapRepositoryImpl @Inject constructor(
    private val mapApi: MapApi,
    private val apiErrorMapper: ApiErrorMapper,
) : MapRepository {

    private suspend fun <T> failureFrom(e: Exception, fallback: String): Result<T> =
        Result.failure(apiErrorMapper.toException(e, fallback))

    override suspend fun getMapPinsInBounds(
        bounds: MapBounds,
        zoomLevel: Int,
        category: PinCategory?,
    ): Result<MapPinQueryResult> {
        return try {
            val apiCategory = category?.toApiCategory()
            val response = if (zoomLevel <= CLUSTERING_MAX_ZOOM_LEVEL) {
                mapApi.getClusteredPinsInScreen(
                    swLat = bounds.swLat,
                    swLng = bounds.swLng,
                    neLat = bounds.neLat,
                    neLng = bounds.neLng,
                    category = apiCategory,
                    zoomLevel = zoomLevel,
                )
            } else {
                mapApi.getPinsInScreen(
                    swLat = bounds.swLat,
                    swLng = bounds.swLng,
                    neLat = bounds.neLat,
                    neLng = bounds.neLng,
                    category = apiCategory,
                )
            }

            if (response.isSuccess) {
                val result = response.result
                Result.success(
                    MapPinQueryResult(
                        pins = result?.pins.orEmpty().mapNotNull { it.toMapPinMarker() },
                        clusters = result?.clusters.orEmpty().mapNotNull { it.toMapPinCluster() },
                    )
                )
            } else {
                Result.failure(Exception(response.message.ifBlank { "지도 핀 목록 조회에 실패했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPinCard(pinId: String): Result<Pin> {
        val numericPinId = pinId.toLongOrNull()
            ?: return Result.failure(Exception("핀 ID가 올바르지 않습니다."))

        return try {
            val response = mapApi.getPinCard(numericPinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(Exception(response.message.ifBlank { "핀 카드 응답이 올바르지 않습니다." }))
                Result.success(result.toPin())
            } else {
                Result.failure(Exception(response.message.ifBlank { "핀 카드 조회에 실패했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPatchNotes(
        locationId: Long?,
        size: Int?,
        cursor: String?,
    ): Result<PatchNotePage> {
        return try {
            val response = mapApi.getPatchNotes(locationId = locationId, size = size, cursor = cursor)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(Exception(response.message.ifBlank { "패치노트 응답이 올바르지 않습니다." }))
                Result.success(result.toPatchNotePage())
            } else {
                Result.failure(Exception(response.message.ifBlank { "패치노트 조회에 실패했습니다." }))
            }
        } catch (e: Exception) {
            failureFrom(e, "패치노트 조회에 실패했습니다.")
        }
    }

    override suspend fun getMapNotices(): Result<List<MapNotice>> {
        return try {
            val response = mapApi.getMapNotices()
            if (response.isSuccess) {
                Result.success(response.result?.notices.orEmpty().mapNotNull { it.toMapNotice() })
            } else {
                Result.failure(Exception(response.message.ifBlank { "지도 공지사항 조회에 실패했습니다." }))
            }
        } catch (e: Exception) {
            failureFrom(e, "지도 공지사항 조회에 실패했습니다.")
        }
    }

    private fun MapPinItemResponse.toMapPinMarker(): MapPinMarker? {
        val id = pinId?.toString() ?: return null
        val lat = latitude ?: return null
        val lng = longitude ?: return null
        val category = pinType.toPinCategory()

        return MapPinMarker(
            pinId = id,
            category = category,
            coordinate = PinCoordinate(latitude = lat, longitude = lng),
            address = pinDetailAddress.orEmpty(),
            locationName = pinLocation?.takeIf { it.isNotBlank() } ?: pinDetailAddress.orEmpty(),
        )
    }

    private fun MapPinClusterResponse.toMapPinCluster(): MapPinCluster? {
        val id = clusterId?.toString() ?: return null
        val lat = clusterLatitude ?: return null
        val lng = clusterLongitude ?: return null
        val mappedPins = pins.orEmpty().mapNotNull { it.toMapPinMarker() }

        return MapPinCluster(
            clusterId = id,
            coordinate = PinCoordinate(latitude = lat, longitude = lng),
            pinCount = pinCount ?: mappedPins.size,
            pins = mappedPins,
        )
    }

    private fun MapPinCardResponse.toPin(): Pin {
        val category = pinType.toPinCategory()
        val writer = PinUser(
            id = pinUserId.orEmpty(),
            name = pinUserNickname?.takeIf { it.isNotBlank() } ?: "익명",
            imageUrl = pinUserProfile,
        )

        val detail: PinDetail = when (category) {
            PinCategory.ISSUE -> IssuePinDetail(
                writer = writer,
                resolutionStatus = issuePinState.toResolutionStatus(),
            )
            PinCategory.COMMUNICATION -> CommunicationPinDetail(writer = writer)
            PinCategory.SHOP -> ShopPinDetail(currentNews = discount)
            PinCategory.FESTIVAL -> FestivalPinDetail()
        }

        return Pin(
            id = pinId?.toString().orEmpty(),
            title = pinTitle.orEmpty(),
            description = pinContent.orEmpty().decodePinContentNewlines(),
            coordinate = PinCoordinate(
                latitude = latitude ?: 0.0,
                longitude = longitude ?: 0.0,
            ),
            address = pinDetailAddress.orEmpty(),
            locationName = pinDetailAddress,
            imageUrls = listOfNotNull(pinImageUrl, storeImageUrl).filter { it.isNotBlank() },
            viewCount = 0,
            sympathyCount = likeCount?.toInt() ?: 0,
            isSympathizedByMe = isLike ?: false,
            isMine = isMine ?: false,
            communityPostId = communityId?.toString(),
            createdAt = "",
            detail = detail,
        )
    }

    private fun PatchNoteResponse.toPatchNotePage(): PatchNotePage {
        return PatchNotePage(
            items = pins.orEmpty().mapNotNull { it.toPatchNote() },
            hasNext = pageInfo?.hasNext ?: false,
            nextCursor = pageInfo?.nextCursor,
        )
    }

    private fun PatchNotePinItemResponse.toPatchNote(): PatchNote? {
        val id = pinId?.toString() ?: return null
        return PatchNote(
            id = id,
            title = pinTitle.orEmpty(),
            viewCount = viewCount ?: 0,
            locationName = pinDetailAddress.orEmpty(),
            writerName = pinUserNickname?.takeIf { it.isNotBlank() } ?: "익명",
            writerImageUrl = pinUserProfile,
            resolutionStatus = issuePinState.toResolutionStatus(),
            createdAt = createdAt,
        )
    }

    private fun MapNoticeItemResponse.toMapNotice(): MapNotice? {
        val id = noticeId?.toString() ?: return null
        val content = noticeContent?.takeIf { it.isNotBlank() } ?: return null
        return MapNotice(
            id = id,
            pinId = pinId?.toString(),
            content = content,
        )
    }

    private fun String?.toPinCategory(): PinCategory {
        return when (this?.uppercase()) {
            "ISSUE" -> PinCategory.ISSUE
            "COMMUNICATION" -> PinCategory.COMMUNICATION
            "STORE", "SHOP" -> PinCategory.SHOP
            "FESTIVAL" -> PinCategory.FESTIVAL
            else -> PinCategory.ISSUE
        }
    }

    private fun PinCategory.toApiCategory(): String {
        return when (this) {
            PinCategory.ISSUE -> "issue"
            PinCategory.COMMUNICATION -> "communication"
            PinCategory.SHOP -> "store"
            PinCategory.FESTIVAL -> "festival"
        }
    }

    private fun String?.toResolutionStatus(): ResolutionStatus {
        return when (this?.uppercase()) {
            "IN_PROGRESS", "PROGRESS", "RESOLVING" -> ResolutionStatus.IN_PROGRESS
            "RESOLVED", "DONE" -> ResolutionStatus.RESOLVED
            else -> ResolutionStatus.BEFORE_RESOLUTION
        }
    }

    private companion object {
        const val CLUSTERING_MAX_ZOOM_LEVEL = 10
    }
}
