package com.issueissyu.fe.ui.screens.community.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.core.issue.IssueReliabilityPolling
import com.issueissyu.fe.domain.model.community.CommunityItemKind
import com.issueissyu.fe.domain.model.issue.IssueReliability
import com.issueissyu.fe.domain.model.issue.IssueReliabilityStatus
import com.issueissyu.fe.domain.model.pin.PinEmojiReaction
import com.issueissyu.fe.domain.model.pin.PinEmojis
import com.issueissyu.fe.domain.repository.PinRepository
import com.issueissyu.fe.domain.usecase.community.CreateCommunityCommentUseCase
import com.issueissyu.fe.domain.usecase.community.DeleteCommunityCommentUseCase
import com.issueissyu.fe.domain.usecase.community.DeleteCommunityUseCase
import com.issueissyu.fe.domain.usecase.community.GetCommunityCommentsUseCase
import com.issueissyu.fe.domain.usecase.community.GetCommunityDetailUseCase
import com.issueissyu.fe.domain.usecase.community.LikeCommunityUseCase
import com.issueissyu.fe.domain.usecase.community.TakedownCommunityUseCase
import com.issueissyu.fe.domain.usecase.community.UpdateCommunityCommentUseCase
import com.issueissyu.fe.domain.usecase.issue.GetIssueReliabilityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityDetailViewModel @Inject constructor(
    private val getCommunityDetailUseCase: GetCommunityDetailUseCase,
    private val getCommunityCommentsUseCase: GetCommunityCommentsUseCase,
    private val createCommunityCommentUseCase: CreateCommunityCommentUseCase,
    private val updateCommunityCommentUseCase: UpdateCommunityCommentUseCase,
    private val deleteCommunityCommentUseCase: DeleteCommunityCommentUseCase,
    private val deleteCommunityUseCase: DeleteCommunityUseCase,
    private val takedownCommunityUseCase: TakedownCommunityUseCase,
    private val likeCommunityUseCase: LikeCommunityUseCase,
    private val getIssueReliabilityUseCase: GetIssueReliabilityUseCase,
    private val pinRepository: PinRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val communityId: Long = savedStateHandle.get<Long>("communityId") ?: 0L
    private var issueReliabilityJob: Job? = null
    private var observedReliabilityPinId: Long? = null

    private val _uiState = MutableStateFlow(CommunityDetailUiState())
    val uiState: StateFlow<CommunityDetailUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _deleteCompleted = MutableSharedFlow<Unit>()
    val deleteCompleted: SharedFlow<Unit> = _deleteCompleted.asSharedFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        if (_uiState.value.isLoading) return

        if (communityId == 0L) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "잘못된 접근입니다."
                )
            }
            return
        }

        viewModelScope.launch {
            getCommunityDetailUseCase(communityId)
                .onStart {
                    _uiState.update {
                        it.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                }
                .catch {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "게시글을 불러오지 못했습니다.\n잠시 후 다시 시도해주세요."
                        )
                    }
                }
                .collect { detail ->
                    val pinId = detail.pinId
                    val (petitionStatus, solveStatus) = if (detail.kind == CommunityItemKind.ISSUE && pinId != null) {
                        coroutineScope {
                            val petitionDeferred = async {
                                pinRepository.getPetitionStatus(pinId).getOrNull()
                            }
                            val solveDeferred = async {
                                pinRepository.getPinSolveStatus(pinId).getOrNull()
                            }

                            petitionDeferred.await() to solveDeferred.await()
                        }
                    } else {
                        null to null
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            detail = detail.copy(
                                petitionCount = petitionStatus?.petitionCount ?: detail.petitionCount,
                                petitionTargetCount = petitionStatus?.targetPetition ?: detail.petitionTargetCount,
                                isPetitionedByMe = petitionStatus?.isPetitioned
                                    ?: solveStatus?.isPetitioned
                                    ?: detail.isPetitionedByMe,
                                isPetitioned = petitionStatus?.isPetitioned
                                    ?: solveStatus?.isPetitioned
                                    ?: detail.isPetitioned,
                                isProblemSolver = solveStatus?.isProblemSolver ?: detail.isProblemSolver,
                            ),
                            reliabilityStatus = IssueReliabilityStatus.PENDING,
                            errorMessage = null
                        )
                    }
                    if (pinId != null) {
                        loadPinEmojis(pinId)
                    } else {
                        _uiState.update { it.copy(emojiReactions = emptyList(), emojiPicker = CommunityEmojiPickerUiState()) }
                    }
                    if (detail.kind == CommunityItemKind.ISSUE && pinId != null) {
                        startIssueReliabilityObservation(pinId)
                    }
                }
        }

        loadComments()
    }

    private fun loadComments() {
        viewModelScope.launch {
            getCommunityCommentsUseCase(communityId)
                .onStart {
                    _uiState.update { it.copy(isCommentLoading = true) }
                }
                .catch { throwable ->
                    _uiState.update { it.copy(isCommentLoading = false) }
                    _toastMessage.emit(
                        throwable.message?.takeIf { it.isNotBlank() } ?: "댓글을 불러오지 못했습니다.",
                    )
                }
                .collect { comments ->
                    _uiState.update {
                        it.copy(
                            comments = comments.sortedByCreatedAt(),
                            isCommentLoading = false,
                        )
                    }
                }
        }
    }

    fun submitPetition() {
        val detail = _uiState.value.detail ?: return
        val pinId = detail.pinId ?: return
        if (detail.kind != CommunityItemKind.ISSUE || detail.isPetitionedByMe) return

        viewModelScope.launch {
            pinRepository.submitPetition(pinId)
                .onSuccess { petition ->
                    _uiState.update { state ->
                        state.copy(
                            detail = state.detail?.copy(
                                petitionCount = petition.petitionCount,
                                isPetitioned = petition.isPetitioned,
                                isPetitionedByMe = petition.isPetitioned,
                            ),
                        )
                    }
                    _toastMessage.emit("청원에 참여했습니다.")
                }
                .onFailure { throwable ->
                    _toastMessage.emit(
                        throwable.message?.takeIf { it.isNotBlank() } ?: "청원하기에 실패했습니다.",
                    )
                }
        }
    }

    fun goNow() {
        val detail = _uiState.value.detail ?: return
        val pinId = detail.pinId ?: return
        if (detail.kind != CommunityItemKind.ISSUE || detail.isProblemSolver) return

        viewModelScope.launch {
            pinRepository.goNow(pinId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            detail = state.detail?.copy(isProblemSolver = true),
                        )
                    }
                    _toastMessage.emit("지금가요 참여가 완료되었습니다.")
                }
                .onFailure { throwable ->
                    _toastMessage.emit(
                        throwable.message?.takeIf { it.isNotBlank() } ?: "지금가요 참여에 실패했습니다.",
                    )
                }
        }
    }

    fun createComment(content: String) {
        val trimmedContent = content.trim()
        if (trimmedContent.isBlank() || _uiState.value.isCommentSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCommentSubmitting = true) }

            createCommunityCommentUseCase(communityId, trimmedContent)
                .onSuccess { comment ->
                    _uiState.update { state ->
                        state.copy(
                            comments = (state.comments + comment).sortedByCreatedAt(),
                            isCommentSubmitting = false,
                        )
                    }
                    _toastMessage.emit("댓글이 등록되었습니다.")
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isCommentSubmitting = false) }
                    _toastMessage.emit(
                        throwable.message?.takeIf { it.isNotBlank() } ?: "댓글 등록에 실패했습니다.",
                    )
                }
        }
    }

    fun updateComment(commentId: Long, content: String) {
        val trimmedContent = content.trim()
        if (trimmedContent.isBlank() || _uiState.value.isCommentSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCommentSubmitting = true) }

            updateCommunityCommentUseCase(commentId, trimmedContent)
                .onSuccess { updatedComment ->
                    _uiState.update { state ->
                        state.copy(
                            comments = state.comments.map { comment ->
                                if (comment.commentId == updatedComment.commentId) updatedComment else comment
                            }.sortedByCreatedAt(),
                            isCommentSubmitting = false,
                        )
                    }
                    _toastMessage.emit("댓글이 수정되었습니다.")
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isCommentSubmitting = false) }
                    _toastMessage.emit(
                        throwable.message?.takeIf { it.isNotBlank() } ?: "댓글 수정에 실패했습니다.",
                    )
                }
        }
    }

    fun deleteComment(commentId: Long) {
        if (commentId in _uiState.value.deletingCommentIds) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(deletingCommentIds = state.deletingCommentIds + commentId)
            }

            deleteCommunityCommentUseCase(commentId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            comments = state.comments.filterNot { it.commentId == commentId },
                            deletingCommentIds = state.deletingCommentIds - commentId,
                        )
                    }
                    _toastMessage.emit("댓글이 삭제되었습니다.")
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(deletingCommentIds = state.deletingCommentIds - commentId)
                    }
                    _toastMessage.emit(
                        throwable.message?.takeIf { it.isNotBlank() } ?: "댓글 삭제에 실패했습니다.",
                    )
                }
        }
    }

    fun deleteCommunity() {
        val detail = _uiState.value.detail ?: return
        if (!detail.isMine || detail.kind != CommunityItemKind.COMMUNICATION || _uiState.value.isCommunityDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCommunityDeleting = true) }

            deleteCommunityUseCase(detail.communityId)
                .onSuccess {
                    _uiState.update { it.copy(isCommunityDeleting = false) }
                    _toastMessage.emit("게시글이 삭제되었습니다.")
                    _deleteCompleted.emit(Unit)
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isCommunityDeleting = false) }
                    _toastMessage.emit(
                        throwable.message?.takeIf { it.isNotBlank() } ?: "게시글 삭제에 실패했습니다.",
                    )
                }
        }
    }

    fun takedownCommunity() {
        val detail = _uiState.value.detail ?: return
        val canTakedown = detail.kind == CommunityItemKind.ISSUE || detail.kind == CommunityItemKind.COMMUNICATION
        if (!detail.isMine || !canTakedown || _uiState.value.isCommunityDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCommunityDeleting = true) }

            takedownCommunityUseCase(detail.communityId)
                .onSuccess {
                    _uiState.update { it.copy(isCommunityDeleting = false) }
                    _toastMessage.emit("게시글이 내려갔습니다.")
                    _deleteCompleted.emit(Unit)
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isCommunityDeleting = false) }
                    _toastMessage.emit(
                        throwable.message?.takeIf { it.isNotBlank() } ?: "게시글 내리기에 실패했습니다.",
                    )
                }
        }
    }

    fun likeCommunity() {
        val detail = _uiState.value.detail ?: return
        if (detail.isLikedByMe || _uiState.value.isCommunityLikeSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCommunityLikeSubmitting = true) }

            likeCommunityUseCase(detail.communityId)
                .onSuccess { like ->
                    _uiState.update { state ->
                        state.copy(
                            detail = state.detail?.copy(
                                likeCount = like.pinLikeCount,
                                isLikedByMe = like.isLike,
                            ),
                            isCommunityLikeSubmitting = false,
                        )
                    }
                    _toastMessage.emit("게시글에 공감했습니다.")
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isCommunityLikeSubmitting = false) }
                    _toastMessage.emit(
                        throwable.message?.takeIf { it.isNotBlank() } ?: "공감에 실패했습니다.",
                    )
                }
        }
    }

    fun openEmojiPicker() {
        val detail = _uiState.value.detail ?: return
        val pinId = detail.pinId ?: return

        val selectedEmojiId = _uiState.value.emojiReactions
            .firstOrNull { it.reactedByMe }
            ?.emojiId
            ?.toLongOrNull()

        _uiState.update {
            it.copy(
                emojiPicker = CommunityEmojiPickerUiState(
                    targetPinId = pinId,
                    selectedEmojiId = selectedEmojiId,
                    isLoading = true,
                )
            )
        }

        viewModelScope.launch {
            pinRepository.getEmojiCandidates()
                .onSuccess { candidates ->
                    _uiState.update {
                        it.copy(
                            emojiPicker = it.emojiPicker.copy(
                                candidates = candidates,
                                isLoading = false,
                                errorMessage = null,
                            )
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            emojiPicker = it.emojiPicker.copy(
                                isLoading = false,
                                errorMessage = throwable.message?.takeIf { message -> message.isNotBlank() }
                                    ?: "이모지 목록을 불러오지 못했습니다.",
                            )
                        )
                    }
                }
        }
    }

    fun closeEmojiPicker() {
        _uiState.update { it.copy(emojiPicker = CommunityEmojiPickerUiState()) }
    }

    fun selectEmojiCandidate(emojiId: Long) {
        val candidate = _uiState.value.emojiPicker.candidates.firstOrNull { it.emojiId == emojiId }
            ?: return
        if (!candidate.canReact) {
            viewModelScope.launch {
                _toastMessage.emit("구매가 필요한 이모지입니다.")
            }
            return
        }
        _uiState.update {
            val nextSelection = if (it.emojiPicker.selectedEmojiId == emojiId) null else emojiId
            it.copy(emojiPicker = it.emojiPicker.copy(selectedEmojiId = nextSelection))
        }
    }

    fun applySelectedEmoji() {
        val picker = _uiState.value.emojiPicker
        val pinId = picker.targetPinId ?: return
        val selectedEmojiId = picker.selectedEmojiId
        val selectedCandidate = selectedEmojiId?.let { emojiId ->
            picker.candidates.firstOrNull { it.emojiId == emojiId }
        }
        if (selectedCandidate?.canReact == false || picker.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(emojiPicker = it.emojiPicker.copy(isSubmitting = true)) }

            pinRepository.applyPinEmojiFromPicker(pinId, selectedEmojiId)
                .onSuccess {
                    loadPinEmojis(pinId)
                    closeEmojiPicker()
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(emojiPicker = it.emojiPicker.copy(isSubmitting = false)) }
                    _toastMessage.emit(
                        throwable.message?.takeIf { it.isNotBlank() } ?: "이모지 반응 등록에 실패했습니다.",
                    )
                }
        }
    }

    private suspend fun loadPinEmojis(pinId: Long) {
        pinRepository.getPinEmojis(pinId)
            .onSuccess { pinEmojis ->
                _uiState.update { it.copy(emojiReactions = pinEmojis.toEmojiReactions()) }
            }
    }

    private fun startIssueReliabilityObservation(pinId: Long) {
        val isNewPin = observedReliabilityPinId != pinId
        observedReliabilityPinId = pinId
        issueReliabilityJob?.cancel()
        issueReliabilityJob = viewModelScope.launch {
            if (isNewPin) {
                _uiState.update {
                    it.copy(
                        reliabilityStatus = IssueReliabilityStatus.PENDING,
                        detail = it.detail?.copy(
                            reliabilityScore = null,
                            reliabilityReason = null,
                        ),
                    )
                }
            }
            IssueReliabilityPolling.fetchWithPolling(
                fetch = { getIssueReliabilityUseCase(pinId) },
                onUpdate = { reliability -> applyIssueReliability(reliability) },
            )
        }
    }

    private fun applyIssueReliability(reliability: IssueReliability) {
        if (observedReliabilityPinId != reliability.pinId) return
        _uiState.update { state ->
            state.copy(
                detail = state.detail?.copy(
                    reliabilityScore = reliability.score,
                    reliabilityReason = reliability.reason,
                ),
                reliabilityStatus = reliability.status,
            )
        }
    }

    private fun PinEmojis.toEmojiReactions(): List<PinEmojiReaction> {
        return emojis
            .filter { it.count > 0 }
            .map { emoji ->
                PinEmojiReaction(
                    emojiId = emoji.emojiId.toString(),
                    count = emoji.count,
                    reactedByMe = emoji.emojiId == selectedEmojiId,
                    emojiImageUrl = emoji.emojiImageUrl,
                )
            }
    }
}

private fun List<com.issueissyu.fe.domain.model.community.CommunityComment>.sortedByCreatedAt() =
    sortedWith(compareBy(nullsLast()) { it.createdAt })
