package com.issueissyu.fe.data.repository

import com.issueissyu.fe.data.remote.api.MapApi
import com.issueissyu.fe.data.remote.dto.response.map.MapNoticeItemResponse
import com.issueissyu.fe.data.remote.dto.response.map.MapPinCardResponse
import com.issueissyu.fe.data.remote.dto.response.map.MapPinItemResponse
import com.issueissyu.fe.data.remote.dto.response.map.PatchNotePinItemResponse
import com.issueissyu.fe.data.remote.dto.response.map.PatchNoteResponse
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.MapNotice
import com.issueissyu.fe.domain.model.MapPinMarker
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
) : MapRepository {

    override suspend fun getMapPinsInBounds(
        bounds: MapBounds,
        category: PinCategory?,
    ): Result<List<MapPinMarker>> {
        return try {
            val response = mapApi.getPinsInScreen(
                swLat = bounds.swLat,
                swLng = bounds.swLng,
                neLat = bounds.neLat,
                neLng = bounds.neLng,
                category = category?.toApiCategory(),
            )

            if (response.isSuccess) {
                Result.success(response.result?.pins.orEmpty().mapNotNull { it.toMapPinMarker() })
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
        region: String?,
        size: Int?,
        cursor: String?,
    ): Result<PatchNotePage> {
        return try {
            val response = mapApi.getPatchNotes(region = region, size = size, cursor = cursor)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(Exception(response.message.ifBlank { "패치노트 응답이 올바르지 않습니다." }))
                Result.success(result.toPatchNotePage())
            } else {
                Result.failure(Exception(response.message.ifBlank { "패치노트 조회에 실패했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
            Result.failure(e)
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
            description = pinContent.orEmpty(),
            coordinate = PinCoordinate(latitude = 0.0, longitude = 0.0),
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
            PinCategory.ISSUE -> "ISSUE"
            PinCategory.COMMUNICATION -> "COMMUNICATION"
            PinCategory.SHOP -> "STORE"
            PinCategory.FESTIVAL -> "FESTIVAL"
        }
    }

    private fun String?.toResolutionStatus(): ResolutionStatus {
        return when (this?.uppercase()) {
            "IN_PROGRESS", "PROGRESS", "RESOLVING" -> ResolutionStatus.IN_PROGRESS
            "RESOLVED", "DONE" -> ResolutionStatus.RESOLVED
            else -> ResolutionStatus.BEFORE_RESOLUTION
        }
    }
}
