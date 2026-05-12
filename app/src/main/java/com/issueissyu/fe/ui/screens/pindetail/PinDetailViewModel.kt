package com.issueissyu.fe.ui.screens.pindetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.data.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class PinDetailTab {
    HOME,
    POST,
    RESOLUTION
}

data class PinDetailUiState(
    val isLoading: Boolean = false,
    val pin: Pin? = null,
    val selectedTab: PinDetailTab = PinDetailTab.HOME,
    val errorMessage: String? = null,
    val demoComments: List<DemoCommentUiModel> = InitialDemoComments
)

sealed interface PinDetailEvent {
    data object Deleted : PinDetailEvent
}

@HiltViewModel
class PinDetailViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinDetailUiState())
    val uiState: StateFlow<PinDetailUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<PinDetailEvent>()
    val event = _event.asSharedFlow()

    fun loadPin(pinId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                pinRepository.getPinById(pinId)
            }.onSuccess { pin ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pin = pin,
                        errorMessage = if (pin == null) "핀 정보를 찾을 수 없습니다." else null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message?.takeIf { msg -> msg.isNotBlank() }
                            ?: "핀 정보를 불러오지 못했습니다."
                    )
                }
            }
        }
    }

    fun selectTab(tab: PinDetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleSympathy(pinId: String) {
        viewModelScope.launch {
            runCatching {
                pinRepository.toggleSympathy(pinId)
            }.onSuccess { updatedPin ->
                _uiState.update {
                    it.copy(pin = updatedPin, errorMessage = null)
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "공감 처리에 실패했습니다.")
                }
            }
        }
    }

    fun petitionPin(pinId: String) {
        viewModelScope.launch {
            runCatching {
                pinRepository.petitionPin(pinId)
            }.onSuccess { updatedPin ->
                _uiState.update {
                    it.copy(pin = updatedPin, errorMessage = null)
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "청원 처리에 실패했습니다.")
                }
            }
        }
    }

    fun joinResolver(pinId: String, currentUserId: String) {
        viewModelScope.launch {
            runCatching {
                pinRepository.joinResolver(pinId, currentUserId)
            }.onSuccess { updatedPin ->
                _uiState.update {
                    it.copy(pin = updatedPin, errorMessage = null)
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "시민해결사 참여에 실패했습니다.")
                }
            }
        }
    }

    fun submitDemoComment(content: String) {
        val trimmed = content.trim()
        if (trimmed.isBlank()) return

        val newComment = DemoCommentUiModel(
            id = UUID.randomUUID().toString(),
            authorId = DemoMyAuthorId,
            authorName = "나",
            content = trimmed,
            createdAt = "방금 전"
        )

        _uiState.update {
            it.copy(demoComments = it.demoComments + newComment)
        }
    }

    fun deletePinForDemo(pinId: String) {
        viewModelScope.launch {
            runCatching {
                pinRepository.deletePinForDemo(pinId)
            }.onSuccess {
                // TODO: 실제 삭제 API 연결 후 서버 삭제 성공 시 화면 복귀.
                _event.emit(PinDetailEvent.Deleted)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "핀 삭제에 실패했습니다.")
                }
            }
        }
    }
}
