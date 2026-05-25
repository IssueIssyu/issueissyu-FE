package com.issueissyu.fe.ui.screens.community.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.community.CommunityItemKind
import com.issueissyu.fe.domain.repository.PinRepository
import com.issueissyu.fe.domain.usecase.community.CreateCommunityCommentUseCase
import com.issueissyu.fe.domain.usecase.community.DeclareCommunityUseCase
import com.issueissyu.fe.domain.usecase.community.GetCommunityCommentsUseCase
import com.issueissyu.fe.domain.usecase.community.GetCommunityDetailUseCase
import com.issueissyu.fe.domain.usecase.community.LikeCommunityUseCase
import com.issueissyu.fe.domain.usecase.community.UpdateCommunityCommentUseCase
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityDetailViewModel @Inject constructor(
    private val getCommunityDetailUseCase: GetCommunityDetailUseCase,
    private val getCommunityCommentsUseCase: GetCommunityCommentsUseCase,
    private val createCommunityCommentUseCase: CreateCommunityCommentUseCase,
    private val updateCommunityCommentUseCase: UpdateCommunityCommentUseCase,
    private val likeCommunityUseCase: LikeCommunityUseCase,
    private val declareCommunityUseCase: DeclareCommunityUseCase,
    private val pinRepository: PinRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val communityId: Long = savedStateHandle.get<Long>("communityId") ?: 0L

    private val _uiState = MutableStateFlow(CommunityDetailUiState())
    val uiState: StateFlow<CommunityDetailUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

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
                    val petitionStatus = if (detail.kind == CommunityItemKind.ISSUE && pinId != null) {
                        pinRepository.getPetitionStatus(pinId).getOrNull()
                    } else {
                        null
                    }
                    val solveStatus = if (detail.kind == CommunityItemKind.ISSUE && pinId != null) {
                        pinRepository.getPinSolveStatus(pinId).getOrNull()
                    } else {
                        null
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
                                reliabilityScore = solveStatus?.reliability ?: detail.reliabilityScore,
                            ),
                            errorMessage = null
                        )
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
                            comments = comments,
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
                            comments = listOf(comment) + state.comments,
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
                            },
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

    fun declareCommunity(reasonIndex: Int) {
        val detail = _uiState.value.detail ?: return
        if (detail.isMine || detail.isReported || _uiState.value.isCommunityDeclarationSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCommunityDeclarationSubmitting = true) }

            declareCommunityUseCase(detail.communityId, reasonIndex)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            detail = state.detail?.copy(isReported = true),
                            isCommunityDeclarationSubmitting = false,
                        )
                    }
                    _toastMessage.emit("신고가 접수되었습니다.")
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isCommunityDeclarationSubmitting = false) }
                    _toastMessage.emit(
                        throwable.message?.takeIf { it.isNotBlank() } ?: "신고에 실패했습니다.",
                    )
                }
        }
    }
}
