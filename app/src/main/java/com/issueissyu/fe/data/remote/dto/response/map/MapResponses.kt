package com.issueissyu.fe.data.remote.dto.response.map

data class MapPinResponse(
    val pins: List<MapPinItemResponse>? = null,
    val clusters: List<MapPinClusterResponse>? = null,
)

data class MapPinClusterResponse(
    val clusterId: Long? = null,
    val clusterLatitude: Double? = null,
    val clusterLongitude: Double? = null,
    val pinCount: Int? = null,
    val pins: List<MapPinItemResponse>? = null,
)

data class MapPinItemResponse(
    val pinId: Long? = null,
    val pinType: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val pinDetailAddress: String? = null,
    val pinLocation: String? = null,
)

data class MapPinCardResponse(
    val pinId: Long? = null,
    val pinType: String? = null,
    val pinTitle: String? = null,
    val pinContent: String? = null,
    val issuePinState: String? = null,
    val pinDetailAddress: String? = null,
    val likeCount: Long? = null,
    val pinUserId: String? = null,
    val pinUserProfile: String? = null,
    val pinUserNickname: String? = null,
    val pinImageUrl: String? = null,
    val discount: String? = null,
    val storeImageUrl: String? = null,
    val communityId: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLike: Boolean? = null,
    val isMine: Boolean? = null,
)

data class PatchNoteResponse(
    val pins: List<PatchNotePinItemResponse>? = null,
    val pageInfo: PatchNotePageInfoResponse? = null,
)

data class PatchNotePinItemResponse(
    val pinId: Long? = null,
    val pinType: String? = null,
    val pinTitle: String? = null,
    val viewCount: Int? = null,
    val pinDetailAddress: String? = null,
    val issuePinState: String? = null,
    val pinUserProfile: String? = null,
    val pinUserNickname: String? = null,
    val createdAt: String? = null,
)

data class PatchNotePageInfoResponse(
    val hasNext: Boolean? = null,
    val nextCursor: String? = null,
)

data class MapNoticeListResponse(
    val notices: List<MapNoticeItemResponse>? = null,
)

data class MapNoticeItemResponse(
    val noticeId: Long? = null,
    val pinId: Long? = null,
    val noticeContent: String? = null,
)
