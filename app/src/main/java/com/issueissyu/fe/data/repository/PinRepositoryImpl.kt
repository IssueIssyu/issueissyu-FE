package com.issueissyu.fe.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.issueissyu.fe.data.remote.api.PinApi
import com.issueissyu.fe.data.remote.dto.pin.toPinSolveInfo
import com.issueissyu.fe.data.remote.dto.pin.toPetitionJoinInfo
import com.issueissyu.fe.data.remote.dto.pin.toPetitionStatusInfo
import com.issueissyu.fe.data.remote.dto.pin.toPinOrNull
import com.issueissyu.fe.data.remote.dto.pin.toPinComment
import com.issueissyu.fe.data.remote.dto.pin.toPinEmojiCandidate
import com.issueissyu.fe.data.remote.dto.pin.toPinEmojis
import com.issueissyu.fe.data.remote.dto.pin.toPostSympathyContentOrNull
import com.issueissyu.fe.data.remote.dto.pin.toPinLike
import com.issueissyu.fe.data.remote.dto.pin.toProblemSolverInfo
import com.issueissyu.fe.data.remote.dto.pin.toProblemSolverJoinInfo
import com.issueissyu.fe.data.remote.dto.pin.toProblemSolverPhotoInfo
import com.issueissyu.fe.data.remote.dto.pin.toProblemSolverVerificationInfo
import com.issueissyu.fe.data.remote.dto.pin.toUnsupportedPinTypeMessage
import com.issueissyu.fe.data.remote.dto.request.pin.PinCommentsRequest
import com.issueissyu.fe.data.remote.dto.request.pin.PinDeclarationRequest
import com.issueissyu.fe.data.remote.dto.request.pin.ApplyPinEmojiRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojiDto
import com.issueissyu.fe.data.remote.dto.response.pin.PinEmojisResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinLikeResponse
import com.issueissyu.fe.core.time.parseFlexibleDateTimeToEpochMilli
import com.issueissyu.fe.domain.model.pin.PinComment
import com.issueissyu.fe.domain.model.pin.PinEmojiCandidate
import com.issueissyu.fe.domain.model.pin.PinEmojis
import com.issueissyu.fe.domain.model.pin.PinLike
import com.issueissyu.fe.domain.model.pin.PinPostSympathyContent
import com.issueissyu.fe.domain.model.pin.PinSolveInfo
import com.issueissyu.fe.domain.model.pin.PetitionJoinInfo
import com.issueissyu.fe.domain.model.pin.PetitionStatusInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverJoinInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverPhotoInfo
import com.issueissyu.fe.domain.model.pin.ProblemSolverVerificationInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import com.issueissyu.fe.domain.model.MapPinMarker
import com.issueissyu.fe.data.sample.PinSamples
import com.issueissyu.fe.domain.model.pin.AuthoredPinDetail
import com.issueissyu.fe.domain.model.pin.CommunicationPinDetail
import com.issueissyu.fe.domain.model.pin.CreatePinRequest
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinDetail
import com.issueissyu.fe.domain.model.pin.PinUser
import com.issueissyu.fe.domain.model.pin.UpdatePinRequest
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import com.issueissyu.fe.domain.repository.PinRepository
import java.io.File
import java.util.UUID
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class PinRepositoryImpl @Inject constructor(
    private val pinApi: PinApi,
    @ApplicationContext private val context: Context,
) : PinRepository {

    // TODO: 실제 백엔드와 연결 시 PinSamples 의존을 제거하고 네트워크 호출 로직으로 대체.
    private val dummyPins: MutableList<Pin> = PinSamples.pins.toMutableList()
    private val currentUser: PinUser = PinSamples.user1

    override suspend fun getPins(): List<Pin> {
        // TODO: 실제 백엔드 API 호출로 핀 목록을 가져오도록 구현해야 합니다.
        return dummyPins.toList()
    }

    override suspend fun getPinById(pinId: String): Pin? {
        // TODO: 실제 백엔드 API 호출로 특정 ID의 핀을 가져오도록 구현해야 합니다.
        return dummyPins.find { it.id == pinId }
    }

    override suspend fun getIssuePins(): List<Pin> {
        // TODO: 실제 백엔드 API 호출로 이슈 핀 목록을 가져오도록 구현해야 합니다.
        return dummyPins.filter { it.detail is IssuePinDetail }
    }

    override suspend fun getCommunityPins(): List<Pin> {
        // TODO: 실제 백엔드 API 호출로 커뮤니티 핀 목록을 가져오도록 구현해야 합니다.
        return dummyPins.filter { it.communityPostId != null && (it.detail is IssuePinDetail || it.detail is CommunicationPinDetail) }
    }

    override suspend fun getMyPins(): List<Pin> {
        // TODO: 실제 백엔드 API 호출로 내 핀 목록을 가져오도록 구현해야 합니다.
        return dummyPins.filter { pin ->
            (pin.detail as? AuthoredPinDetail)?.writer?.id == currentUser.id
        }
    }

    override suspend fun createPin(request: CreatePinRequest): Pin {
        val newPinDetail: PinDetail = when (request.category) {
            PinCategory.ISSUE -> IssuePinDetail(writer = currentUser)
            PinCategory.COMMUNICATION -> CommunicationPinDetail(writer = currentUser)
            PinCategory.SHOP, PinCategory.FESTIVAL -> throw IllegalArgumentException("Shop and Festival pins cannot be created by users.")
        }

        val newPin = Pin(
            id = UUID.randomUUID().toString(), // 새 핀은 UUID로 생성
            title = request.title,
            description = request.description,
            coordinate = request.coordinate,
            address = request.address,
            locationName = request.locationName,
            neighborhoodId = request.neighborhoodId,
            neighborhoodName = request.neighborhoodName,
            imageUrls = request.imageUrls,
            createdAt = Instant.now().toString(),
            updatedAt = null,
            detail = newPinDetail
        )
        // TODO: 실제 백엔드 API를 호출하여 핀을 생성하고, 서버로부터 반환된 실제 Pin 객체를 사용해야 합니다.
        dummyPins.add(newPin)
        return newPin
    }

    override suspend fun updatePin(pinId: String, request: UpdatePinRequest): Pin {
        val index = dummyPins.indexOfFirst { it.id == pinId }
        if (index == -1) {
            throw NoSuchElementException("Pin with id $pinId not found.")
        }

        val existingPin = dummyPins[index]

        // 작성자가 있는 핀만 일반 사용자가 수정 가능
        val authoredDetail = existingPin.detail as? AuthoredPinDetail
            ?: throw IllegalArgumentException("This pin type cannot be updated by users.")

        // 커뮤니티 게시물인 경우 수정 불가
        if (existingPin.communityPostId != null) {
            throw IllegalArgumentException("Community posts cannot be updated.")
        }

        // 작성자만 수정 가능
        if (authoredDetail.writer.id != currentUser.id) {
            throw SecurityException("User does not have permission to edit this pin.")
        }

        val updatedPin = existingPin.copy(
            title = request.title,
            description = request.description,
            coordinate = request.coordinate,
            address = request.address,
            locationName = request.locationName,
            neighborhoodId = request.neighborhoodId,
            neighborhoodName = request.neighborhoodName,
            imageUrls = request.imageUrls,
            updatedAt = Instant.now().toString()
        )

        // TODO: 실제 백엔드 API를 호출하여 핀을 업데이트하고, 서버로부터 반환된 실제 Pin 객체를 사용해야 합니다.
        dummyPins[index] = updatedPin
        return updatedPin
    }

    override suspend fun getMapPinsInBounds(bounds: MapBounds): List<MapPinMarker> {
        return dummyPins
            .filter { pin ->
                pin.coordinate.latitude in bounds.swLat..bounds.neLat &&
                pin.coordinate.longitude in bounds.swLng..bounds.neLng
            }
            .map { pin ->
                MapPinMarker(
                    pinId = pin.id,
                    category = pin.category,
                    coordinate = pin.coordinate,
                    address = pin.address,
                    locationName = pin.locationName ?: pin.address
                )
            }
    }

    override suspend fun getPinDetailHome(pinId: Long): Result<Pin> {
        return try {
            val response = pinApi.pinHome(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "핀 상세 홈 응답이 올바르지 않습니다." },
                        ),
                    )
                val pin = result.toPinOrNull()
                    ?: return Result.failure(Exception(result.pinType.toUnsupportedPinTypeMessage()))
                Result.success(pin)
            } else {
                when (response.code) {
                    "PIN_HOME_404" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "존재하지 않는 핀 입니다." },
                            ),
                        )

                    "PIN_HOME_400" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 상세 홈 조회 API를 실행 할 수 없습니다." },
                            ),
                        )

                    else ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 상세 홈 조회에 실패했습니다." },
                            ),
                        )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPinDetailPost(pinId: Long): Result<PinPostSympathyContent?> {
        return try {
            val response = pinApi.pinPost(pinId)
            if (response.isSuccess) {
                val result = response.result ?: return Result.success(null)
                Result.success(result.toPostSympathyContentOrNull())
            } else {
                when (response.code) {
                    "PIN_POST_404" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "존재하지 않는 핀 입니다." },
                            ),
                        )

                    "PIN_POST_400" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 상세 포스트 조회 API를 실행할 수 없습니다." },
                            ),
                        )

                    else ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 상세 포스트 조회에 실패했습니다." },
                            ),
                        )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun likePin(pinId: Long): Result<PinLike> {
        return try {
            val response = pinApi.pinLike(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "핀 공감 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toPinLike())
            } else {
                when (response.code) {
                    "PIN_LIKE_400_1" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "이미 공감된 핀입니다." },
                            ),
                        )

                    "PIN_LIKE_404" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "존재하지 않는 핀 입니다." },
                            ),
                        )

                    "PIN_LIKE_500" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 공감하기 중 서버 오류가 발생했습니다." },
                            ),
                        )

                    else ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 공감에 실패했습니다." },
                            ),
                        )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 이모지 조회
    override suspend fun getPinEmojis(pinId: Long): Result<PinEmojis> {
        return try {
            val response = pinApi.getPinEmojis(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.success(PinEmojis(selectedEmojiId = null, emojis = emptyList()))
                Result.success(result.toPinEmojis())
            } else {
                when (response.code) {
                    "PIN_NOT_FOUND_404" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "존재하지 않는 핀입니다." },
                            ),
                        )

                    "JWT_401" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "인증이 필요합니다." },
                            ),
                        )

                    else ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 반응 목록 조회에 실패했습니다." },
                            ),
                        )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEmojiCandidates(): Result<List<PinEmojiCandidate>> {
        return try {
            val response = pinApi.getEmojiCandidates()
            if (response.isSuccess) {
                Result.success(response.result.orEmpty().mapNotNull { dto -> dto.toPinEmojiCandidate() })
            } else {
                Result.failure(Exception(response.message.ifBlank { "이모지 목록 조회에 실패했습니다." }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun applyPinEmojiFromPicker(pinId: Long, emojiId: Long?): Result<Long?> {
        return applyPinEmojiInternal(
            pinId = pinId,
            emojiId = emojiId,
            apiCall = { id, request ->
                pinApi.applyPinEmojiPicker(pinId = id, request = request)
            },
        )
    }

    override suspend fun togglePinEmojiFromList(pinId: Long, emojiId: Long): Result<Long?> {
        return applyPinEmojiInternal(
            pinId = pinId,
            emojiId = emojiId,
            apiCall = { id, request ->
                pinApi.applyPinEmojiList(pinId = id, request = request)
            },
        )
    }

    private suspend fun applyPinEmojiInternal(
        pinId: Long,
        emojiId: Long?,
        apiCall: suspend (Long, ApplyPinEmojiRequest) -> com.issueissyu.fe.data.remote.dto.response.BaseResponse<com.issueissyu.fe.data.remote.dto.response.pin.ApplyPinEmojiResponse?>,
    ): Result<Long?> {
        return try {
            val response = apiCall(pinId, ApplyPinEmojiRequest(emojiId = emojiId))
            if (response.isSuccess) {
                Result.success(response.result?.selectedEmojiId)
            } else {
                Result.failure(Exception(resolveApplyPinEmojiErrorMessage(response.code, response.message)))
            }
        } catch (e: Exception) {
            Result.failure(
                Exception(
                    e.message?.takeIf { it.isNotBlank() } ?: "이모지 반응 처리에 실패했습니다.",
                    e,
                ),
            )
        }
    }

    private fun resolveApplyPinEmojiErrorMessage(code: String, message: String): String {
        return when (code) {
            "PIN_NOT_FOUND_404" ->
                message.ifBlank { "존재하지 않는 핀입니다." }
            "EMOJI_NOT_FOUND_404_1" ->
                message.ifBlank { "존재하지 않는 이모지입니다." }
            "EMOJI_NOT_OWNED_403_1" ->
                message.ifBlank { "구매하지 않은 이모지입니다." }
            else ->
                message.ifBlank { "이모지 반응 처리에 실패했습니다." }
        }
    }

    private fun PinLikeResponse.toPinLike(): PinLike {
        return PinLike(
            pinId = pinId,
            pinLikeCount = pinLikeCount,
            isLike = isLike,
        )
    }

    // 핀 삭제
    override suspend fun deletePin(pinId: Long): Result<Unit> {
        return try {
            val response = pinApi.pinDelete(pinId)
            if (response.isSuccess) {
                Result.success(Unit)
            } else {
                when (response.code) {
                    "PIN_DELETE_400_1" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "등업된 이슈 핀은 삭제가 불가능 합니다." },
                            ),
                        )

                    "PIN_DELETE_400_2" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "존재하지 않는 핀 입니다." },
                            ),
                        )

                    "PIN_DELETE_400_3" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 작성자가 아니므로 삭제 권한이 없습니다." },
                            ),
                        )

                    "PIN_DELETE_400_4" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 삭제 API를 실행할 수 없습니다." },
                            ),
                        )

                    else ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 삭제에 실패했습니다." },
                            ),
                        )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //핀 신고
    override suspend fun declarePin(pinId: Long, reasonIndex: Int): Result<Unit> {
        return try {
            val response = pinApi.pinDeclare(
                pinId = pinId,
                request = PinDeclarationRequest(reasonIndex=reasonIndex)
            )
            if (response.isSuccess){
                Result.success(Unit)
            } else {
                when (response.code){
                    "PIN_DECLARATION_404_1" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "존재하지 않는 핀 입니다." }
                            )
                        )

                    "PIN_DECLARATION_409_1" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "이미 신고한 핀입니다." }
                            )
                        )

                    else ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank {"핀 신고에 실패했습니다."}
                            )
                        )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPinComments(pinId: Long): Result<List<PinComment>> {
        return try {
            val response = pinApi.getPinComments(pinId)
            if (response.isSuccess) {
                Result.success(
                    response.result.orEmpty()
                        .map { it.toPinComment() }
                        .sortedBy { parsePinCommentInstant(it.createdAt) },
                )
            } else {
                when (response.code) {
                    "PIN_NOT_FOUND_404" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "존재하지 않는 핀입니다." },
                            ),
                        )

                    else ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 댓글 목록 조회에 실패했습니다." },
                            ),
                        )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPinComment(pinId: Long, content: String): Result<PinComment> {
        return try {
            val response = pinApi.createPinComments(
                pinId = pinId,
                request = PinCommentsRequest(commentContent = content),
            )
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "핀 댓글 작성 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toPinComment())
            } else {
                when (response.code) {
                    "PIN_400" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "필수 값이 누락되었습니다." },
                            ),
                        )

                    "PIN_401" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "인증이 필요합니다." },
                            ),
                        )

                    "PIN_404" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "존재하지 않는 핀입니다." },
                            ),
                        )

                    else ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 댓글 작성에 실패했습니다." },
                            ),
                        )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePinComment(commentId: Long, content: String): Result<PinComment> {
        return try {
            val response = pinApi.updatePinComments(
                commentId = commentId,
                request = PinCommentsRequest(commentContent = content),
            )
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "핀 댓글 수정 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toPinComment())
            } else {
                when (response.code) {
                    "PIN_403" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "댓글 수정 권한이 없습니다." },
                            ),
                        )

                    "COMMENT_NOT_FOUND_404_3" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "존재하지 않는 댓글입니다." },
                            ),
                        )

                    else ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 댓글 수정에 실패했습니다." },
                            ),
                        )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePinComment(commentId: Long): Result<Unit> {
        return try {
            val response = pinApi.deletePinComments(commentId)
            if (response.isSuccess) {
                Result.success(Unit)
            } else {
                when (response.code) {
                    "PIN_403" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "댓글 삭제 권한이 없습니다." },
                            ),
                        )

                    "PIN_404" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "존재하지 않는 댓글입니다." },
                            ),
                        )

                    else ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 댓글 삭제에 실패했습니다." },
                            ),
                        )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //해결하기 조회
    override suspend fun getPinSolve(pinId: Long): Result<PinSolveInfo> {
        return try {
            val response = pinApi.getPinSolve(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "핀 상세 해결하기 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toPinSolveInfo())
            } else {
                when (response.code) {
                    "PIN_SOLVE_404" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "존재하지 않는 핀 입니다." },
                            ),
                        )

                    "PIN_SOLVE_400" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 상세 해결하기 조회 API를 실행 할 수 없습니다." },
                            ),
                        )

                    else ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "핀 상세 해결하기 조회에 실패했습니다." },
                            ),
                        )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //시민 해결사
    override suspend fun joinProblemSolver(pinId: Long): Result<ProblemSolverJoinInfo> {
        return try {
            val response = pinApi.joinProblemSolver(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "시민 해결사 참여 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toProblemSolverJoinInfo())
            } else {
                Result.failure(problemSolverJoinException(response.code, response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProblemSolver(pinId: Long, userUid: String): Result<ProblemSolverInfo> {
        return try {
            val response = pinApi.getProblemSolver(pinId = pinId, userUid = userUid)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "시민 해결사 조회 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toProblemSolverInfo())
            } else {
                Result.failure(problemSolverGetException(response.code, response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun photoProblemSolver(
        problemSolverId: Long,
        imageUri: String,
    ): Result<ProblemSolverPhotoInfo> {
        return try {
            val photoPart = createProblemSolverPhotoPart(context, imageUri)
                .getOrElse { return Result.failure(it) }
            val response = pinApi.photoProblemSolver(
                problemSolverId = problemSolverId,
                photo = photoPart,
            )
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "시민 해결사 사진 첨부 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toProblemSolverPhotoInfo())
            } else {
                Result.failure(problemSolverPhotoException(response.code, response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verificationProblemSolver(problemSolverId: Long): Result<ProblemSolverVerificationInfo> {
        return try {
            val response = pinApi.verificationProblemSolver(problemSolverId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "시민 해결사 인증 완료 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toProblemSolverVerificationInfo())
            } else {
                Result.failure(problemSolverVerificationException(response.code, response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //청원하기
    override suspend fun getPetition(pinId: Long): Result<PetitionStatusInfo> {
        return try {
            val response = pinApi.getIssuePetition(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "청원 현황 조회 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toPetitionStatusInfo())
            } else {
                Result.failure(petitionStatusException(response.code, response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinPetition(pinId: Long): Result<PetitionJoinInfo> {
        return try {
            val response = pinApi.joinIssuePetition(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "청원 등록 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toPetitionJoinInfo())
            } else {
                Result.failure(petitionJoinException(response.code, response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private suspend fun createProblemSolverPhotoPart(
    context: Context,
    imageUri: String,
): Result<MultipartBody.Part> = withContext(Dispatchers.IO) {
    runCatching {
        val uri = Uri.parse(imageUri)
        val mimeType = context.contentResolver.getType(uri)?.takeIf { it.isNotBlank() } ?: "image/jpeg"
        val fileName = resolveUploadFileName(context, uri)
        val photoBytes = readUploadBytes(context, uri, imageUri)
            ?: throw IllegalArgumentException("첨부한 이미지 파일을 읽을 수 없습니다.")
        val requestBody = photoBytes.toRequestBody(mimeType.toMediaType())
        MultipartBody.Part.createFormData(
            name = "photo",
            filename = fileName,
            body = requestBody,
        )
    }
}

private fun readUploadBytes(context: Context, uri: Uri, rawImageUri: String): ByteArray? {
    return when (uri.scheme?.lowercase()) {
        null -> File(rawImageUri).takeIf { it.exists() }?.readBytes()
        "file" -> uri.path?.let { File(it) }?.takeIf { it.exists() }?.readBytes()
        else -> context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    }
}

private fun resolveUploadFileName(context: Context, uri: Uri): String {
    if (uri.scheme == "content") {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                cursor.getString(index)?.takeIf { it.isNotBlank() }?.let { return it }
            }
        }
    }
    return uri.lastPathSegment
        ?.substringAfterLast('/')
        ?.takeIf { it.isNotBlank() }
        ?: "problem-solver-photo.jpg"
}

private fun problemSolverJoinException(code: String, message: String): Exception {
    return Exception(
        message.ifBlank {
            when (code) {
                "GO_NOW_400_1" -> "이미 시민해결사로 참여한 핀입니다."
                "GO_NOW_400_2" -> "시민해결사 참여가 가능한 핀 종류가 아닙니다."
                "GO_NOW_404" -> "존재하지 않는 핀입니다."
                else -> "시민해결사 참여에 실패했습니다."
            }
        },
    )
}

private fun problemSolverGetException(code: String, message: String): Exception {
    return Exception(
        message.ifBlank {
            when (code) {
                "PROBLEM_SOLVER_404_1" -> "존재하지 않는 핀입니다."
                "PROBLEM_SOLVER_404_2" -> "존재하지 않는 사용자입니다."
                else -> "시민해결사 조회에 실패했습니다."
            }
        },
    )
}

private fun problemSolverPhotoException(code: String, message: String): Exception {
    return Exception(
        message.ifBlank {
            when (code) {
                "PROBLEM_SOLVER_PHOTO_404" -> "존재하지 않는 시민해결사 입니다."
                "PROBLEM_SOLVER_PHOTO_400_1" -> "첨부한 사진 용량이 너무 큽니다."
                "PROBLEM_SOLVER_PHOTO_400_2" -> "시민해결사 사진 첨부에 실패했습니다."
                else -> "시민해결사 사진 첨부에 실패했습니다."
            }
        },
    )
}

private fun problemSolverVerificationException(code: String, message: String): Exception {
    return Exception(
        message.ifBlank {
            when (code) {
                "PROBLEM_SOLVER_CHECK_404" -> "존재하지 않는 시민해결사 입니다."
                "PROBLEM_SOLVER_CHECK_400_1" -> "인증 가능한 진행 상태가 아닙니다."
                "PROBLEM_SOLVER_CHECK_400_2" -> "내 핀 시민해결사 인증에 실패했습니다."
                else -> "시민해결사 인증에 실패했습니다."
            }
        },
    )
}

private fun petitionStatusException(code: String, message: String): Exception {
    return Exception(
        message.ifBlank {
            when (code) {
                "PETITION_STATUS_404", "PETITION_STATUS_404_1" -> "청원 현황을 조회할 수 없는 핀입니다."
                else -> "청원 현황 조회에 실패했습니다."
            }
        },
    )
}

private fun petitionJoinException(code: String, message: String): Exception {
    return Exception(
        message.ifBlank {
            when (code) {
                "PETITION_400_1" -> "이미 청원되었습니다."
                "PETITION_400_2" -> "청원이 가능한 핀 종류가 아닙니다."
                "PETITION_404" -> "존재하지 않는 핀 입니다."
                else -> "청원 등록에 실패했습니다."
            }
        },
    )
}

private fun parsePinCommentInstant(raw: String): Long =
    parseFlexibleDateTimeToEpochMilli(raw)
