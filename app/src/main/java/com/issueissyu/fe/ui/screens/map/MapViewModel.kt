package com.issueissyu.fe.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.model.MapBounds
import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.data.model.PinCategory
import com.issueissyu.fe.data.model.MapPinMarker
import com.issueissyu.fe.data.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _pins = MutableStateFlow<List<Pin>>(emptyList())
    val pins: StateFlow<List<Pin>> = _pins.asStateFlow()

    private val _mapPins = MutableStateFlow<List<MapPinMarker>>(emptyList())
    val mapPins: StateFlow<List<MapPinMarker>> = _mapPins.asStateFlow()

    private val _selectedPin = MutableStateFlow<Pin?>(null)
    val selectedPin: StateFlow<Pin?> = _selectedPin.asStateFlow()

    private val _selectedCategory = MutableStateFlow<PinCategory?>(null)
    val selectedCategory: StateFlow<PinCategory?> = _selectedCategory.asStateFlow()

    private val _showResearchButton = MutableStateFlow(false)
    val showResearchButton: StateFlow<Boolean> = _showResearchButton.asStateFlow()

    private val _showPinTypeSelector = MutableStateFlow(false)
    val showPinTypeSelector: StateFlow<Boolean> = _showPinTypeSelector.asStateFlow()

    private val _currentBounds = MutableStateFlow<MapBounds?>(null)
    @Suppress("unused") // TODO: UI에서 bounds를 관찰할 계획이면 유지하되 @Suppress("unused")를 붙여도 됩니다.
    val currentBounds: StateFlow<MapBounds?> = _currentBounds.asStateFlow()

    private val _emojiTargetPinId = MutableStateFlow<String?>(null)
    @Suppress("unused") // TODO: 이모지 선택 BottomSheet 연결 시 사용 예정
    val emojiTargetPinId: StateFlow<String?> = _emojiTargetPinId.asStateFlow()

    init {
        loadPins()
    }

    private fun loadPins() {
        viewModelScope.launch {
            val loadedPins = pinRepository.getPins()
            _pins.value = loadedPins

            _mapPins.value = loadedPins.map { pin ->
                MapPinMarker(
                    pinId = pin.id,
                    category = pin.category,
                    coordinate = pin.coordinate,
                    address = pin.address,
                    locationName = pin.locationName ?: pin.address
                )
            }
        }
    }

    fun selectPin(pin: Pin) {
        _selectedPin.value = pin
    }

    fun clearSelectedPin() {
        _selectedPin.value = null
    }

    fun onCategorySelected(categoryName: String?) {
        _selectedCategory.value = when (categoryName) {
            "이슈" -> PinCategory.ISSUE
            "소통" -> PinCategory.COMMUNICATION
            "가게" -> PinCategory.SHOP
            "축제" -> PinCategory.FESTIVAL
            else -> null
        }
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
        _currentBounds.value = bounds
        showResearchAreaButton()
    }

    fun fetchPinsInBounds() {
        @Suppress("unused") // Property "bounds" is never used.
        val bounds = _currentBounds.value ?: return

        viewModelScope.launch {
            _mapPins.value = pinRepository.getMapPinsInBounds(
                swLat = bounds.swLat,
                swLng = bounds.swLng,
                neLat = bounds.neLat,
                neLng = bounds.neLng
            )

            hideResearchAreaButton()
        }
    }

    fun toggleSympathy(pinId: String) {
        _pins.value = _pins.value.map { pin ->
            if (pin.id != pinId) return@map pin

            val nextIsSympathized = !pin.isSympathizedByMe
            val nextCount = if (nextIsSympathized) {
                pin.sympathyCount + 1
            } else {
                (pin.sympathyCount - 1).coerceAtLeast(0)
            }

            pin.copy(
                isSympathizedByMe = nextIsSympathized,
                sympathyCount = nextCount
            )
        }

        _selectedPin.value = _pins.value.firstOrNull { it.id == pinId }
    }

    fun deletePinLocally(pinId: String) {
        _pins.value = _pins.value.filterNot { it.id == pinId }

        if (_selectedPin.value?.id == pinId) {
            _selectedPin.value = null
        }
        // TODO: 실제 삭제 API 연결 시 Repository.deletePin(pinId)로 교체
    }

    fun openEmojiSelector(pinId: String) {
        _emojiTargetPinId.value = pinId
        // TODO: 이모지 선택 BottomSheet 표시 상태 연결
    }

    @Suppress("unused") // TODO: 이모지 선택 BottomSheet 닫기 처리 연결 예정
    fun closeEmojiSelector() {
        _emojiTargetPinId.value = null
    }

    fun selectPinById(pinId: String) {
        _selectedPin.value = _pins.value.firstOrNull { it.id == pinId }
    }
}