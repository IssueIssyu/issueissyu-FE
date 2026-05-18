package com.issueissyu.fe.ui.screens.pindetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val isLoading: Boolean = false,
    val pin: Pin? = null,
    val selectedTab: PinDetailTab = PinDetailTab.HOME,
    val errorMessage: String? = null
)

@HiltViewModel
class PinDetailViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinDetailUiState())
    val uiState: StateFlow<PinDetailUiState> = _uiState.asStateFlow()

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
}
