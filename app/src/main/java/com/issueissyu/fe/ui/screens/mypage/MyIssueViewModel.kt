package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.mypage.MyIssuePage
import com.issueissyu.fe.domain.model.mypage.MyIssuePin
import com.issueissyu.fe.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyIssueUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val issues: List<MyIssuePin> = emptyList(),
    val hasNext: Boolean = false,
    val nextCursor: String? = null,
)

@HiltViewModel
class MyIssueViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyIssueUiState())
    val uiState: StateFlow<MyIssueUiState> = _uiState.asStateFlow()

    private var loadIssuesJob: Job? = null

    init {
        loadIssues()
    }

    fun loadIssues(append: Boolean = false) {
        val currentState = _uiState.value
        if (append) {
            if (currentState.isLoadingMore || !currentState.hasNext) return
        }

        loadIssuesJob?.cancel()
        loadIssuesJob = viewModelScope.launch {
            _uiState.update {
                if (append) {
                    it.copy(isLoadingMore = true, errorMessage = null)
                } else {
                    it.copy(
                        isLoading = true,
                        isLoadingMore = false,
                        errorMessage = null,
                        issues = emptyList(),
                        hasNext = false,
                        nextCursor = null,
                    )
                }
            }

            userRepository.getMyIssues(cursor = if (append) currentState.nextCursor else null)
                .onSuccess { page -> applyPage(page, append) }
                .onFailure { error ->
                    _uiState.update {
                        if (append) {
                            it.copy(
                                isLoadingMore = false,
                                errorMessage = error.message ?: DEFAULT_ERROR_MESSAGE,
                            )
                        } else {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: DEFAULT_ERROR_MESSAGE,
                            )
                        }
                    }
                }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun applyPage(page: MyIssuePage, append: Boolean) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isLoadingMore = false,
                issues = if (append) {
                    it.issues + page.items
                } else {
                    page.items
                },
                hasNext = page.hasNext,
                nextCursor = page.nextCursor,
                errorMessage = null,
            )
        }
    }

    companion object {
        const val LOAD_MORE_THRESHOLD = 2
        private const val DEFAULT_ERROR_MESSAGE = "내 이슈를 불러오는 중 오류가 발생했습니다."
    }
}
