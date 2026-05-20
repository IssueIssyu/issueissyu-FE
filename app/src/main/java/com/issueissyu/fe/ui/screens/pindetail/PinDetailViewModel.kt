package com.issueissyu.fe.ui.screens.pindetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.pin.Pin
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
import javax.inject.Inject

enum class PinDetailTab {
    HOME,
    POST,
    RESOLUTION
}

data class PinDetailUiState(
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val pin: Pin? = null,
    val selectedTab: PinDetailTab = PinDetailTab.HOME,
    val errorMessage: String? = null,
)

sealed interface PinDetailEffect {
    data class ShowToast(val message: String) : PinDetailEffect
}

@HiltViewModel
class PinDetailViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinDetailUiState())
    val uiState: StateFlow<PinDetailUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<PinDetailEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<PinDetailEffect> = _effect.asSharedFlow()

    fun loadPin(pinId: String) {
        val id = pinId.toLongOrNull()
        if (id == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    pin = null,
                    errorMessage = "잘못된 핀 ID입니다.",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            pinRepository.getPinDetailHome(id)
                .onSuccess { pin ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pin = pin,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pin = null,
                            errorMessage = throwable.message?.takeIf { msg -> msg.isNotBlank() }
                                ?: "핀 정보를 불러오지 못했습니다.",
                        )
                    }
                }
        }
    }

    fun deletePin(pinId: String, onSuccess: () -> Unit) {
        if (_uiState.value.isDeleting) return

        val id = pinId.toLongOrNull() ?: run {
            emitToast("잘못된 핀 ID입니다.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }

            pinRepository.deletePin(id)
                .onSuccess {
                    _uiState.update { it.copy(isDeleting = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isDeleting = false) }
                    emitToast(
                        e.message?.takeIf { it.isNotBlank() } ?: "핀 삭제에 실패했습니다.",
                    )
                }
        }
    }

    private fun emitToast(message: String) {
        viewModelScope.launch {
            _effect.emit(PinDetailEffect.ShowToast(message))
        }
    }

    fun selectTab(tab: PinDetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
