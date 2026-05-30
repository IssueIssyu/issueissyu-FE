package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.collection.GetCollectionResponse
import com.issueissyu.fe.data.remote.dto.response.collection.SetProfileResponse
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface CollectionApi {

    //마이페이지 프로필, 내 컬렉션 목록
    @GET("api/users/me/collections")
    suspend fun getCollections(
        @Query("checkUnlock") checkUnlock: Boolean = false
    ): BaseResponse<GetCollectionResponse?>

    //컬렉션 프로필 설정
    @PATCH("api/users/me/collections/{collectionId}/profile")
    suspend fun setProfile(
        @Path("collectionId") collectionId: Long
    ): BaseResponse<SetProfileResponse?>

    //컬렉션 북마크 설정
}