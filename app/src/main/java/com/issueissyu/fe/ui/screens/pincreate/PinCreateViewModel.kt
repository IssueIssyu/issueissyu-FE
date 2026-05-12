package com.issueissyu.fe.ui.screens.pincreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.model.PinCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PinCreateUiState(
    val category: PinCategory? = null,
    val pinLat: Double? = null,
    val pinLng: Double? = null,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val title: String = "",
    val description: String = "",
    val address: String = "",
    val locationName: String? = null,
    val imageUris: List<String> = emptyList(),
    val selectedTone: String? = null,
    val isSubmitting: Boolean = false,
    val isGeneratingAiContent: Boolean = false,
    val errorMessage: String? = null
)

sealed interface PinCreateEvent {
    data object Created : PinCreateEvent
}

@HiltViewModel
class PinCreateViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PinCreateUiState())
    val uiState: StateFlow<PinCreateUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<PinCreateEvent>()
    val event = _event.asSharedFlow()

    fun initialize(
        category: PinCategory,
        pinLat: Double,
        pinLng: Double,
        userLat: Double,
        userLng: Double
    ) {
        val current = _uiState.value
        if (current.category == category &&
            current.pinLat == pinLat &&
            current.pinLng == pinLng &&
            current.userLat == userLat &&
            current.userLng == userLng
        ) {
            return
        }
        // TODO: 좌표 기반 주소 변환 결과를 address/locationName으로 표시
        _uiState.update {
            it.copy(
                category = category,
                pinLat = pinLat,
                pinLng = pinLng,
                userLat = userLat,
                userLng = userLng
            )
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, errorMessage = null) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value, errorMessage = null) }
    }

    fun onToneChange(value: String) {
        _uiState.update { it.copy(selectedTone = value) }
    }

    fun submitPin() {
        val state = _uiState.value

        if (state.isSubmitting) return

        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "제목을 입력해 주세요.") }
            return
        }

        if (state.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "상세 설명을 입력해 주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, errorMessage = null)
            }

            // TODO: PinRepository.createPin 연결 후 생성된 pinId 기반 상세 이동 또는 지도 새로고침 처리.
            // TODO: 이미지 업로드 후 반환된 imageUrls로 생성 요청 구성.
            // TODO: 선택된 말투를 AI 초안 요청/생성 요청 파라미터에 포함.
            _event.emit(PinCreateEvent.Created)

            _uiState.update {
                it.copy(isSubmitting = false)
            }
        }
    }
}
