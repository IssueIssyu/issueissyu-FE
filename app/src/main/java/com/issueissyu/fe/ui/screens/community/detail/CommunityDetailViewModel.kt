package com.issueissyu.fe.ui.screens.community.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.community.CommunityItemKind
import com.issueissyu.fe.domain.repository.PinRepository
import com.issueissyu.fe.domain.usecase.community.GetCommunityDetailUseCase
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
}
