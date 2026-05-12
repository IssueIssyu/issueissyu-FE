package com.issueissyu.fe.ui.screens.pinedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PinEditUiState(
    val isLoading: Boolean = false,
    val pinId: String = "",
    val title: String = "",
    val description: String = "",
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false
)

sealed interface PinEditEvent {
    data class Edited(val pinId: String) : PinEditEvent
}

@HiltViewModel
class PinEditViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinEditUiState())
    val uiState: StateFlow<PinEditUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<PinEditEvent>()
    val event = _event.asSharedFlow()

    fun loadPin(pinId: String) {
        val current = _uiState.value
        if (current.pinId == pinId && current.title.isNotBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                pinRepository.getPinById(pinId)
            }.onSuccess { pin ->
                _uiState.update {
                    if (pin == null) {
                        it.copy(
                            isLoading = false,
                            pinId = pinId,
                            errorMessage = "핀 정보를 찾을 수 없습니다."
                        )
                    } else {
                        it.copy(
                            isLoading = false,
                            pinId = pin.id,
                            title = pin.title,
                            description = pin.description,
                            errorMessage = null
                        )
                    }
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pinId = pinId,
                        errorMessage = throwable.message?.takeIf { message -> message.isNotBlank() }
                            ?: "핀 정보를 불러오지 못했습니다."
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, errorMessage = null) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value, errorMessage = null) }
    }

    fun submitEdit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val title = state.title.trim()
        val description = state.description.trim()

        if (title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "제목을 입력해 주세요.") }
            return
        }

        if (description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "상세 설명을 입력해 주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            runCatching {
                // TODO: 실제 API 연결 시 서버 수정 요청으로 교체.
                pinRepository.updatePinContentForDemo(
                    pinId = state.pinId,
                    title = title,
                    description = description
                )
            }.onSuccess { updatedPin ->
                _event.emit(PinEditEvent.Edited(updatedPin.id))
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "핀 수정에 실패했습니다.")
                }
            }.also {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }
}
