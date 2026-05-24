package com.issueissyu.fe.data.remote.api

import com.issueissyu.fe.data.remote.dto.request.pin.PinDeclarationRequest
import com.issueissyu.fe.data.remote.dto.request.pin.ApplyPinEmojiRequest
import com.issueissyu.fe.data.remote.dto.request.pin.PinCommentsRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinDetailHomeResponse
import com.issueissyu.fe.data.remote.dto.response.pin.ApplyPinEmojiResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PetitionsJoinResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PetitionsGetResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PetitionStatusResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinCommentDto
import com.issueissyu.fe.data.remote.dto.response.pin.PinDetailPostResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojisResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojiDto
import com.issueissyu.fe.data.remote.dto.response.pin.PinLikeResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinSolveResponse
import com.issueissyu.fe.data.remote.dto.response.pin.GoNowResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PetitionSubmitResponse
import com.issueissyu.fe.data.remote.dto.response.pin.ProblemSolverJoinResponse
import com.issueissyu.fe.data.remote.dto.response.pin.ProblemSolverListResponse
import com.issueissyu.fe.data.remote.dto.response.pin.ProblemSolverPhotoResponse
import com.issueissyu.fe.data.remote.dto.response.pin.ProblemSolverVerificationResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Multipart

interface PinApi {

    //핀 상세 홈
    @GET("api/pins/{pinId}/home")
    suspend fun pinHome(
        @Path("pinId") pinId: Long,
    ): BaseResponse<PinDetailHomeResponse?>

    //핀 상세 포스트
    @GET("api/pins/{pinId}/post")
    suspend fun pinPost(
        @Path("pinId") pinId: Long,
    ): BaseResponse<PinDetailPostResponse?>

    //핀 삭제
    @DELETE("api/pins/{pinId}/delete")
    suspend fun pinDelete(
        @Path("pinId") pinId: Long
    ): BaseResponse<Unit?>

    //핀 공감
    @POST("api/pins/{pinId}/like")
    suspend fun pinLike(
        @Path("pinId") pinId: Long,
    ): BaseResponse<PinLikeResponse?>

    //청원하기
    @POST("api/pins/{pinId}/petitions")
    suspend fun submitPetition(
        @Path("pinId") pinId: Long,
    ): BaseResponse<PetitionSubmitResponse?>

    //시민해결사 참여
    @POST("api/pins/{pinId}/go-now")
    suspend fun goNow(
        @Path("pinId") pinId: Long,
    ): BaseResponse<GoNowResponse?>

    //청원 현황 조회
    @GET("api/pins/{pinId}/petitions/status")
    suspend fun getPetitionStatus(
        @Path("pinId") pinId: Long,
    ): BaseResponse<PetitionStatusResponse?>

    //이슈 핀 해결 상태 조회
    @GET("api/pins/{pinId}/solve")
    suspend fun getPinSolveStatus(
        @Path("pinId") pinId: Long,
    ): BaseResponse<PinSolveResponse?>

    //이모지 조회
    @GET("api/pins/{pinId}/emojis")
    suspend fun getPinEmojis(
        @Path("pinId") pinId: Long,
    ): BaseResponse<PinEmojisResponse?>

    // 이모지 피커 목록 조회
    @GET("api/emojis/candidates")
    suspend fun getEmojiCandidates(): BaseResponse<List<PinEmojiDto>?>

    // 이모지 등록 - 피커
    @POST("api/pins/{pinId}/emojis/me")
    suspend fun applyPinEmojiPicker(
        @Path("pinId") pinId: Long,
        @Body request: ApplyPinEmojiRequest,
    ): BaseResponse<ApplyPinEmojiResponse?>

    // 이모지 등록 - 목록
    @PUT("api/pins/{pinId}/emojis/me")
    suspend fun applyPinEmojiList(
        @Path("pinId") pinId: Long,
        @Body request: ApplyPinEmojiRequest,
    ): BaseResponse<ApplyPinEmojiResponse?>

    //핀 신고
    @POST("api/pins/{pinId}/declarations")
    suspend fun pinDeclare(
        @Path("pinId") pinId: Long,
        @Body request: PinDeclarationRequest
    ): BaseResponse<Unit>

    //핀 댓글 목록 조회
    @GET("api/pins/{pinId}/comments")
    suspend fun getPinComments(
        @Path("pinId") pinId: Long
    ): BaseResponse<List<PinCommentDto>?>

    //핀 댓글 작성
    @POST("api/pins/{pinId}/comments")
    suspend fun createPinComments(
        @Path("pinId") pinId: Long,
        @Body request: PinCommentsRequest
    ): BaseResponse<PinCommentDto?>

    //핀 댓글 수정
    @PATCH("api/pins/comments/{commentId}")
    suspend fun updatePinComments(
        @Path("commentId") commentId: Long,
        @Body request: PinCommentsRequest
    ): BaseResponse<PinCommentDto?>

    //핀 댓글 삭제
    @DELETE("api/pins/comments/{commentId}")
    suspend fun deletePinComments(
        @Path("commentId") commentId: Long
    ): BaseResponse<Unit>

    //핀 해결하기 조회
    @GET("api/pins/{pinId}/solve")
    suspend fun getPinSolve(
        @Path("pinId") pinId: Long
    ): BaseResponse<PinSolveResponse?>

    //시민 해결사 목록 조회
    @GET("api/pins/{pinId}/problem-solver/{userUid}")
    suspend fun getProblemSolver(
        @Path("pinId") pinId: Long,
        @Path("userUid") userUid: String
    ): BaseResponse<ProblemSolverListResponse?>

    //시민 해결사 참여 (지금가요)
    @POST("api/pins/{pinId}/go-now")
    suspend fun joinProblemSolver(
        @Path("pinId") pinId: Long
    ): BaseResponse<ProblemSolverJoinResponse?>

    //시민 해결사 인증 사진 첨부
    @Multipart
    @POST("api/pins/{problemSolverId}/photo")
    suspend fun photoProblemSolver(
        @Path("problemSolverId") problemSolverId: Long,
        @Part photo: MultipartBody.Part
    ): BaseResponse<ProblemSolverPhotoResponse?>

    //시민 해결사 인증 완료(내 핀)
    @PATCH("api/pins/{problemSolverId}")
    suspend fun verificationProblemSolver(
        @Path("problemSolverId") problemSolverId: Long
    ): BaseResponse<ProblemSolverVerificationResponse?>

    //청원
    @GET("api/pins/{pinId}/petitions/status")
    suspend fun getIssuePetition(
        @Path("pinId") pinId: Long
    ): BaseResponse<PetitionsGetResponse?>

    @POST("api/pins/{pinId}/petitions")
    suspend fun joinIssuePetition(
        @Path("pinId") pinId: Long
    ): BaseResponse<PetitionsJoinResponse?>
}
