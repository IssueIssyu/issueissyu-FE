package com.issueissyu.fe.ui.screens.pincreate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.core.constants.PinImageUploadConstraints
import com.issueissyu.fe.core.media.PinImageUploadValidator
import com.issueissyu.fe.domain.model.pin.CreatePinRequest
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinCreateException
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val toneOptions: List<String> = emptyList(),
    val selectedTone: String? = null,
    val isLoadingToneOptions: Boolean = false,
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

    private var lastFailedImageFingerprint: String? = null

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
        if (category == PinCategory.ISSUE) {
            loadIssueToneTypes()
        }
    }

    private fun loadIssueToneTypes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingToneOptions = true) }
            issueRepository.getIssueToneTypes()
                .onSuccess { tones ->
                    val labels = tones.map { it.label }
                    _uiState.update { state ->
                        state.copy(
                            toneOptions = labels,
                            selectedTone = state.selectedTone?.takeIf { it in labels }
                                ?: labels.firstOrNull(),
                            isLoadingToneOptions = false,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            toneOptions = FALLBACK_TONE_OPTIONS,
                            selectedTone = state.selectedTone?.takeIf { it in FALLBACK_TONE_OPTIONS }
                                ?: DEFAULT_AI_TONE,
                            isLoadingToneOptions = false,
                        )
                    }
                }
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

        val snapshot = _uiState.value
        val remaining = PinImageUploadConstraints.MAX_COUNT - snapshot.imageUris.size
        if (remaining <= 0) {
            _uiState.update {
                it.copy(errorMessage = "사진은 최대 ${PinImageUploadConstraints.MAX_COUNT}장까지 첨부할 수 있습니다.")
            }
            return
        }

        val newUris = uris
            .filter { uri -> uri !in snapshot.imageUris }
            .take(remaining)
        if (newUris.isEmpty()) return

        val merged = snapshot.imageUris + newUris

        viewModelScope.launch {
            val validation = runCatching {
                withContext(Dispatchers.IO) {
                    PinImageUploadValidator.validate(context, merged)
                }
            }.getOrElse { error ->
                Result.failure(
                    IllegalArgumentException(
                        error.message?.takeIf { message -> message.isNotBlank() }
                            ?: "첨부한 사진을 확인할 수 없습니다.",
                    ),
                )
            }

            validation.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message?.takeIf { message -> message.isNotBlank() }
                            ?: "첨부한 사진을 확인할 수 없습니다.",
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    imageUris = merged,
                    mainImageUri = it.mainImageUri?.takeIf { mainUri -> mainUri in merged } ?: merged.firstOrNull(),
                    errorMessage = null,
                )
            }
            clearImageUploadFailureTracking()
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
        clearImageUploadFailureTracking()
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

        viewModelScope.launch {
            val validation = runCatching {
                withContext(Dispatchers.IO) {
                    PinImageUploadValidator.validate(context, state.imageUris)
                }
            }.getOrElse { error ->
                Result.failure(
                    IllegalArgumentException(
                        error.message?.takeIf { message -> message.isNotBlank() }
                            ?: "첨부한 사진을 확인할 수 없습니다.",
                    ),
                )
            }

            validation.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message?.takeIf { message -> message.isNotBlank() }
                            ?: "첨부한 사진을 확인할 수 없습니다.",
                    )
                }
                return@launch
            }

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
                clearImageUploadFailureTracking()
                _uiState.update { it.copy(isSubmitting = false, errorMessage = null) }
                _createdEvents.emit(createdPin.id)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = resolveSubmitErrorMessage(e, state.imageUris),
                    )
                }
            }
        }
    }

    private fun resolveSubmitErrorMessage(error: Throwable, imageUris: List<String>): String {
        val pinCreateError = error as? PinCreateException
        val fingerprint = imageUris.sorted().joinToString("|")
        val isImageRelated = pinCreateError?.isImageRelated == true
        val isRepeatImageFailure = isImageRelated &&
            fingerprint.isNotEmpty() &&
            fingerprint == lastFailedImageFingerprint

        if (isImageRelated && fingerprint.isNotEmpty()) {
            lastFailedImageFingerprint = fingerprint
        }

        return when {
            isRepeatImageFailure ->
                "선택한 사진으로 등록에 실패했습니다. 다른 사진으로 다시 시도해주세요."
            else ->
                error.message?.takeIf { message -> message.isNotBlank() } ?: "핀 생성에 실패했습니다."
        }
    }

    private fun clearImageUploadFailureTracking() {
        lastFailedImageFingerprint = null
    }

    companion object {
        private const val DEFAULT_AI_TONE = "없음"
        private val FALLBACK_TONE_OPTIONS = listOf(
            "없음",
            "한줄요약형",
            "상황설명형",
            "개선요청형",
            "긴급요청형",
            "불편호소형",
        )
    }
}
