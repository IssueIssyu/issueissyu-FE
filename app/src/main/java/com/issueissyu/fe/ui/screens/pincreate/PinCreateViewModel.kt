package com.issueissyu.fe.ui.screens.pincreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.pin.CreatePinRequest
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.domain.repository.IssueRepository
import com.issueissyu.fe.domain.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
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

@HiltViewModel
class PinCreateViewModel @Inject constructor(
    private val issueRepository: IssueRepository,
    private val pinRepository: PinRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinCreateUiState())
    val uiState: StateFlow<PinCreateUiState> = _uiState.asStateFlow()

    private val _createdEvents = MutableSharedFlow<Unit>()
    val createdEvents = _createdEvents.asSharedFlow()

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
        _uiState.update { it.copy(title = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onToneChange(value: String) {
        _uiState.update { it.copy(selectedTone = value) }
    }

    fun createAiDraft() {
        val state = _uiState.value
        if (state.category != PinCategory.ISSUE || state.isGeneratingAiContent) return

        val pinLat = state.pinLat
        val pinLng = state.pinLng
        when {
            state.title.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "제목을 먼저 입력해주세요.") }
                return
            }
            state.description.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "상세 설명을 먼저 입력해주세요.") }
                return
            }
            pinLat == null || pinLng == null -> {
                _uiState.update { it.copy(errorMessage = "핀 위치 정보를 확인하지 못했습니다.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGeneratingAiContent = true,
                    errorMessage = null,
                )
            }

            issueRepository.createIssueAiDraft(
                title = state.title,
                content = state.description,
                tone = state.selectedTone ?: DEFAULT_AI_TONE,
                latitude = pinLat,
                longitude = pinLng,
            ).onSuccess { draft ->
                _uiState.update {
                    it.copy(
                        title = draft.title?.takeIf { title -> title.isNotBlank() } ?: it.title,
                        description = draft.content.orEmpty(),
                        isGeneratingAiContent = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isGeneratingAiContent = false,
                        errorMessage = e.message?.takeIf { message -> message.isNotBlank() } ?: "AI 글쓰기에 실패했습니다.",
                    )
                }
            }
        }
    }

    fun submitPin() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val category = state.category
        val pinLat = state.pinLat
        val pinLng = state.pinLng
        when {
            category == null -> {
                _uiState.update { it.copy(errorMessage = "핀 종류를 확인하지 못했습니다.") }
                return
            }
            state.title.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "제목을 입력해주세요.") }
                return
            }
            state.description.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "상세 설명을 입력해주세요.") }
                return
            }
            pinLat == null || pinLng == null -> {
                _uiState.update { it.copy(errorMessage = "핀 위치 정보를 확인하지 못했습니다.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            pinRepository.createPin(
                CreatePinRequest(
                    category = category,
                    title = state.title,
                    description = state.description,
                    coordinate = PinCoordinate(
                        latitude = pinLat,
                        longitude = pinLng,
                    ),
                    address = state.address,
                    locationName = state.locationName,
                    imageUris = state.imageUris,
                    tone = state.selectedTone ?: DEFAULT_AI_TONE,
                )
            ).onSuccess {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = null) }
                _createdEvents.emit(Unit)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.message?.takeIf { message -> message.isNotBlank() } ?: "핀 생성에 실패했습니다.",
                    )
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_AI_TONE = "없음"
    }
}
