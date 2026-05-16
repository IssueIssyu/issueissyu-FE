package com.issueissyu.fe.ui.screens.community.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val communityId: Long = savedStateHandle.get<Long>("communityId") ?: 0L

    private val _uiState = MutableStateFlow(CommunityDetailUiState())
    val uiState: StateFlow<CommunityDetailUiState> = _uiState.asStateFlow()

    init {
        if (communityId != 0L) {
            loadDetail()
        } else {
            _uiState.update { it.copy(errorMessage = "잘못된 접근입니다.") }
        }
    }

    fun loadDetail() {
        viewModelScope.launch {
            getCommunityDetailUseCase(communityId)
                .onStart {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "상세 정보를 불러오는 중 오류가 발생했습니다.") }
                }
                .collect { detail ->
                    _uiState.update { it.copy(isLoading = false, detail = detail) }
                }
        }
    }
}
