package com.issueissyu.fe.ui.screens.map

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.domain.model.billing.BillingPurchaseEvent
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.MapNotice
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.MapPinMarker
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.domain.model.pin.PinEmojiCandidate
import com.issueissyu.fe.domain.model.pin.PinEmojiReaction
import com.issueissyu.fe.domain.model.pin.PinEmojis
import com.issueissyu.fe.domain.repository.LocationRepository
import com.issueissyu.fe.domain.repository.BillingRepository
import com.issueissyu.fe.domain.repository.MapRepository
import com.issueissyu.fe.domain.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import com.naver.maps.geometry.LatLng
import javax.inject.Inject

data class PinCreationNavigationEvent(
    val category: PinCategory,
    val pinCoordinate: PinCoordinate,
    val userCoordinate: PinCoordinate,
    val address: String,
)

data class MapEmojiPickerUiState(
    val targetPinId: Long? = null,
    val candidates: List<PinEmojiCandidate> = emptyList(),
    val selectedEmojiId: Long? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val isVisible: Boolean
        get() = targetPinId != null
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapRepository: MapRepository,
    private val pinRepository: PinRepository,
    private val billingRepository: BillingRepository,
    private val locationRepository: LocationRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    val currentUserId: String
        get() = tokenManager.getCurrentUserUuid().orEmpty()

    private val _mapPins = MutableStateFlow<List<MapPinMarker>>(emptyList())
    val mapPins: StateFlow<List<MapPinMarker>> = _mapPins.asStateFlow()

    private val _selectedPin = MutableStateFlow<Pin?>(null)
    val selectedPin: StateFlow<Pin?> = _selectedPin.asStateFlow()

    private val _focusPin = MutableSharedFlow<Pin>(extraBufferCapacity = 1)
    val focusPin = _focusPin.asSharedFlow()

    private val _selectedCategory = MutableStateFlow<PinCategory?>(null)
    val selectedCategory: StateFlow<PinCategory?> = _selectedCategory.asStateFlow()

    private val _notices = MutableStateFlow<List<MapNotice>>(emptyList())
    val notices: StateFlow<List<MapNotice>> = _notices.asStateFlow()

    private val _showResearchButton = MutableStateFlow(false)
    val showResearchButton: StateFlow<Boolean> = _showResearchButton.asStateFlow()

    private val _showPinTypeSelector = MutableStateFlow(false)
    val showPinTypeSelector: StateFlow<Boolean> = _showPinTypeSelector.asStateFlow()

    private val _currentBounds = MutableStateFlow<MapBounds?>(null)
    @Suppress("unused") // TODO: UI에서 bounds를 관찰할 계획이면 유지하되 @Suppress("unused")를 붙여도 됩니다.
    val currentBounds: StateFlow<MapBounds?> = _currentBounds.asStateFlow()

    private val _isLocationSelectionMode = MutableStateFlow(false)
    val isLocationSelectionMode: StateFlow<Boolean> = _isLocationSelectionMode.asStateFlow()

    private val _selectedPinCategory = MutableStateFlow<PinCategory?>(null)
    val selectedPinCategory: StateFlow<PinCategory?> = _selectedPinCategory.asStateFlow()

    private val _selectedPinCoordinate = MutableStateFlow<PinCoordinate?>(null)
    val selectedPinCoordinate: StateFlow<PinCoordinate?> = _selectedPinCoordinate.asStateFlow()

    private val _currentLocation = MutableStateFlow<PinCoordinate?>(null)
    val currentLocation: StateFlow<PinCoordinate?> = _currentLocation.asStateFlow()

    private val _navigateToPinCreation = MutableSharedFlow<PinCreationNavigationEvent>()
    val navigateToPinCreation = _navigateToPinCreation.asSharedFlow()

    private val _messageEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messageEvents = _messageEvents.asSharedFlow()

    private val _emojiPickerUiState = MutableStateFlow(MapEmojiPickerUiState())
    val emojiPickerUiState: StateFlow<MapEmojiPickerUiState> = _emojiPickerUiState.asStateFlow()
    private var pendingBillingProductId: String? = null

    init {
        loadNotices()
        observeBillingPurchaseEvents()
    }

    private fun loadNotices() {
        viewModelScope.launch {
            mapRepository.getMapNotices()
                .onSuccess { _notices.value = it }
        }
    }


    fun clearSelectedPin() {
        _selectedPin.value = null
    }

    fun onCategorySelected(categoryName: String?) {
        // TODO: 추후 CategoryButtons에서 PinCategory를 직접 전달하도록 변경
        _selectedCategory.value = when (categoryName) {
            "이슈" -> PinCategory.ISSUE
            "소통" -> PinCategory.COMMUNICATION
            "가게" -> PinCategory.SHOP
            "축제" -> PinCategory.FESTIVAL
            else -> null
        }
        fetchPinsInBounds()
    }

    fun openPinTypeSelector() {
        _showPinTypeSelector.value = true
    }

    fun closePinTypeSelector() {
        _showPinTypeSelector.value = false
    }

    fun showResearchAreaButton() {
        _showResearchButton.value = true
    }

    fun hideResearchAreaButton() {
        _showResearchButton.value = false
    }

    fun updateMapBounds(bounds: MapBounds) {
        val isInitialBounds = _currentBounds.value == null
        _currentBounds.value = bounds
        if (isInitialBounds) {
            fetchPinsInBounds()
        } else {
            showResearchAreaButton()
        }
    }

    fun fetchPinsInBounds() {
        val bounds = _currentBounds.value ?: return

        viewModelScope.launch {
            mapRepository.getMapPinsInBounds(bounds, _selectedCategory.value)
                .onSuccess { _mapPins.value = it }

            hideResearchAreaButton()
        }
    }

    fun toggleSympathy(pinId: String) {
        val pin = _selectedPin.value?.takeIf { it.id == pinId } ?: return
        if (pin.isSympathizedByMe) return
        val numericPinId = pinId.toLongOrNull() ?: return

        viewModelScope.launch {
            pinRepository.likePin(numericPinId)
                .onSuccess { like ->
                    val currentPin = _selectedPin.value?.takeIf { it.id == pinId } ?: return@onSuccess
                    _selectedPin.value = currentPin.copy(
                        isSympathizedByMe = like.isLike,
                        sympathyCount = like.pinLikeCount,
                    )
                }
                .onFailure { e ->
                    _messageEvents.emit(e.message?.takeIf { it.isNotBlank() } ?: "핀 공감에 실패했습니다.")
                }
        }
    }

    fun deletePin(pinId: String) {
        val numericPinId = pinId.toLongOrNull() ?: return

        viewModelScope.launch {
            pinRepository.deletePin(numericPinId)
                .onSuccess {
                    _mapPins.value = _mapPins.value.filterNot { it.pinId == pinId }

                    if (_selectedPin.value?.id == pinId) {
                        _selectedPin.value = null
                    }
                }
                .onFailure { e ->
                    _messageEvents.emit(e.message?.takeIf { it.isNotBlank() } ?: "핀 삭제에 실패했습니다.")
                }
        }
    }

    fun selectPinById(pinId: String) {
        viewModelScope.launch {
            val pin = mapRepository.getPinCard(pinId).getOrNull() ?: return@launch
            _selectedPin.value = pin
            loadPinEmojis(pinId)
        }
    }

    fun focusPinById(pinId: String) {
        viewModelScope.launch {
            val pin = mapRepository.getPinCard(pinId).getOrNull() ?: return@launch
            _selectedPin.value = pin
            _focusPin.emit(pin)
            loadPinEmojis(pinId)
        }
    }

    private suspend fun loadPinEmojis(pinId: String) {
        val numericPinId = pinId.toLongOrNull() ?: return
        pinRepository.getPinEmojis(numericPinId)
            .onSuccess { pinEmojis ->
                val currentPin = _selectedPin.value?.takeIf { it.id == pinId } ?: return@onSuccess
                _selectedPin.value = currentPin.copy(
                    emojiReactions = pinEmojis.toEmojiReactions()
                )
            }
    }

    private fun PinEmojis.toEmojiReactions(): List<PinEmojiReaction> {
        return emojis
            .filter { it.count > 0 }
            .map { emoji ->
                PinEmojiReaction(
                    emojiId = emoji.emojiId.toString(),
                    count = emoji.count,
                    reactedByMe = emoji.emojiId == selectedEmojiId,
                    emojiImageUrl = emoji.emojiImageUrl,
                )
            }
    }

    fun openEmojiPicker(pinId: String) {
        val numericPinId = pinId.toLongOrNull() ?: return
        val selectedEmojiId = _selectedPin.value
            ?.takeIf { it.id == pinId }
            ?.emojiReactions
            ?.firstOrNull { it.reactedByMe }
            ?.emojiId
            ?.toLongOrNull()

        _emojiPickerUiState.value = MapEmojiPickerUiState(
            targetPinId = numericPinId,
            selectedEmojiId = selectedEmojiId,
            isLoading = true,
        )

        viewModelScope.launch {
            refreshEmojiCandidates()
        }
    }

    fun closeEmojiPicker() {
        _emojiPickerUiState.value = MapEmojiPickerUiState()
    }

    fun selectEmojiCandidate(emojiId: Long) {
        val candidate = _emojiPickerUiState.value.candidates.firstOrNull { it.emojiId == emojiId }
            ?: return
        if (!candidate.canReact) return
        val nextSelection = if (_emojiPickerUiState.value.selectedEmojiId == emojiId) null else emojiId
        _emojiPickerUiState.value = _emojiPickerUiState.value.copy(selectedEmojiId = nextSelection)
    }

    fun purchaseEmoji(activity: Activity?, emojiId: Long) {
        val candidate = _emojiPickerUiState.value.candidates.firstOrNull { it.emojiId == emojiId }
            ?: return
        if (candidate.canReact || pendingBillingProductId != null) return
        val productId = candidate.productId ?: run {
            _messageEvents.tryEmit("구매 정보를 찾을 수 없습니다.")
            return
        }
        if (activity == null) {
            _messageEvents.tryEmit("결제 화면을 열 수 없습니다.")
            return
        }
        pendingBillingProductId = productId
        viewModelScope.launch {
            billingRepository.purchaseProduct(activity, productId)
                .onFailure { error ->
                    pendingBillingProductId = null
                    _messageEvents.emit(error.message ?: "결제창을 열지 못했습니다.")
                }
        }
    }

    private fun observeBillingPurchaseEvents() {
        viewModelScope.launch {
            billingRepository.purchaseEvents.collect { event ->
                if (event.productId != pendingBillingProductId) return@collect
                when (event) {
                    is BillingPurchaseEvent.Verified -> {
                        pendingBillingProductId = null
                        refreshEmojiCandidates()
                        _messageEvents.emit("이모지를 구매했습니다.")
                    }
                    is BillingPurchaseEvent.Pending -> {
                        _messageEvents.emit("결제가 대기 중입니다.")
                    }
                    is BillingPurchaseEvent.Canceled -> {
                        pendingBillingProductId = null
                    }
                    is BillingPurchaseEvent.Failed -> {
                        pendingBillingProductId = null
                        _messageEvents.emit(event.message)
                    }
                }
            }
        }
    }

    private suspend fun refreshEmojiCandidates() {
        pinRepository.getEmojiCandidates()
            .onSuccess { candidates ->
                _emojiPickerUiState.value = _emojiPickerUiState.value.copy(
                    candidates = candidates,
                    isLoading = false,
                    errorMessage = null,
                )
            }
            .onFailure { error ->
                _emojiPickerUiState.value = _emojiPickerUiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message?.takeIf { it.isNotBlank() }
                        ?: "이모지 목록을 불러오지 못했습니다.",
                )
            }
    }

    fun applySelectedEmoji() {
        val picker = _emojiPickerUiState.value
        val pinId = picker.targetPinId ?: return
        val selectedEmojiId = picker.selectedEmojiId
        val selectedCandidate = selectedEmojiId?.let { emojiId ->
            picker.candidates.firstOrNull { it.emojiId == emojiId }
        }
        if (selectedCandidate?.canReact == false || picker.isSubmitting) return

        viewModelScope.launch {
            _emojiPickerUiState.value = _emojiPickerUiState.value.copy(isSubmitting = true)
            pinRepository.applyPinEmojiFromPicker(pinId, selectedEmojiId)
                .onSuccess {
                    loadPinEmojis(pinId.toString())
                    closeEmojiPicker()
                }
                .onFailure { error ->
                    _emojiPickerUiState.value = _emojiPickerUiState.value.copy(isSubmitting = false)
                    _messageEvents.emit(
                        error.message?.takeIf { it.isNotBlank() } ?: "이모지 반응 등록에 실패했습니다.",
                    )
                }
        }
    }

    fun enterLocationSelectionMode(category: PinCategory) {
        _isLocationSelectionMode.value = true
        _selectedPinCategory.value = category
        _selectedPinCoordinate.value = null
        _selectedPin.value = null
    }

    fun exitLocationSelectionMode() {
        _isLocationSelectionMode.value = false
        _selectedPinCategory.value = null
        _selectedPinCoordinate.value = null
        closePinTypeSelector()
    }

    fun onMapCoordinateSelected(selectedCoordinate: PinCoordinate, currentCoordinate: PinCoordinate?) {
        val selectedCategory = _selectedPinCategory.value
        if (!_isLocationSelectionMode.value || selectedCategory == null) return
        if (currentCoordinate == null) {
            _messageEvents.tryEmit("현재 위치를 확인한 뒤 다시 시도해주세요.")
            return
        }

        viewModelScope.launch {
            locationRepository.checkPinCreationAvailable(
                userCoordinate = currentCoordinate,
                pinCoordinate = selectedCoordinate,
            ).onSuccess { address ->
                _navigateToPinCreation.emit(
                    PinCreationNavigationEvent(
                        category = selectedCategory,
                        pinCoordinate = selectedCoordinate,
                        userCoordinate = currentCoordinate,
                        address = address,
                    )
                )
                exitLocationSelectionMode()
            }.onFailure { e ->
                _messageEvents.emit(e.message?.takeIf { it.isNotBlank() } ?: "이 위치에는 핀을 생성할 수 없습니다.")
                // 생성 불가 위치를 선택한 경우 위치 선택 모드를 종료해 핀 종류 선택부터 다시 진행하도록 한다.
                exitLocationSelectionMode()
            }
        }
    }

    fun updateCurrentLocation(latLng: LatLng) {
        _currentLocation.value = PinCoordinate(latitude = latLng.latitude, longitude = latLng.longitude)
    }
}
