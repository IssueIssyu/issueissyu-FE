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

// TODO: PinRepository 주입 후 createPin 호출 흐름 연결.
//       이번 커밋에서는 route argument 수신 + 입력 상태 관리만 담당하고 실제 제출은 TODO로 둔다.
@HiltViewModel
class PinCreateViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PinCreateUiState())
    val uiState: StateFlow<PinCreateUiState> = _uiState.asStateFlow()

    // route argument는 화면 진입 시 1회만 반영한다.
    // 같은 카테고리/좌표로 이미 초기화되어 있으면 사용자 입력을 덮어쓰지 않기 위해 무시한다.
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
        _uiState.update {
            it.copy(
                category = category,
                pinLat = pinLat,
                pinLng = pinLng,
                userLat = userLat,
                userLng = userLng
                // TODO: 좌표 → 주소 역지오코딩 결과를 받아 address/locationName 채우기.
            )
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    // TODO: 말투 설정 화면/BottomSheet 연결 후 실제 선택값(enum 또는 서버 정의 String) 전달.
    fun onToneChange(value: String) {
        _uiState.update { it.copy(selectedTone = value) }
    }

    // TODO: PinRepository.createPin(CreatePinRequest(...)) 연결.
    //       - 실행 전: isSubmitting=true
    //       - 성공 시: 화면 종료(NavGraph에서 popBackStack)
    //       - 실패 시: isSubmitting=false, errorMessage 반영
    //       AI 초안 생성도 별도 함수(generateAiDraft 등)로 분리 예정.
    fun submitPin() {
        // no-op
    }
}
