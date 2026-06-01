package com.issueissyu.fe.ui.screens.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.MapNotice
import com.issueissyu.fe.domain.model.collection.CollectionCharacter
import com.issueissyu.fe.domain.model.collection.CollectionPageSummary
import com.issueissyu.fe.domain.repository.MapRepository
import com.issueissyu.fe.domain.repository.UserCollectionsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 핀 데이터
data class PinItem(
    val collectionId: Long,
    val name: String,
    val imageUrl: String,
    val isLocked: Boolean = false,
    val isBookmarked: Boolean = false,
    val unlockCondition: String = "없음",
) {
    val id: String get() = collectionId.toString()
}

// UI 상태
data class CollectionUiState(
    val pins: List<PinItem> = emptyList(),
    val selectedPin: PinItem? = null,
    val currentProfilePin: PinItem? = null,
    val canUpdateProfile: Boolean = false,
    val isUpdatingProfile: Boolean = false,
    val bookmarkingCollectionId: Long? = null,
    val characterMessage: String = "새로운 친구가 생겼어!\n기대돼!",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val newlyUnlocked: List<NewUnlockItem> = emptyList(),
    val notices: List<MapNotice> = emptyList(),
)

data class NewUnlockItem(
    val name: String,
    val imageUrl: String,
)

// 이벤트
sealed class CollectionEvent {
    data class SelectPin(val pinId: String) : CollectionEvent()
    data class ToggleBookmark(val pinId: String) : CollectionEvent()
    object UpdateProfile : CollectionEvent()
    object DismissNewUnlockNotice : CollectionEvent()
    object RetryLoad : CollectionEvent()
    data class NoticeClicked(val pinId: String) : CollectionEvent()
}

sealed class CollectionEffect {
    data class ShowToast(val message: String) : CollectionEffect()
    data class ShowSnackbar(val message: String) : CollectionEffect()
    data class NavigateToPinDetail(val pinId: String) : CollectionEffect()
}

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val userCollectionsStore: UserCollectionsStore,
    private val mapRepository: MapRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<CollectionEffect>()
    val effects = _effects.asSharedFlow()

    private var loadJob: Job? = null
    private var resetSelectionOnNextSnapshot = false
    private var alignSelectionToProfileOnNextSnapshot = false
    private var unlockOverlayDismissed = false

    init {
        viewModelScope.launch {
            userCollectionsStore.snapshot.collect { summary ->
                summary?.let { syncFromSnapshot(it) }
            }
        }
        loadCollectionPage()
        loadNotices()
    }

    //이벤트 처리
    fun onEvent(event: CollectionEvent) {
        when (event) {
            is CollectionEvent.SelectPin -> selectPin(event.pinId)
            is CollectionEvent.ToggleBookmark -> toggleBookmark(event.pinId)
            is CollectionEvent.UpdateProfile -> updateProfile()
            is CollectionEvent.DismissNewUnlockNotice -> dismissNewUnlockNotice()
            is CollectionEvent.RetryLoad -> loadCollectionPage()
            is CollectionEvent.NoticeClicked -> handleNoticeClick(event.pinId)
        }
    }

    fun loadCollectionPage() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val hasCachedData = userCollectionsStore.snapshot.value != null
            resetSelectionOnNextSnapshot = true
            unlockOverlayDismissed = false
            _uiState.update {
                it.copy(
                    isLoading = !hasCachedData,
                    errorMessage = null,
                )
            }

            userCollectionsStore.refreshForCollectionTab()
                .onFailure { error ->
                    val message = error.message ?: LOAD_COLLECTION_PAGE_ERROR_MESSAGE
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = if (hasCachedData) null else message,
                        )
                    }
                    if (hasCachedData) {
                        emitSnackbar(message)
                    }
                }
        }
    }

    private fun syncFromSnapshot(summary: CollectionPageSummary) {
        val profileId = summary.profileCollection.collectionId
        val pins = summary.collections.map { it.toPinItem() }
        val profilePin = pins.find { it.collectionId == profileId }
            ?: summary.profileCollection.toPinItem()

        val current = _uiState.value
        val selectedPin = resolveSelectedPin(
            pins = pins,
            profilePin = profilePin,
            current = current,
        )
        val newlyUnlocked = if (unlockOverlayDismissed) {
            emptyList()
        } else {
            summary.newlyUnlocked.map { NewUnlockItem(name = it.name, imageUrl = it.imageUrl) }
        }
        val unlockNames = newlyUnlocked.map { it.name }
        val characterMessage = if (unlockNames.isNotEmpty()) {
            buildNewUnlockCharacterMessage(unlockNames)
        } else {
            DEFAULT_CHARACTER_MESSAGE
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = null,
                pins = pins,
                currentProfilePin = profilePin,
                selectedPin = selectedPin,
                canUpdateProfile = selectedPin?.collectionId != profilePin.collectionId,
                characterMessage = characterMessage,
                newlyUnlocked = newlyUnlocked,
            )
        }
    }

    private fun resolveSelectedPin(
        pins: List<PinItem>,
        profilePin: PinItem,
        current: CollectionUiState,
    ): PinItem {
        when {
            resetSelectionOnNextSnapshot -> {
                resetSelectionOnNextSnapshot = false
                return profilePin
            }

            alignSelectionToProfileOnNextSnapshot -> {
                alignSelectionToProfileOnNextSnapshot = false
                return profilePin
            }

            else -> {
                val selectedId = current.selectedPin?.collectionId
                return pins.find { it.collectionId == selectedId } ?: profilePin
            }
        }
    }

    private fun selectPin(pinId: String) {
        val pin = _uiState.value.pins.find { it.id == pinId } ?: return
        if (pin.isLocked) return

        // 선택한 핀 != 내 프로필 -> 버튼 활성화
        _uiState.update { state ->
            state.copy(
                selectedPin = pin,
                canUpdateProfile = pin.collectionId != state.currentProfilePin?.collectionId,
            )
        }
    }

    private fun toggleBookmark(pinId: String) {
        val state = _uiState.value
        val pin = state.pins.find { it.id == pinId } ?: return
        if (pin.isLocked || state.bookmarkingCollectionId != null) return

        val targetBookmarked = !pin.isBookmarked

        viewModelScope.launch {
            _uiState.update { it.copy(bookmarkingCollectionId = pin.collectionId) }
            try {
                userCollectionsStore.setBookmark(
                    collectionId = pin.collectionId,
                    isBookmarked = targetBookmarked,
                )
                    .onSuccess {
                        emitToast("북마크가 변경되었습니다")
                    }
                    .onFailure { error ->
                        emitToast(error.message ?: UPDATE_BOOKMARK_ERROR_MESSAGE)
                    }
            } finally {
                _uiState.update { current ->
                    if (current.bookmarkingCollectionId == pin.collectionId) {
                        current.copy(bookmarkingCollectionId = null)
                    } else {
                        current
                    }
                }
            }
        }
    }

    private fun updateProfile() {
        val state = _uiState.value
        val selected = state.selectedPin ?: return
        if (!state.canUpdateProfile || state.isUpdatingProfile) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingProfile = true) }

            userCollectionsStore.setProfile(selected.collectionId)
                .onSuccess {
                    alignSelectionToProfileOnNextSnapshot = true
                    _uiState.update {
                        it.copy(
                            isUpdatingProfile = false,
                            canUpdateProfile = false,
                        )
                    }
                    emitToast("프로필이 업데이트되었습니다")
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isUpdatingProfile = false) }
                    emitToast(error.message ?: UPDATE_PROFILE_ERROR_MESSAGE)
                }
        }
    }

    private fun loadNotices() {
        viewModelScope.launch {
            mapRepository.getMapNotices()
                .onSuccess { notices ->
                    _uiState.update { it.copy(notices = notices) }
                }
        }
    }

    private fun dismissNewUnlockNotice() {
        unlockOverlayDismissed = true
        _uiState.update { it.copy(newlyUnlocked = emptyList()) }
    }

    private fun handleNoticeClick(pinId: String) {
        if (pinId.isBlank()) return
        viewModelScope.launch {
            _effects.emit(CollectionEffect.NavigateToPinDetail(pinId))
        }
    }

    private fun emitToast(message: String) {
        viewModelScope.launch {
            _effects.emit(CollectionEffect.ShowToast(message))
        }
    }

    private fun emitSnackbar(message: String) {
        viewModelScope.launch {
            _effects.emit(CollectionEffect.ShowSnackbar(message))
        }
    }

    private fun CollectionCharacter.toPinItem(): PinItem {
        return PinItem(
            collectionId = collectionId,
            name = name,
            imageUrl = imageUrl,
            isLocked = isLocked,
            isBookmarked = isBookmarked,
            unlockCondition = unlockCondition,
        )
    }
    companion object {
        private const val DEFAULT_CHARACTER_MESSAGE = "새로운 친구가 생겼어!\n기대돼!"
        private const val LOAD_COLLECTION_PAGE_ERROR_MESSAGE = "컬렉션을 불러오지 못했습니다."
        private const val UPDATE_PROFILE_ERROR_MESSAGE = "프로필 업데이트에 실패했습니다."
        private const val UPDATE_BOOKMARK_ERROR_MESSAGE = "북마크 변경에 실패했습니다."
    }
}

private fun buildNewUnlockCharacterMessage(unlockNames: List<String>): String {
    val namesText = unlockNames.joinToString(", ")
    return "새로운 친구가 생겼어!\n$namesText"
}
