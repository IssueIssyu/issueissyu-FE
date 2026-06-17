package com.issueissyu.fe.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.gson.Gson
import com.issueissyu.fe.data.remote.api.AiIssueApiService
import com.issueissyu.fe.data.remote.api.PinApi
import com.issueissyu.fe.data.remote.dto.pin.toPinSolveInfo
import com.issueissyu.fe.data.remote.dto.pin.toPetitionJoinInfo
import com.issueissyu.fe.data.remote.dto.pin.toPetitionStatusInfo
import com.issueissyu.fe.data.remote.dto.pin.toIssuePinEditResult
import com.issueissyu.fe.data.remote.dto.pin.toPinEditRateLimitQuota
import com.issueissyu.fe.data.remote.dto.pin.toPinOrNull
import com.issueissyu.fe.data.remote.dto.pin.toPinComment
import com.issueissyu.fe.data.remote.dto.pin.toPinEmojiCandidate
import com.issueissyu.fe.data.remote.dto.pin.toPinEmojis
import com.issueissyu.fe.data.remote.dto.pin.toPostSympathyContentOrNull
import com.issueissyu.fe.data.remote.dto.pin.toProblemSolverInfo
import com.issueissyu.fe.data.remote.dto.pin.toProblemSolverJoinInfo
import com.issueissyu.fe.data.remote.dto.pin.toProblemSolverPhotoInfo
import com.issueissyu.fe.data.remote.dto.pin.toProblemSolverVerificationInfo
import com.issueissyu.fe.data.remote.dto.pin.toUnsupportedPinTypeMessage
import com.issueissyu.fe.data.remote.dto.request.pin.PinCommentsRequest
import com.issueissyu.fe.data.remote.dto.request.pin.PinDeclarationRequest
import com.issueissyu.fe.data.remote.dto.request.pin.ApplyPinEmojiRequest
import com.issueissyu.fe.data.remote.dto.request.pin.CommunicationPinImportRequest
import com.issueissyu.fe.data.remote.dto.request.pin.toIssuePinEditRequest
import com.issueissyu.fe.data.remote.dto.request.pin.IssuePinImportRequest
import com.issueissyu.fe.data.remote.dto.request.pin.PinImageItemRequest
import com.issueissyu.fe.data.remote.dto.response.BaseResponse
import com.issueissyu.fe.data.remote.dto.response.pin.GoNowResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PetitionStatusResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PetitionSubmitResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinImportResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinLikeResponse
import com.issueissyu.fe.data.remote.dto.response.pin.PinSolveResponse
import com.issueissyu.fe.core.time.parseFlexibleDateTimeToEpochMilli
import com.issueissyu.fe.domain.model.pin.PinComment
import com.issueissyu.fe.domain.model.pin.PetitionStatus
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
import com.issueissyu.fe.domain.model.pin.IssuePinEditResult
import com.issueissyu.fe.domain.model.pin.PinDetailHomeResult
import com.issueissyu.fe.domain.model.pin.PinEditRateLimitQuota
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.pin.GoNow
import com.issueissyu.fe.domain.model.pin.PetitionSubmit
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinSolveStatus
import com.issueissyu.fe.domain.model.pin.PinUser
import com.issueissyu.fe.domain.model.pin.UpdatePinRequest
import com.issueissyu.fe.domain.model.pin.PinHomeEditSubmitResult
import com.issueissyu.fe.domain.model.pin.UpdatePinEditRequest
import java.time.Instant
import com.issueissyu.fe.core.network.ApiErrorMapper
import com.issueissyu.fe.core.media.PinImageMimeResolver
import com.issueissyu.fe.core.media.PinImageUploadDiagnostics
import com.issueissyu.fe.core.media.PinImageUploadMeta
import com.issueissyu.fe.core.media.PinImageUploadValidator
import com.issueissyu.fe.domain.model.pin.PinCreateException
import com.issueissyu.fe.domain.repository.PinRepository
import java.io.File
import java.util.UUID
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@Singleton
class PinRepositoryImpl @Inject constructor(
    private val pinApi: PinApi,
    private val aiIssueApiService: AiIssueApiService,
    private val gson: Gson,
    private val apiErrorMapper: ApiErrorMapper,
    @ApplicationContext private val context: Context,
) : PinRepository {

    private suspend fun <T> failureFrom(e: Exception, fallback: String): Result<T> =
        Result.failure(apiErrorMapper.toException(e, fallback))

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

    override suspend fun createPin(request: CreatePinRequest): Result<Pin> = withContext(Dispatchers.IO) {
        var uploadMetas = emptyList<PinImageUploadMeta>()
        try {
            if (request.category == PinCategory.ISSUE && request.imageUris.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("이슈 핀은 사진을 최소 1장 첨부해야 합니다."))
            }
            PinImageUploadValidator.validate(context, request.imageUris).getOrThrow()
            val multipart = request.imageUris.toMultipartParts()
            uploadMetas = multipart.uploadMetas
            if (uploadMetas.isNotEmpty()) {
                PinImageUploadDiagnostics.logUploadPrepare(
                    category = request.category.name,
                    metas = uploadMetas,
                )
            }
            val response = when (request.category) {
                PinCategory.ISSUE -> aiIssueApiService.createIssuePin(
                    request = gson.toJson(request.toIssuePinImportRequest()).toJsonPart(),
                    photos = multipart.parts,
                )
                PinCategory.COMMUNICATION -> pinApi.createCommunicationPin(
                    request = gson.toJson(request.toCommunicationPinImportRequest()).toJsonPart(),
                    photos = multipart.parts,
                )
                PinCategory.SHOP,
                PinCategory.FESTIVAL -> return@withContext Result.failure(
                    IllegalArgumentException("가게와 축제 핀은 일반 사용자가 생성할 수 없습니다."),
                )
            }

            if (response.isSuccess) {
                Result.success(response.toCreatedPin(request))
            } else {
                if (request.imageUris.isNotEmpty()) {
                    PinImageUploadDiagnostics.logUploadFailure(
                        httpStatus = null,
                        serverCode = response.code,
                        metas = uploadMetas,
                        responseBody = gson.toJson(response),
                    )
                }
                Result.failure(
                    toPinCreateException(
                        code = response.code,
                        message = response.message,
                    ),
                )
            }
        } catch (e: HttpException) {
            val rawBody = apiErrorMapper.readHttpErrorBody(e)
            val errorEnvelope = apiErrorMapper.parseErrorBody(rawBody, PinCreateErrorEnvelope::class.java)
            if (request.imageUris.isNotEmpty()) {
                PinImageUploadDiagnostics.logUploadFailure(
                    httpStatus = e.code(),
                    serverCode = errorEnvelope?.code,
                    metas = uploadMetas,
                    responseBody = rawBody.takeIf { it.isNotBlank() },
                )
            }
            Result.failure(
                toPinCreateException(
                    code = errorEnvelope?.code,
                    message = errorEnvelope?.message,
                    fallbackMessage = "핀 생성에 실패했습니다.",
                ),
            )
        } catch (e: Exception) {
            if (request.imageUris.isNotEmpty() && uploadMetas.isNotEmpty()) {
                PinImageUploadDiagnostics.logUploadFailure(
                    httpStatus = null,
                    serverCode = e.javaClass.simpleName,
                    metas = uploadMetas,
                )
            }
            failureFrom(e, "핀 생성에 실패했습니다.")
        }
    }

    private data class PinCreateErrorEnvelope(
        val code: String = "",
        val message: String = "",
    )

    private data class PinMultipartUpload(
        val parts: List<MultipartBody.Part>,
        val uploadMetas: List<PinImageUploadMeta>,
    )

    private fun toPinCreateException(
        code: String?,
        message: String?,
        fallbackMessage: String = "핀 생성에 실패했습니다.",
    ): PinCreateException {
        val normalizedCode = code?.takeIf { it.isNotBlank() }
        val normalizedMessage = message?.takeIf { it.isNotBlank() }
        val isImageRelated = normalizedCode?.startsWith("PIN_IMAGE") == true
        val userMessage = when (normalizedCode) {
            "PIN_IMAGE_400_2" ->
                normalizedMessage?.ifBlank { null }
                    ?: "사진 첨부에 실패했습니다. 다른 사진으로 다시 시도해주세요."
            else ->
                normalizedMessage ?: fallbackMessage
        }
        return PinCreateException(
            message = userMessage,
            isImageRelated = isImageRelated,
            serverCode = normalizedCode,
        )
    }

    private fun CreatePinRequest.toCommunicationPinImportRequest(): CommunicationPinImportRequest {
        val mainUri = mainImageUri?.takeIf { imageUris.contains(it) } ?: imageUris.firstOrNull()
        val pinImages = imageUris.takeIf { it.isNotEmpty() }?.map { uri ->
            PinImageItemRequest(isMain = uri == mainUri)
        }

        return CommunicationPinImportRequest(
            lat = coordinate.latitude,
            lng = coordinate.longitude,
            pinImages = pinImages,
            pinTitle = title,
            pinContent = description,
        )
    }

    private fun CreatePinRequest.toIssuePinImportRequest(): IssuePinImportRequest {
        val mainUri = mainImageUri?.takeIf { imageUris.contains(it) } ?: imageUris.firstOrNull()
        val pinImages = imageUris.takeIf { it.isNotEmpty() }?.map { uri ->
            PinImageItemRequest(isMain = uri == mainUri)
        }

        return IssuePinImportRequest(
            lat = coordinate.latitude,
            lng = coordinate.longitude,
            pinTitle = title,
            pinContent = description,
            pinImages = pinImages,
        )
    }

    private fun BaseResponse<PinImportResponse?>.toCreatedPin(request: CreatePinRequest): Pin {
        val result = result
        val detail = when (request.category) {
            PinCategory.ISSUE -> IssuePinDetail(writer = currentUser)
            PinCategory.COMMUNICATION -> CommunicationPinDetail(writer = currentUser)
            PinCategory.SHOP, PinCategory.FESTIVAL -> error("Unsupported created pin category.")
        }
        return Pin(
            id = result?.pinId?.takeIf { it > 0 }?.toString() ?: UUID.randomUUID().toString(),
            title = request.title,
            description = request.description,
            coordinate = request.coordinate,
            address = result?.pinDetailAddress?.takeIf { it.isNotBlank() } ?: request.address,
            locationName = request.locationName,
            neighborhoodId = request.neighborhoodId,
            neighborhoodName = result?.region ?: request.neighborhoodName,
            imageUrls = result?.pinImageUrls.orEmpty().mapNotNull { it.pinImageUrl.takeIf { url -> url.isNotBlank() } },
            createdAt = result?.createdAt ?: Instant.now().toString(),
            updatedAt = result?.updatedAt,
            detail = detail,
        )
    }

    private fun String.toJsonPart(): RequestBody = toRequestBody("application/json".toMediaType())

    private fun List<String>.toMultipartParts(): PinMultipartUpload {
        val parts = mutableListOf<MultipartBody.Part>()
        val uploadMetas = mutableListOf<PinImageUploadMeta>()
        forEachIndexed { index, uriString ->
            val uri = Uri.parse(uriString)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalArgumentException("첨부한 사진을 읽을 수 없습니다.")

            val fileName = resolveUploadFileName(context, uri)
            val mimeResolution = PinImageMimeResolver.resolve(context, uri, fileName, bytes)
            PinImageUploadDiagnostics.logMimeFallback(
                resolution = mimeResolution,
                fileName = fileName,
                uriScheme = uri.scheme.orEmpty(),
                bytes = bytes,
            )
            val uploadFilename = PinImageMimeResolver.ensureExtension(fileName, mimeResolution.mimeType)
                .ifBlank { "pin_image_${index + 1}.jpg" }
            uploadMetas += PinImageUploadMeta(
                uriScheme = uri.scheme.orEmpty(),
                fileName = fileName,
                uploadFilename = uploadFilename,
                byteSize = bytes.size,
                resolvedMime = mimeResolution.mimeType,
                mimeSource = mimeResolution.source,
                contentResolverMime = mimeResolution.contentResolverMime,
                header = PinImageUploadDiagnostics.formatHeaderHex(bytes),
            )
            val mediaType = mimeResolution.mimeType.toMediaTypeOrNull()
                ?: "image/jpeg".toMediaType()
            parts += MultipartBody.Part.createFormData(
                name = "photos",
                filename = uploadFilename,
                body = bytes.toRequestBody(mediaType),
            )
        }
        return PinMultipartUpload(parts = parts, uploadMetas = uploadMetas)
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

    override suspend fun updatePinHomeEdit(
        pinId: Long,
        category: PinCategory,
        request: UpdatePinEditRequest,
        existingPin: Pin?,
    ): Result<PinHomeEditSubmitResult> = withContext(Dispatchers.IO) {
        when (category) {
            PinCategory.ISSUE -> executePinHomeEditUpload(
                request = request,
                category = category,
                isCommunicationPin = false,
                fallbackMessage = "이슈 핀 수정에 실패했습니다.",
            ) {
                val response = aiIssueApiService.editIssuePin(
                    pinId = pinId,
                    request = it.jsonPart,
                    photos = it.photoParts,
                )
                if (!response.isSuccess) {
                    return@executePinHomeEditUpload Result.failure(
                        pinEditException(false, response.code, response.message),
                    )
                }
                val result = response.result
                    ?: return@executePinHomeEditUpload Result.failure(
                        pinEditException(
                            isCommunicationPin = false,
                            code = response.code,
                            message = response.message.ifBlank { "이슈 핀 수정 응답이 올바르지 않습니다." },
                        ),
                    )
                val editResult = result.toIssuePinEditResult()
                    ?: return@executePinHomeEditUpload Result.failure(
                        pinEditException(
                            isCommunicationPin = false,
                            code = response.code,
                            message = result.pinType.toUnsupportedPinTypeMessage(),
                        ),
                    )
                Result.success(PinHomeEditSubmitResult.Issue(editResult))
            }

            PinCategory.COMMUNICATION -> executePinHomeEditUpload(
                request = request,
                category = category,
                isCommunicationPin = true,
                fallbackMessage = "소통 핀 수정에 실패했습니다.",
            ) {
                val response = pinApi.pinCommunicationEdit(
                    pinId = pinId,
                    request = it.jsonPart,
                    photos = it.photoParts,
                )
                if (!response.isSuccess) {
                    return@executePinHomeEditUpload Result.failure(
                        pinEditException(true, response.code, response.message),
                    )
                }
                val result = response.result
                    ?: return@executePinHomeEditUpload Result.failure(
                        pinEditException(
                            isCommunicationPin = true,
                            code = response.code,
                            message = response.message.ifBlank { "소통 핀 수정 응답이 올바르지 않습니다." },
                        ),
                    )
                val pin = result.toPinOrNull(preserveFrom = existingPin)
                    ?: return@executePinHomeEditUpload Result.failure(
                        pinEditException(
                            isCommunicationPin = true,
                            code = response.code,
                            message = result.pinType.toUnsupportedPinTypeMessage(),
                        ),
                    )
                Result.success(PinHomeEditSubmitResult.Communication(pin))
            }

            PinCategory.SHOP,
            PinCategory.FESTIVAL,
            -> Result.failure(Exception("수정할 수 없는 핀 유형입니다."))
        }
    }

    private data class PreparedPinHomeEditUpload(
        val jsonPart: RequestBody,
        val photoParts: List<MultipartBody.Part>,
        val uploadMetas: List<PinImageUploadMeta>,
    )

    private suspend fun <T> executePinHomeEditUpload(
        request: UpdatePinEditRequest,
        category: PinCategory,
        isCommunicationPin: Boolean,
        fallbackMessage: String,
        submit: suspend (PreparedPinHomeEditUpload) -> Result<T>,
    ): Result<T> {
        var uploadMetas = emptyList<PinImageUploadMeta>()
        return try {
            if (request.newImageUris.isNotEmpty()) {
                PinImageUploadValidator.validate(context, request.newImageUris).getOrThrow()
            }
            val multipart = request.newImageUris.toMultipartParts()
            uploadMetas = multipart.uploadMetas
            if (uploadMetas.isNotEmpty()) {
                PinImageUploadDiagnostics.logUploadPrepare(
                    category = category.name,
                    metas = uploadMetas,
                )
            }
            val payload = PreparedPinHomeEditUpload(
                jsonPart = gson.toJson(request.toIssuePinEditRequest()).toJsonPart(),
                photoParts = multipart.parts,
                uploadMetas = uploadMetas,
            )
            submit(payload).also { result ->
                if (result.isFailure && request.newImageUris.isNotEmpty()) {
                    val error = result.exceptionOrNull()
                    PinImageUploadDiagnostics.logUploadFailure(
                        httpStatus = null,
                        serverCode = (error as? PinEditApiException)?.serverCode
                            ?: error?.javaClass?.simpleName,
                        metas = uploadMetas,
                    )
                }
            }
        } catch (e: HttpException) {
            val rawBody = apiErrorMapper.readHttpErrorBody(e)
            val errorEnvelope = apiErrorMapper.parseErrorBody(rawBody, PinCreateErrorEnvelope::class.java)
            if (request.newImageUris.isNotEmpty()) {
                PinImageUploadDiagnostics.logUploadFailure(
                    httpStatus = e.code(),
                    serverCode = errorEnvelope?.code,
                    metas = uploadMetas,
                    responseBody = rawBody.takeIf { it.isNotBlank() },
                )
            }
            Result.failure(
                pinEditException(
                    isCommunicationPin = isCommunicationPin,
                    code = errorEnvelope?.code,
                    message = errorEnvelope?.message,
                ),
            )
        } catch (e: Exception) {
            if (request.newImageUris.isNotEmpty() && uploadMetas.isNotEmpty()) {
                PinImageUploadDiagnostics.logUploadFailure(
                    httpStatus = null,
                    serverCode = e.javaClass.simpleName,
                    metas = uploadMetas,
                )
            }
            failureFrom(e, fallbackMessage)
        }
    }

    override suspend fun getIssuePinEditQuota(pinId: Long): Result<PinEditRateLimitQuota> {
        return try {
            val response = aiIssueApiService.getIssuePinEditQuota(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "이슈 핀 수정 제한 횟수 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toPinEditRateLimitQuota())
            } else {
                when (response.code) {
                    "COMMON_403" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "본인이 작성한 핀만 수정할 수 있습니다." },
                            ),
                        )

                    "ISSUE_4041" ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "존재하지 않는 핀이거나 이슈 핀이 아닙니다." },
                            ),
                        )

                    else ->
                        Result.failure(
                            Exception(
                                response.message.ifBlank { "이슈 핀 수정 제한 횟수 조회에 실패했습니다." },
                            ),
                        )
                }
            }
        } catch (e: Exception) {
            failureFrom(e, "이슈 핀 수정 제한 횟수 조회에 실패했습니다.")
        }
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

    override suspend fun getPinDetailHome(pinId: Long): Result<PinDetailHomeResult> {
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
                Result.success(
                    PinDetailHomeResult(
                        pin = pin,
                        editRateLimitQuota = result.rateLimitQuota?.toPinEditRateLimitQuota(),
                    ),
                )
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
            failureFrom(e, "핀 상세 홈 조회에 실패했습니다.")
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
            failureFrom(e, "핀 상세 포스트 조회에 실패했습니다.")
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
            failureFrom(e, "핀 공감에 실패했습니다.")
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
            failureFrom(e, "핀 반응 목록 조회에 실패했습니다.")
        }
    }

    override suspend fun getPetitionStatus(pinId: Long): Result<PetitionStatus> {
        return try {
            val response = pinApi.getPetitionStatus(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "청원 현황 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toPetitionStatus())
            } else {
                Result.failure(Exception(response.message.ifBlank { "청원 현황 조회에 실패했습니다." }))
            }
        } catch (e: Exception) {
            failureFrom(e, "청원 현황 조회에 실패했습니다.")
        }
    }

    override suspend fun getPinSolveStatus(pinId: Long): Result<PinSolveStatus> {
        return try {
            val response = pinApi.getPinSolveStatus(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "이슈 해결 상태 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toPinSolveStatus())
            } else {
                Result.failure(Exception(response.message.ifBlank { "이슈 해결 상태 조회에 실패했습니다." }))
            }
        } catch (e: Exception) {
            failureFrom(e, "이슈 해결 상태 조회에 실패했습니다.")
        }
    }

    override suspend fun submitPetition(pinId: Long): Result<PetitionSubmit> {
        return try {
            val response = pinApi.submitPetition(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "청원하기 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toPetitionSubmit())
            } else {
                Result.failure(Exception(response.message.ifBlank { "청원하기에 실패했습니다." }))
            }
        } catch (e: Exception) {
            failureFrom(e, "청원하기에 실패했습니다.")
        }
    }

    override suspend fun goNow(pinId: Long): Result<GoNow> {
        return try {
            val response = pinApi.goNow(pinId)
            if (response.isSuccess) {
                val result = response.result
                    ?: return Result.failure(
                        Exception(
                            response.message.ifBlank { "지금가요 응답이 올바르지 않습니다." },
                        ),
                    )
                Result.success(result.toGoNow())
            } else {
                Result.failure(Exception(response.message.ifBlank { "지금가요 참여에 실패했습니다." }))
            }
        } catch (e: Exception) {
            failureFrom(e, "지금가요 참여에 실패했습니다.")
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
            failureFrom(e, "이모지 목록 조회에 실패했습니다.")
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
            failureFrom(e, "이모지 반응 처리에 실패했습니다.")
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

    private fun PinSolveResponse.toPinSolveStatus(): PinSolveStatus {
        return PinSolveStatus(
            isPetitioned = isPetitioned ?: false,
            isProblemSolver = isProblemSolver,
            reliability = reliability,
        )
    }

    private fun PetitionSubmitResponse.toPetitionSubmit(): PetitionSubmit {
        return PetitionSubmit(
            pinId = pinId,
            petitionCount = petitionCount,
            isPetitioned = isPetitioned,
        )
    }

    private fun GoNowResponse.toGoNow(): GoNow {
        return GoNow(
            pinId = pinId,
            problemSolverId = problemSolverId,
            problemSolveState = problemSolveState,
        )
    }

    private fun PetitionStatusResponse.toPetitionStatus(): PetitionStatus {
        return PetitionStatus(
            pinId = pinId,
            petitionCount = petitionCount,
            isPetitioned = isPetitioned,
            targetPetition = targetPetition,
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
            failureFrom(e, "핀 삭제에 실패했습니다.")
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
            failureFrom(e, "핀 신고에 실패했습니다.")
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
            failureFrom(e, "핀 댓글 목록 조회에 실패했습니다.")
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
            failureFrom(e, "핀 댓글 작성에 실패했습니다.")
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
            failureFrom(e, "핀 댓글 수정에 실패했습니다.")
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
            failureFrom(e, "핀 댓글 삭제에 실패했습니다.")
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
            failureFrom(e, "핀 상세 해결하기 조회에 실패했습니다.")
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
            failureFrom(e, "시민해결사 참여에 실패했습니다.")
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
            failureFrom(e, "시민해결사 조회에 실패했습니다.")
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
            failureFrom(e, "사진 인증 등록에 실패했습니다.")
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
            failureFrom(e, "시민해결사 인증에 실패했습니다.")
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
            failureFrom(e, "청원 현황 조회에 실패했습니다.")
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
            failureFrom(e, "청원에 실패했습니다.")
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

private class PinEditApiException(
    val serverCode: String?,
    message: String,
) : Exception(message)

private fun pinEditException(
    isCommunicationPin: Boolean,
    code: String?,
    message: String?,
): PinEditApiException {
    val normalizedCode = code?.takeIf { it.isNotBlank() }
    val normalizedMessage = message?.takeIf { it.isNotBlank() }
    val userMessage = when (normalizedCode) {
        "ISSUE_PIN_EDIT_400_1",
        "PIN_EDIT_COMMUNICATION_400_1",
        ->
            normalizedMessage
                ?: "요청 정보가 올바르지 않습니다. 제목, 내용, 사진을 확인해주세요."
        "ISSUE_PIN_EDIT_400_2" ->
            normalizedMessage ?: "이슈 핀 수정 저장에 실패했습니다."
        "PIN_EDIT_COMMUNICATION_400_2" ->
            normalizedMessage ?: "소통 핀 수정 API를 실행할 수 없습니다."
        "PIN_IMAGE_400_1" ->
            normalizedMessage ?: "첨부한 사진 용량이 너무 큽니다. (최대 50MB)"
        "PIN_IMAGE_400_2" ->
            normalizedMessage
                ?: "사진 첨부에 실패했습니다. 다른 사진으로 다시 시도해주세요."
        "PIN_IMAGE_400_3" ->
            normalizedMessage ?: "사진은 최대 5장까지 등록할 수 있습니다."
        "COMMON_403" ->
            normalizedMessage ?: "본인이 작성한 핀만 수정할 수 있습니다."
        "ISSUE_4041" ->
            normalizedMessage ?: "존재하지 않는 핀이거나 이슈 핀이 아닙니다."
        "USER_404_1", "USER_4041" ->
            normalizedMessage ?: if (isCommunicationPin) {
                "존재하지 않는 회원입니다."
            } else {
                "사용자 정보를 찾을 수 없습니다."
            }
        "ISSUE_4292" ->
            normalizedMessage ?: "이 핀의 일일 수정 횟수를 초과했습니다."
        else ->
            normalizedMessage ?: if (isCommunicationPin) {
                "소통 핀 수정에 실패했습니다."
            } else {
                "이슈 핀 수정에 실패했습니다."
            }
    }
    return PinEditApiException(normalizedCode, userMessage)
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
