package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.collection.GetCollectionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CollectionApi {

    //마이페이지 프로필, 내 컬렉션 목록
    @GET("api/users/me/collections")
    suspend fun getCollections(
        @Query("checkUnlock") checkUnlock: Boolean = false
    ): BaseResponse<GetCollectionResponse?>

    //컬렉션 프로필 설정

    //컬렉션 북마크 설정
}