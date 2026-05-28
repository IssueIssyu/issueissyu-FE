package com.issueissyu.fe.ui.screens.pincreate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.core.constants.PinImageUploadConstraints
import com.issueissyu.fe.core.media.PinImageUploadValidator
import com.issueissyu.fe.domain.model.pin.CreatePinRequest
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.domain.repository.IssueRepository
import com.issueissyu.fe.domain.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val mainImageUri: String? = null,
    val selectedTone: String? = null,
    val isSubmitting: Boolean = false,
    val isGeneratingAiContent: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PinCreateViewModel @Inject constructor(
    private val issueRepository: IssueRepository,
    private val pinRepository: PinRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinCreateUiState())
    val uiState: StateFlow<PinCreateUiState> = _uiState.asStateFlow()

    private val _createdEvents = MutableSharedFlow<String>()
    val createdEvents = _createdEvents.asSharedFlow()

    fun initialize(
        category: PinCategory,
        pinLat: Double,
        pinLng: Double,
        userLat: Double,
        userLng: Double,
        address: String,
    ) {
        val current = _uiState.value
        if (current.category == category &&
            current.pinLat == pinLat &&
            current.pinLng == pinLng &&
            current.userLat == userLat &&
            current.userLng == userLng &&
            current.address == address
        ) {
            return
        }
        _uiState.update {
            it.copy(
                category = category,
                pinLat = pinLat,
                pinLng = pinLng,
                userLat = userLat,
                userLng = userLng,
                address = address,
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

    fun addImageUris(uris: List<String>) {
        if (uris.isEmpty()) return

        val state = _uiState.value
        if (state.imageUris.size + uris.size > PinImageUploadConstraints.MAX_COUNT) {
            _uiState.update {
                it.copy(errorMessage = "사진은 최대 ${PinImageUploadConstraints.MAX_COUNT}장까지 첨부할 수 있습니다.")
            }
            return
        }

        val merged = (state.imageUris + uris)
            .distinct()
            .take(PinImageUploadConstraints.MAX_COUNT)

        PinImageUploadValidator.validate(context, merged).onFailure { error ->
            _uiState.update {
                it.copy(
                    errorMessage = error.message?.takeIf { message -> message.isNotBlank() }
                        ?: "첨부한 사진을 확인할 수 없습니다.",
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                imageUris = merged,
                mainImageUri = it.mainImageUri?.takeIf { mainUri -> mainUri in merged } ?: merged.firstOrNull(),
                errorMessage = null,
            )
        }
    }

    fun removeImageUri(uri: String) {
        _uiState.update { state ->
            val remaining = state.imageUris.filterNot { it == uri }
            state.copy(
                imageUris = remaining,
                mainImageUri = state.mainImageUri
                    ?.takeIf { mainUri -> mainUri in remaining }
                    ?: remaining.firstOrNull(),
            )
        }
    }

    fun setMainImageUri(uri: String) {
        if (uri !in _uiState.value.imageUris) return
        _uiState.update { it.copy(mainImageUri = uri) }
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
            category == PinCategory.ISSUE && state.imageUris.isEmpty() -> {
                _uiState.update { it.copy(errorMessage = "이슈 핀은 사진을 최소 1장 첨부해야 합니다.") }
                return
            }
        }

        PinImageUploadValidator.validate(context, state.imageUris).onFailure { error ->
            _uiState.update {
                it.copy(
                    errorMessage = error.message?.takeIf { message -> message.isNotBlank() }
                        ?: "첨부한 사진을 확인할 수 없습니다.",
                )
            }
            return
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
                    mainImageUri = state.mainImageUri,
                    tone = state.selectedTone ?: DEFAULT_AI_TONE,
                )
            ).onSuccess { createdPin ->
                _uiState.update { it.copy(isSubmitting = false, errorMessage = null) }
                _createdEvents.emit(createdPin.id)
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
