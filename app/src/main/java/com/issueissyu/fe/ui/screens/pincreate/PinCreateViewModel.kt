package com.issueissyu.fe.ui.screens.pincreate

import androidx.lifecycle.ViewModel
import com.issueissyu.fe.data.model.PinCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
class PinCreateViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PinCreateUiState())
    val uiState: StateFlow<PinCreateUiState> = _uiState.asStateFlow()

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

    // TODO: PinRepository.createPin 연결 (isSubmitting / errorMessage 흐름 포함)
    fun submitPin() {
        // no-op
    }
}
