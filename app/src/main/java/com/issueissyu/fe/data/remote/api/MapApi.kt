package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.map.MapNoticeListResponse
import com.issueissyu.fe.data.remote.dto.response.map.MapPinCardResponse
import com.issueissyu.fe.data.remote.dto.response.map.MapPinResponse
import com.issueissyu.fe.data.remote.dto.response.map.PatchNoteResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MapApi {
    @GET("api/map/pins")
    suspend fun getPinsInScreen(
        @Query("swLat") swLat: Double,
        @Query("swLng") swLng: Double,
        @Query("neLat") neLat: Double,
        @Query("neLng") neLng: Double,
        @Query("category") category: String? = null,
    ): BaseResponse<MapPinResponse?>

    @GET("api/map/{pinId}/card")
    suspend fun getPinCard(
        @Path("pinId") pinId: Long,
    ): BaseResponse<MapPinCardResponse?>

    @GET("api/map/patch-note")
    suspend fun getPatchNotes(
        @Query("locationId") locationId: Long? = null,
        @Query("size") size: Int? = null,
        @Query("cursor") cursor: String? = null,
    ): BaseResponse<PatchNoteResponse?>

    @GET("api/map/notices")
    suspend fun getMapNotices(): BaseResponse<MapNoticeListResponse?>
}
