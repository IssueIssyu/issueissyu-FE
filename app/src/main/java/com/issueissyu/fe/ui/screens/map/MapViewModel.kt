package com.issueissyu.fe.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.MapNotice
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.MapPinMarker
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.domain.model.pin.PinEmojiReaction
import com.issueissyu.fe.domain.model.pin.PinEmojis
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
    val userCoordinate: PinCoordinate
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapRepository: MapRepository,
    private val pinRepository: PinRepository,
) : ViewModel() {

    private val _mapPins = MutableStateFlow<List<MapPinMarker>>(emptyList())
    val mapPins: StateFlow<List<MapPinMarker>> = _mapPins.asStateFlow()

    private val _selectedPin = MutableStateFlow<Pin?>(null)
    val selectedPin: StateFlow<Pin?> = _selectedPin.asStateFlow()

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

    init {
        loadNotices()
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
