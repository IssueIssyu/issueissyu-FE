package com.issueissyu.fe.domain.repository

import com.issueissyu.fe.domain.model.pin.PinComment
import com.issueissyu.fe.domain.model.pin.CreatePinRequest
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PetitionStatus
import com.issueissyu.fe.domain.model.pin.PetitionSubmit
import com.issueissyu.fe.domain.model.pin.UpdatePinRequest
import com.issueissyu.fe.domain.model.MapPinMarker
import com.issueissyu.fe.domain.model.pin.GoNow
import com.issueissyu.fe.domain.model.pin.PinEmojiCandidate
import com.issueissyu.fe.domain.model.pin.PinEmojis
import com.issueissyu.fe.domain.model.pin.PinLike
import com.issueissyu.fe.domain.model.pin.PinPostSympathyContent
import com.issueissyu.fe.domain.model.pin.PinSolveInfo
import com.issueissyu.fe.domain.model.pin.PinSolveStatus
import com.issueissyu.fe.domain.model.pin.PetitionJoinInfo
import com.issueissyu.fe.domain.model.pin.PetitionStatusInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverJoinInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverPhotoInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverVerificationInfo

interface PinRepository {
    suspend fun getPins(): List<Pin>
    suspend fun getPinById(pinId: String): Pin?
    suspend fun getIssuePins(): List<Pin>
    suspend fun getCommunityPins(): List<Pin>
    suspend fun getMyPins(): List<Pin>
    suspend fun createPin(request: CreatePinRequest): Pin
    suspend fun updatePin(pinId: String, request: UpdatePinRequest): Pin

    suspend fun getMapPinsInBounds(bounds: MapBounds): List<MapPinMarker>

    suspend fun getPinDetailHome(pinId: Long): Result<Pin>

    suspend fun getPinDetailPost(pinId: Long): Result<PinPostSympathyContent?>

    //핀 이모지
    suspend fun getPinEmojis(pinId: Long): Result<PinEmojis>

    suspend fun getEmojiCandidates(): Result<List<PinEmojiCandidate>>

    suspend fun applyPinEmojiFromPicker(pinId: Long, emojiId: Long?): Result<Long?>

    suspend fun togglePinEmojiFromList(pinId: Long, emojiId: Long): Result<Long?>

    //핀 공감
    suspend fun likePin(pinId: Long): Result<PinLike>

    suspend fun getPetitionStatus(pinId: Long): Result<PetitionStatus>

    suspend fun getPinSolveStatus(pinId: Long): Result<PinSolveStatus>

    suspend fun submitPetition(pinId: Long): Result<PetitionSubmit>

    suspend fun goNow(pinId: Long): Result<GoNow>

    //핀 삭제
    suspend fun deletePin(pinId: Long): Result<Unit>

    //핀 신고
    suspend fun declarePin(pinId: Long, reasonIndex: Int): Result<Unit>

    //핀 댓글
    suspend fun getPinComments(pinId: Long): Result<List<PinComment>>
    suspend fun createPinComment(pinId: Long, content: String): Result<PinComment>
    suspend fun updatePinComment(commentId: Long, content: String): Result<PinComment>
    suspend fun deletePinComment(commentId: Long): Result<Unit>

    //해결하기 조회
    suspend fun getPinSolve(pinId: Long): Result<PinSolveInfo>

    //시민 해결사
    suspend fun getProblemSolver(pinId: Long, userUid: String): Result<ProblemSolverInfo>
    suspend fun joinProblemSolver(pinId: Long): Result<ProblemSolverJoinInfo>
    suspend fun photoProblemSolver(
        problemSolverId: Long,
        imageUri: String,
    ): Result<ProblemSolverPhotoInfo>
    suspend fun verificationProblemSolver(problemSolverId: Long): Result<ProblemSolverVerificationInfo>

    //청원
    suspend fun getPetition(pinId: Long): Result<PetitionStatusInfo>
    suspend fun joinPetition(pinId: Long): Result<PetitionJoinInfo>
}
