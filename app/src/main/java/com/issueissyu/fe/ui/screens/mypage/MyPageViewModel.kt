package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.collection.CollectionPageSummary
import com.issueissyu.fe.domain.repository.AuthRepository
import com.issueissyu.fe.domain.repository.UserCollectionsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Pin(
    val id: String,
    val name: String,
    val imageUrl: String,
)

data class MyPageUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val bookmarkedPins: List<Pin> = emptyList(),
)

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val userCollectionsStore: UserCollectionsStore,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    private val _authActionState = MutableStateFlow<AuthActionState>(AuthActionState.Idle)
    val authActionState = _authActionState.asStateFlow()

    private var loadCollectionsJob: Job? = null

    init {
        viewModelScope.launch {
            userCollectionsStore.snapshot.collect { summary ->
                summary?.let { applySummary(it) }
            }
        }
        loadCollections(force = false)
    }

    fun loadCollections(force: Boolean = true) {
        loadCollectionsJob?.cancel()
        loadCollectionsJob = viewModelScope.launch {
            val hasCachedData = userCollectionsStore.snapshot.value != null
            _uiState.update {
                it.copy(
                    isLoading = !hasCachedData,
                    errorMessage = null,
                )
            }

            userCollectionsStore.refreshForMyPage(force = force)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: LOAD_COLLECTIONS_ERROR_MESSAGE,
                        )
                    }
                }
        }
    }

    private fun applySummary(summary: CollectionPageSummary) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = null,
                nickname = summary.nickname,
                profileImageUrl = summary.profileImageUrl,
                bookmarkedPins = summary.bookmarkedCollections.map { item ->
                    Pin(
                        id = item.collectionId.toString(),
                        name = item.name,
                        imageUrl = item.imageUrl,
                    )
                },
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authActionState.value = AuthActionState.Loading
            authRepository.logout()
                .onSuccess { _authActionState.value = AuthActionState.Success }
                .onFailure { error ->
                    _authActionState.value = AuthActionState.Error(
                        error.message ?: LOGOUT_ERROR_MESSAGE,
                    )
                }
        }
    }

    fun withdraw() {
        viewModelScope.launch {
            _authActionState.value = AuthActionState.Loading
            authRepository.withdraw()
                .onSuccess { _authActionState.value = AuthActionState.Success }
                .onFailure { error ->
                    _authActionState.value = AuthActionState.Error(
                        error.message ?: WITHDRAW_ERROR_MESSAGE,
                    )
                }
        }
    }

    fun resetAuthActionState() {
        _authActionState.value = AuthActionState.Idle
    }

    sealed class AuthActionState {
        data object Idle : AuthActionState()
        data object Loading : AuthActionState()
        data object Success : AuthActionState()
        data class Error(val message: String) : AuthActionState()
    }

    companion object {
        private const val LOAD_COLLECTIONS_ERROR_MESSAGE = "마이페이지 정보를 불러오지 못했습니다."
        private const val LOGOUT_ERROR_MESSAGE = "로그아웃에 실패했습니다."
        private const val WITHDRAW_ERROR_MESSAGE = "회원탈퇴에 실패했습니다."
    }
}
