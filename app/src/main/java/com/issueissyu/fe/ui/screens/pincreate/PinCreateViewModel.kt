package com.issueissyu.fe.ui.screens.pincreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.model.CreatePinRequest
import com.issueissyu.fe.data.model.PinCoordinate
import com.issueissyu.fe.data.model.PinCategory
import com.issueissyu.fe.data.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DemoAddress = "서울 광진구 능동로 120"
private const val DemoLocationName = "건국대학교 입구"

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
    val showAiWritingDialog: Boolean = false,
    val remainingAiWriteCount: Int = 3,
    val errorMessage: String? = null
)

sealed interface PinCreateEvent {
    data class Created(val pinId: String) : PinCreateEvent
}

@HiltViewModel
class PinCreateViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

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
                userLng = userLng,
                // TODO: 실제 구현에서는 선택 좌표를 기반으로 주소를 조회해 표시한다.
                address = it.address.ifBlank { DemoAddress },
                locationName = it.locationName ?: DemoLocationName
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

    fun openAiWritingDialog() {
        _uiState.update { it.copy(showAiWritingDialog = true) }
    }

    fun dismissAiWritingDialog() {
        _uiState.update { it.copy(showAiWritingDialog = false) }
    }

    fun generateAiDraftForDemo() {
        val current = _uiState.value
        if (current.remainingAiWriteCount <= 0) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showAiWritingDialog = false,
                    isGeneratingAiContent = true,
                    errorMessage = null
                )
            }

            // 시연용 딜레이
            delay(1500)

            val aiDraft = "주변 주민들이 불편을 겪고 있는 문제입니다. 빠른 확인과 조치가 필요합니다."

            _uiState.update {
                it.copy(
                    description = aiDraft,
                    isGeneratingAiContent = false,
                    remainingAiWriteCount = (it.remainingAiWriteCount - 1).coerceAtLeast(0)
                )
            }
        }
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

        val category = state.category
        if (category == null) {
            _uiState.update { it.copy(errorMessage = "핀 종류를 확인하지 못했습니다.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, errorMessage = null)
            }

            runCatching {
                val request = CreatePinRequest(
                    category = category,
                    title = state.title.trim(),
                    description = state.description.trim(),
                    coordinate = PinCoordinate(
                        latitude = state.pinLat ?: 0.0,
                        longitude = state.pinLng ?: 0.0
                    ),
                    address = state.address.ifBlank { DemoAddress },
                    locationName = state.locationName ?: DemoLocationName,
                    imageUrls = emptyList()
                )

                // TODO: 실제 API 연결 시 이미지 업로드 후 반환된 imageUrls로 생성 요청 구성.
                // TODO: 선택된 말투를 AI 초안 요청/생성 요청 파라미터에 포함.
                pinRepository.createPin(request)
            }.onSuccess { createdPin ->
                _event.emit(PinCreateEvent.Created(createdPin.id))
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        errorMessage = throwable.message?.takeIf { message -> message.isNotBlank() }
                            ?: "핀 생성에 실패했습니다."
                    )
                }
            }.also {
                _uiState.update {
                    it.copy(isSubmitting = false)
                }
            }
        }
    }
}
