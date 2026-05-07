package com.issueissyu.fe.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.model.MapBounds
import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.data.model.PinCategory
import com.issueissyu.fe.data.model.MapPinMarker
import com.issueissyu.fe.data.model.PinCoordinate
import com.issueissyu.fe.data.repository.PinRepository
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
    val userCoordinate: PinCoordinate
)

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
        val bounds = _currentBounds.value ?: return

        viewModelScope.launch {
            _mapPins.value = pinRepository.getMapPinsInBounds(bounds)

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
    _mapPins.value = _mapPins.value.filterNot { it.pinId == pinId }

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
    viewModelScope.launch {
        val cachedPin = _pins.value.firstOrNull { it.id == pinId }
        _selectedPin.value = cachedPin ?: pinRepository.getPinById(pinId)
    }
}

    fun enterLocationSelectionMode(category: PinCategory) {
        _isLocationSelectionMode.value = true
        _selectedPinCategory.value = category
        _selectedPinCoordinate.value = null
    }

    fun exitLocationSelectionMode() {
        _isLocationSelectionMode.value = false
        _selectedPinCategory.value = null
        _selectedPinCoordinate.value = null
        closePinTypeSelector()
    }

    fun onMapCoordinateSelected(selectedCoordinate: PinCoordinate, currentCoordinate: PinCoordinate?) {
        if (!_isLocationSelectionMode.value || _selectedPinCategory.value == null) return
        if (currentCoordinate == null) {
            // TODO: 현재 위치를 가져오지 못한 경우 안내 UI 표시
            return
        }

        viewModelScope.launch {
            _navigateToPinCreation.emit(
                PinCreationNavigationEvent(
                    category = _selectedPinCategory.value!!,
                    pinCoordinate = selectedCoordinate,
                    userCoordinate = currentCoordinate
                )
            )
            exitLocationSelectionMode()
        }
    }

    fun updateCurrentLocation(latLng: LatLng) {
        _currentLocation.value = PinCoordinate(latitude = latLng.latitude, longitude = latLng.longitude)
    }
}