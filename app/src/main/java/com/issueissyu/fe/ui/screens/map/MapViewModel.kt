package com.issueissyu.fe.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.model.MapBounds
import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.data.model.PinCategory
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

    private val _selectedPin = MutableStateFlow<Pin?>(null)
    val selectedPin: StateFlow<Pin?> = _selectedPin.asStateFlow()

    private val _selectedCategory = MutableStateFlow<PinCategory?>(null)
    val selectedCategory: StateFlow<PinCategory?> = _selectedCategory.asStateFlow()

    private val _showResearchButton = MutableStateFlow(false)
    val showResearchButton: StateFlow<Boolean> = _showResearchButton.asStateFlow()

    private val _showPinTypeSelector = MutableStateFlow(false)
    val showPinTypeSelector: StateFlow<Boolean> = _showPinTypeSelector.asStateFlow()

    private val _currentBounds = MutableStateFlow<MapBounds?>(null)
    val currentBounds: StateFlow<MapBounds?> = _currentBounds.asStateFlow()

    init {
        loadPins()
    }

    private fun loadPins() {
        viewModelScope.launch {
            _pins.value = pinRepository.getPins()
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
        val bounds = _currentBounds.value ?: return

        viewModelScope.launch {
            // TODO: 실제 백엔드 BBox API 연결 시 아래 bounds 값을 요청 쿼리 파라미터로 전달
            // swLat = bounds.swLat
            // swLng = bounds.swLng
            // neLat = bounds.neLat
            // neLng = bounds.neLng

            // 현재는 더미 Repository 단계이므로 기존 getPins()를 다시 호출하는 방식으로 유지
            _pins.value = pinRepository.getPins()

            hideResearchAreaButton()
        }
    }
}