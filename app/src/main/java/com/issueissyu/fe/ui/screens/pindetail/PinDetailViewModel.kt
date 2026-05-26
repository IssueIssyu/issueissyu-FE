package com.issueissyu.fe.ui.screens.pindetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.pin.IssueResolverParticipation
import com.issueissyu.fe.domain.model.pin.PinDetail
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinComment
import com.issueissyu.fe.domain.model.pin.PinEmojiCandidate
import com.issueissyu.fe.domain.model.pin.PinPostSympathyContent
import com.issueissyu.fe.domain.model.pin.PinUser
import com.issueissyu.fe.domain.model.pin.ResolutionStatus
import com.issueissyu.fe.domain.model.pin.toPostSympathyContent
import com.issueissyu.fe.domain.model.pin.withHomeFallback
import com.issueissyu.fe.domain.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

enum class PinDetailTab {
    HOME,
    POST,
    RESOLUTION,
}

data class PinDetailUiState(
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val isResolutionJoining: Boolean = false,
    val isResolutionProofSubmitting: Boolean = false,
    val pin: Pin? = null,
    val postSympathy: PinPostSympathyContent? = null,
    val postEmojis: PinDetailPostEmojis = PinDetailPostEmojis(),
    val postComments: List<PinComment> = emptyList(),
    val isCommentsLoading: Boolean = false,
    val isCommentSubmitting: Boolean = false,
    val commentInputRevision: Int = 0,
    val editingCommentId: Long? = null,
    val selectedTab: PinDetailTab = PinDetailTab.HOME,
    val errorMessage: String? = null,
)

data class PinDetailEmojiPickerUiState(
    val isVisible: Boolean = false,
    val candidates: List<PinEmojiCandidate> = emptyList(),
    val pickedEmojiId: Long? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface PinDetailEffect {
    data class ShowToast(val message: String) : PinDetailEffect
}

@HiltViewModel
class PinDetailViewModel @Inject constructor(
    private val pinRepository: PinRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinDetailUiState())
    val uiState: StateFlow<PinDetailUiState> = _uiState.asStateFlow()

    private val _emojiPickerUiState = MutableStateFlow(PinDetailEmojiPickerUiState())
    val emojiPickerUiState: StateFlow<PinDetailEmojiPickerUiState> = _emojiPickerUiState.asStateFlow()

    private val _effect = MutableSharedFlow<PinDetailEffect>(extraBufferCapacity = 8)
    val effect: SharedFlow<PinDetailEffect> = _effect.asSharedFlow()

    private var routePinId: Long? = null
    private var emojiImageById: Map<Long, String> = emptyMap()

    fun loadPin(pinId: String) {
        val id = pinId.toLongOrNull() ?: run {
            routePinId = null
            _uiState.update { it.copy(isLoading = false, errorMessage = "잘못된 핀 ID입니다.") }
            return
        }
        routePinId = id

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    postSympathy = null,
                    postEmojis = PinDetailPostEmojis(),
                    postComments = emptyList(),
                    isCommentsLoading = false,
                    isResolutionJoining = false,
                    isResolutionProofSubmitting = false,
                )
            }

            pinRepository.getPinDetailHome(id)
                .onSuccess { pin ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pin = pin,
                            postSympathy = pin.toPostSympathyContent(),
                        )
                    }
                    loadPostTab(id)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    private fun loadPostTab(pinId: Long) {
        viewModelScope.launch {
            pinRepository.getPinDetailPost(pinId).onSuccess { sympathy ->
                val pin = _uiState.value.pin?.takeIf { it.id == pinId.toString() } ?: return@onSuccess
                if (sympathy == null) return@onSuccess
                _uiState.update {
                    it.copy(
                        postSympathy = sympathy.withHomeFallback(
                            fallback = pin.toPostSympathyContent(),
                            layoutCategory = pin.category,
                        ),
                    )
                }
            }
            refreshPostEmojis(pinId)
            loadPinComments(pinId)
        }
    }

    private suspend fun loadPinComments(pinId: Long, showLoading: Boolean = true) {
        if (showLoading) {
            _uiState.update { it.copy(isCommentsLoading = true) }
        }
        pinRepository.getPinComments(pinId)
            .onSuccess { comments ->
                _uiState.update { it.copy(postComments = comments, isCommentsLoading = false) }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isCommentsLoading = false) }
                showToast(e.message ?: "댓글을 불러오지 못했습니다.")
            }
    }

    private suspend fun refreshPostEmojis(pinId: Long): Result<Unit> {
        return pinRepository.getPinEmojis(pinId).fold(
            onSuccess = { data ->
                _uiState.update {
                    it.copy(postEmojis = data.toPinDetailPostEmojis(::lookupEmojiImage))
                }
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
        )
    }

    private fun lookupEmojiImage(emojiId: Long): String? = emojiImageById[emojiId]

    fun selectTab(tab: PinDetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == PinDetailTab.POST) {
            resolvePinId()?.let { pinId ->
                viewModelScope.launch {
                    refreshPostEmojis(pinId)
                    loadPinComments(pinId)
                }
            }
        }
    }

    fun startEditComment(commentId: Long) {
        val exists = _uiState.value.postComments.any { it.commentId == commentId && it.isMine }
        if (!exists) return
        _uiState.update { it.copy(editingCommentId = commentId) }
    }

    fun cancelEditComment() {
        _uiState.update {
            it.copy(
                editingCommentId = null,
                commentInputRevision = it.commentInputRevision + 1,
            )
        }
    }

    fun submitComment(content: String) {
        val trimmed = content.trim()
        if (trimmed.isBlank()) {
            showToast("댓글을 입력해 주세요.")
            return
        }
        if (_uiState.value.isCommentSubmitting) return

        val editingCommentId = _uiState.value.editingCommentId
        if (editingCommentId != null) {
            submitCommentUpdate(editingCommentId, trimmed)
            return
        }

        val pinId = resolvePinId() ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCommentSubmitting = true) }
            pinRepository.createPinComment(pinId, trimmed)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isCommentSubmitting = false,
                            commentInputRevision = state.commentInputRevision + 1,
                        )
                    }
                    loadPinComments(pinId, showLoading = false)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isCommentSubmitting = false) }
                    showToast(e.message ?: "댓글 작성에 실패했습니다.")
                }
        }
    }

    private fun submitCommentUpdate(commentId: Long, content: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCommentSubmitting = true) }
            pinRepository.updatePinComment(commentId, content)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isCommentSubmitting = false,
                            editingCommentId = null,
                            commentInputRevision = state.commentInputRevision + 1,
                        )
                    }
                    resolvePinId()?.let { loadPinComments(it, showLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isCommentSubmitting = false) }
                    showToast(e.message ?: "댓글 수정에 실패했습니다.")
                }
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            pinRepository.deletePinComment(commentId)
                .onSuccess {
                    _uiState.update { state ->
                        val wasEditing = state.editingCommentId == commentId
                        state.copy(
                            postComments = state.postComments.filterNot { it.commentId == commentId },
                            editingCommentId = if (wasEditing) null else state.editingCommentId,
                            commentInputRevision = if (wasEditing) {
                                state.commentInputRevision + 1
                            } else {
                                state.commentInputRevision
                            },
                        )
                    }
                }
                .onFailure { e -> showToast(e.message ?: "댓글 삭제에 실패했습니다.") }
        }
    }

    fun toggleSympathy() {
        val sympathy = _uiState.value.postSympathy ?: return
        if (sympathy.isSympathizedByMe) return

        viewModelScope.launch {
            pinRepository.likePin(sympathy.pinId)
                .onSuccess { like ->
                    val current = _uiState.value.postSympathy ?: return@onSuccess
                    _uiState.update {
                        it.copy(
                            postSympathy = current.copy(
                                isSympathizedByMe = like.isLike,
                                sympathyCount = like.pinLikeCount,
                            ),
                        )
                    }
                }
                .onFailure { e -> showToast(e.message ?: "공감에 실패했습니다.") }
        }
    }

    fun openEmojiPicker() {
        val myEmojiId = _uiState.value.postEmojis.myEmojiId

        _emojiPickerUiState.value = PinDetailEmojiPickerUiState(
            isVisible = true,
            pickedEmojiId = myEmojiId,
            isLoading = true,
        )

        viewModelScope.launch {
            pinRepository.getEmojiCandidates()
                .onSuccess { candidates ->
                    emojiImageById = candidates.associate { it.emojiId to it.emojiImageUrl }
                    _emojiPickerUiState.update {
                        it.copy(
                            candidates = candidates,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { e ->
                    _emojiPickerUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "이모지 목록을 불러오지 못했습니다.",
                        )
                    }
                }
        }
    }

    fun closeEmojiPicker() {
        _emojiPickerUiState.value = PinDetailEmojiPickerUiState()
    }

    fun pickEmojiInPicker(emojiId: Long) {
        val candidate = _emojiPickerUiState.value.candidates.firstOrNull { it.emojiId == emojiId }
            ?: return
        if (!candidate.canReact) {
            showToast("구매가 필요한 이모지입니다.")
            return
        }
        _emojiPickerUiState.update { state ->
            val nextSelection = if (state.pickedEmojiId == emojiId) null else emojiId
            state.copy(pickedEmojiId = nextSelection)
        }
    }

    fun submitPickedEmoji() {
        val pinId = resolvePinId() ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        if (_emojiPickerUiState.value.isSubmitting) return

        val pickedEmojiId = _emojiPickerUiState.value.pickedEmojiId

        viewModelScope.launch {
            _emojiPickerUiState.update { it.copy(isSubmitting = true) }

            pinRepository.applyPinEmojiFromPicker(pinId, pickedEmojiId)
                .onSuccess {
                    refreshPostEmojis(pinId)
                        .onFailure { e ->
                            showToast(e.message ?: "반응 목록을 갱신하지 못했습니다.")
                        }
                    closeEmojiPicker()
                }
                .onFailure { e ->
                    _emojiPickerUiState.update { it.copy(isSubmitting = false) }
                    showToast(e.message ?: "이모지 반응 처리에 실패했습니다.")
                }
        }
    }

    fun toggleEmojiFromList(emojiId: Long) {
        val pinId = resolvePinId() ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }

        viewModelScope.launch {
            pinRepository.togglePinEmojiFromList(pinId, emojiId)
                .onSuccess {
                    refreshPostEmojis(pinId)
                        .onFailure { e ->
                            showToast(e.message ?: "반응 목록을 갱신하지 못했습니다.")
                        }
                }
                .onFailure { e ->
                    showToast(e.message ?: "이모지 반응 처리에 실패했습니다.")
                }
        }
    }

    fun deletePin(pinId: String, onSuccess: () -> Unit) {
        if (_uiState.value.isDeleting) return
        val id = pinId.toLongOrNull() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            pinRepository.deletePin(id)
                .onSuccess {
                    _uiState.update { it.copy(isDeleting = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isDeleting = false) }
                    showToast(e.message ?: "핀 삭제에 실패했습니다.")
                }
        }
    }

    fun joinResolution(
        currentUserId: String,
        onSuccess: () -> Unit = {},
    ) {
        val pinId = resolvePinId() ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        val currentPin = _uiState.value.pin ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        val issueDetail = currentPin.detail as? IssuePinDetail ?: run {
            showToast("해결 참여는 이슈 핀에서만 가능합니다.")
            return
        }
        if (_uiState.value.isResolutionJoining) return
        if (issueDetail.writer.id == currentUserId) {
            showToast("작성자는 시민해결사로 참여할 수 없습니다.")
            return
        }
        if (issueDetail.resolverParticipations.any { it.user.id == currentUserId }) {
            showToast("이미 참여 중입니다.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isResolutionJoining = true) }
            pinRepository.joinResolution(pinId)
                .onSuccess {
                    val joinedAt = Instant.now().toString()
                    _uiState.update { state ->
                        state.copy(
                            isResolutionJoining = false,
                            pin = state.pin?.withJoinedResolution(
                                currentUser = buildResolutionCurrentUser(
                                    currentUserId = currentUserId,
                                    existingPin = state.pin
                                ),
                                joinedAt = joinedAt,
                            ),
                        )
                    }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isResolutionJoining = false) }
                    showToast(e.message ?: "시민해결사 참여에 실패했습니다.")
                }
        }
    }

    fun submitResolutionProof(
        currentUserId: String,
        imageUri: String,
        onSuccess: () -> Unit = {},
    ) {
        val pinId = resolvePinId() ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        val currentPin = _uiState.value.pin ?: run {
            showToast("핀 정보를 찾을 수 없습니다.")
            return
        }
        val issueDetail = currentPin.detail as? IssuePinDetail ?: run {
            showToast("해결 인증은 이슈 핀에서만 가능합니다.")
            return
        }
        if (_uiState.value.isResolutionProofSubmitting) return
        if (issueDetail.resolverParticipations.none { it.user.id == currentUserId }) {
            showToast("참여한 시민해결사만 사진 인증을 할 수 있어요.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isResolutionProofSubmitting = true) }
            pinRepository.submitResolutionProof(pinId, imageUri)
                .onSuccess {
                    val submittedAt = Instant.now().toString()
                    _uiState.update { state ->
                        state.copy(
                            isResolutionProofSubmitting = false,
                            pin = state.pin?.withResolutionProofSubmitted(
                                currentUserId = currentUserId,
                                imageUri = imageUri,
                                submittedAt = submittedAt,
                            ),
                        )
                    }
                    showToast("사진 인증이 등록되었습니다.")
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isResolutionProofSubmitting = false) }
                    showToast(e.message ?: "사진 인증 등록에 실패했습니다.")
                }
        }
    }

    private fun resolvePinId(): Long? {
        return routePinId
            ?: _uiState.value.postSympathy?.pinId?.takeIf { it > 0L }
            ?: _uiState.value.pin?.id?.toLongOrNull()
    }

    private fun showToast(message: String) {
        _effect.tryEmit(PinDetailEffect.ShowToast(message))
    }
}

private fun Pin.withResolutionProofSubmitted(
    currentUserId: String,
    imageUri: String,
    submittedAt: String,
): Pin {
    val issueDetail = detail as? IssuePinDetail ?: return this
    val updatedParticipations = issueDetail.resolverParticipations.map { participation ->
        if (participation.user.id != currentUserId) {
            participation
        } else {
            participation.copy(
                proofImageUrls = listOf(imageUri),
                proofSubmittedAt = submittedAt,
            )
        }
    }
    return copy(
        detail = issueDetail.copy(
            resolverParticipations = updatedParticipations,
        ) as PinDetail
    )
}

private fun Pin.withJoinedResolution(
    currentUser: PinUser,
    joinedAt: String,
): Pin {
    val issueDetail = detail as? IssuePinDetail ?: return this
    if (issueDetail.resolverParticipations.any { it.user.id == currentUser.id }) return this

    return copy(
        detail = issueDetail.copy(
            resolutionStatus = if (issueDetail.resolutionStatus == ResolutionStatus.BEFORE_RESOLUTION) {
                ResolutionStatus.IN_PROGRESS
            } else {
                issueDetail.resolutionStatus
            },
            resolverParticipations = issueDetail.resolverParticipations + IssueResolverParticipation(
                user = currentUser,
                joinedAt = joinedAt,
            ),
        ) as PinDetail
    )
}

private fun buildResolutionCurrentUser(
    currentUserId: String,
    existingPin: Pin?,
): PinUser {
    val existingDetail = existingPin?.detail as? IssuePinDetail
    val existingUser = existingDetail?.resolverParticipations
        ?.firstOrNull { it.user.id == currentUserId }
        ?.user
    if (existingUser != null) return existingUser

    return when (currentUserId) {
        "user1_id" -> PinUser(
            id = "user1_id",
            name = "현재 사용자",
            imageUrl = "https://example.com/user1.jpg"
        )
        "user2_id" -> PinUser(
            id = "user2_id",
            name = "박개발",
            imageUrl = "https://example.com/user2.jpg"
        )
        else -> PinUser(
            id = currentUserId,
            name = "현재 사용자",
            imageUrl = null
        )
    }
}
