package com.issueissyu.fe.ui.screens.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// 핀 데이터
data class PinItem(
    val id: String,
    val name: String,
    val imageResId: Int,    //지금은 로컬로
    val imageUrl: String,  // 서버 저장용 URL
    val isLocked: Boolean = false,
    val unlockCondition: String? = null    //잠금 해제 조건 -> 아직 결정 사항 없음
)

// UI 상태
data class CollectionUiState(
    val pins: List<PinItem> = emptyList(),  //전체 목록
    val selectedPin: PinItem? = null,        // 선택된 핀
    val currentProfilePin: PinItem? = null,  // 현재 프로필 핀
    val bookmarkedPinIds: Set<String> = emptySet(), // 북마크 된 핀 ID 모음
    val canUpdateProfile: Boolean = false,   // 프로필 업데이트 버튼 활성화 여부
    val characterMessage: String = "새로운 친구가 생겼어!\n기대돼!",
    val isLoading: Boolean = false
)

// 이벤트
sealed class CollectionEvent {
    data class SelectPin(val pinId: String) : CollectionEvent()
    data class ToggleBookmark(val pinId: String) : CollectionEvent()
    object UpdateProfile : CollectionEvent()
    data class NoticeClicked(val notice: String) : CollectionEvent()
}

// 효과
sealed class CollectionEffect {
    data class ShowToast(val message: String) : CollectionEffect()
    data class NavigateToNoticeDetail(val notice: String) : CollectionEffect()
    object ProfileUpdatedSuccess : CollectionEffect()
}

@HiltViewModel
class CollectionViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<CollectionEffect>()
    val effects = _effects.asSharedFlow()

    init {
        loadInitialData()
    }

    //이벤트 처리
    fun onEvent(event: CollectionEvent) {
        when (event) {
            is CollectionEvent.SelectPin -> selectPin(event.pinId)
            is CollectionEvent.ToggleBookmark -> toggleBookmark(event.pinId)
            is CollectionEvent.UpdateProfile -> updateProfile()
            is CollectionEvent.NoticeClicked -> handleNoticeClick(event.notice)
        }
    }

    //초기 데이터
    private fun loadInitialData() {
        val mockPins = createMockPins()
        val defaultPin = mockPins.find { it.id == "pin_1" } //기본값 -> 더미

        _uiState.update {
            it.copy(
                pins = mockPins,
                currentProfilePin = defaultPin,
                selectedPin = defaultPin,
                canUpdateProfile = false    // 처음 -> 프로필인 핀 선택되어 있음 -> 비활성화
            )
        }
    }

    private fun selectPin(pinId: String) {
        val pin = _uiState.value.pins.find { it.id == pinId } ?: return

        // 선택한 핀 != 내 프로필 -> 버튼 활성화
        _uiState.update { state ->
            state.copy(
                selectedPin = pin,
                canUpdateProfile = pin.id != state.currentProfilePin?.id
            )
        }
    }

    private fun toggleBookmark(pinId: String) {
        _uiState.update { state ->
            val currentIds = state.bookmarkedPinIds.toMutableSet()

            if (pinId in currentIds) {
                currentIds.remove(pinId)
            } else {
                if (currentIds.size >= MAX_BOOKMARKED_COUNT) {     //임시로 설정
                    emitToast("최대 ${MAX_BOOKMARKED_COUNT}개까지 북마크 가능합니다")
                    return@update state
                }
                currentIds.add(pinId)
            }

            state.copy(bookmarkedPinIds = currentIds)
        }
    }

    private fun updateProfile() {
        val selected = _uiState.value.selectedPin ?: return

        // TODO: selected.imageUrl 전송
        _uiState.update { state ->
            state.copy(
                currentProfilePin = selected,
                canUpdateProfile = false
            )
        }

        viewModelScope.launch {
            _effects.emit(CollectionEffect.ProfileUpdatedSuccess)
            _effects.emit(CollectionEffect.ShowToast("프로필이 업데이트되었습니다"))
        }
    }

    private fun handleNoticeClick(notice: String) {
        viewModelScope.launch {
            _effects.emit(CollectionEffect.NavigateToNoticeDetail(notice))
        }
    }

    private fun emitToast(message: String) {
        viewModelScope.launch {
            _effects.emit(CollectionEffect.ShowToast(message))
        }
    }

    //Mock 데이터 - 나중에 받아올 아이들
    private fun createMockPins(): List<PinItem> {
        return listOf(
            PinItem("pin_1", "감자빵", R.drawable.img_character_potato, "https://api.issueissyu.com/pins/potato.png"),
            PinItem("pin_2", "아자쓰", R.drawable.ic_character_azass, "https://api.issueissyu.com/pins/azass.png"),
            PinItem("pin_3", "버터떡", R.drawable.img_character_butter, "https://api.issueissyu.com/pins/butter.png"),
            PinItem("pin_4", "바게트씨", R.drawable.img_character_baguette, "https://api.issueissyu.com/pins/baguette.png"),
            PinItem("pin_5", "음흉씨", R.drawable.ic_character_wickedness, "https://api.issueissyu.com/pins/wickedness.png", isLocked = true, unlockCondition = "할 일 10개 완료 시 해금"),
            PinItem("pin_6", "돌이곰", R.drawable.img_character_bear, "https://api.issueissyu.com/pins/bear.png"),
            PinItem("pin_7", "행복씨", R.drawable.ic_character_happy, "https://api.issueissyu.com/pins/happy.png"),
            PinItem("pin_8", "으쓱씨", R.drawable.ic_character_proud, "https://api.issueissyu.com/pins/proud.png"),
            PinItem("pin_9", "기본", R.drawable.ic_character_default, "https://api.issueissyu.com/pins/default.png"),
            PinItem("pin_10", "무관심씨", R.drawable.ic_character_unconcern, "https://api.issueissyu.com/pins/unconcern.png")
        )
    }
    companion object {
        private const val MAX_BOOKMARKED_COUNT = 4
    }
}