package com.issueissyu.fe.ui.screens.community.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.community.CommunityItemKind
import com.issueissyu.fe.domain.repository.PinRepository
import com.issueissyu.fe.domain.usecase.community.GetCommunityDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
}
