package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.request.mypage.ChangeNickNameRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.location.LocationVerificationResponse
import com.issueissyu.fe.data.remote.dto.response.mypage.GetMyIssueResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Query

interface MyPageApi {
    //닉네임 변경
    @PATCH("api/users/me/nickname")
    suspend fun changeNickName(
        @Body request: ChangeNickNameRequest
    ): BaseResponse<Unit>
    //동네 변경
    @PATCH("api/users/me/region")
    suspend fun changeAddress(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): BaseResponse<LocationVerificationResponse?>
    //내 이슈 조회
    @GET("api/users/me/pins")
    suspend fun getMyIssue(
        @Query("size") size: Int = 10,
        @Query("cursor") cursor: String? = null,
    ): BaseResponse<GetMyIssueResponse?>

    //알람 설정 토글
}