package com.issueissyu.fe.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.MapPinCluster
import com.issueissyu.fe.domain.model.MapNotice
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.MapPinMarker
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.domain.model.pin.PinEmojiReaction
import com.issueissyu.fe.domain.model.pin.PinEmojis
import com.issueissyu.fe.domain.repository.LocationRepository
import com.issueissyu.fe.domain.repository.MapRepository
import com.issueissyu.fe.domain.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.naver.maps.geometry.LatLng
import javax.inject.Inject

data class PinCreationNavigationEvent(
    val category: PinCategory,
    val pinCoordinate: PinCoordinate,
    val userCoordinate: PinCoordinate,
    val address: String,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapRepository: MapRepository,
    private val pinRepository: PinRepository,
    private val locationRepository: LocationRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    val currentUserId: String
        get() = tokenManager.getCurrentUserUuid().orEmpty()

    private val _mapPins = MutableStateFlow<List<MapPinMarker>>(emptyList())
    val mapPins: StateFlow<List<MapPinMarker>> = _mapPins.asStateFlow()

    private val _mapClusters = MutableStateFlow<List<MapPinCluster>>(emptyList())
    val mapClusters: StateFlow<List<MapPinCluster>> = _mapClusters.asStateFlow()

    private val _selectedPin = MutableStateFlow<Pin?>(null)
    val selectedPin: StateFlow<Pin?> = _selectedPin.asStateFlow()

    private val _selectedPins = MutableStateFlow<List<Pin>>(emptyList())
    val selectedPins: StateFlow<List<Pin>> = _selectedPins.asStateFlow()

    private val _focusPin = MutableSharedFlow<Pin>(extraBufferCapacity = 1)
    val focusPin = _focusPin.asSharedFlow()

    private val _selectedCategory = MutableStateFlow<PinCategory?>(null)
    val selectedCategory: StateFlow<PinCategory?> = _selectedCategory.asStateFlow()

    private val _notices = MutableStateFlow<List<MapNotice>>(emptyList())
    val notices: StateFlow<List<MapNotice>> = _notices.asStateFlow()

    private val _showResearchButton = MutableStateFlow(false)
    val showResearchButton: StateFlow<Boolean> = _showResearchButton.asStateFlow()

    private val _isMapRefreshing = MutableStateFlow(false)
    val isMapRefreshing: StateFlow<Boolean> = _isMapRefreshing.asStateFlow()

    private val _showPinTypeSelector = MutableStateFlow(false)
    val showPinTypeSelector: StateFlow<Boolean> = _showPinTypeSelector.asStateFlow()

    private val _currentBounds = MutableStateFlow<MapBounds?>(null)
    @Suppress("unused") // TODO: UI에서 bounds를 관찰할 계획이면 유지하되 @Suppress("unused")를 붙여도 됩니다.
    val currentBounds: StateFlow<MapBounds?> = _currentBounds.asStateFlow()
    private var currentZoomLevel: Int = DEFAULT_MAP_ZOOM_LEVEL
    private var autoRefreshJob: Job? = null
    private var refreshRequestId: Long = 0

    private val _isLocationSelectionMode = MutableStateFlow(false)
    val isLocationSelectionMode: StateFlow<Boolean> = _isLocationSelectionMode.asStateFlow()

    val visibleMapPins: StateFlow<List<MapPinMarker>> =
        combine(_mapPins, _selectedCategory, _isLocationSelectionMode) { pins, category, isSelectingLocation ->
            when {
                isSelectingLocation -> emptyList()
                category == null -> pins
                else -> pins.filter { it.category == category }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val visibleMapClusters: StateFlow<List<MapPinCluster>> =
        combine(_mapClusters, _selectedCategory, _isLocationSelectionMode) { clusters, category, isSelectingLocation ->
            when {
                isSelectingLocation -> emptyList()
                category == null -> clusters
                else -> clusters.mapNotNull { cluster ->
                    val categoryPins = cluster.pins.filter { it.category == category }
                    if (categoryPins.isEmpty()) {
                        null
                    } else {
                        cluster.copy(
                            pinCount = categoryPins.size,
                            pins = categoryPins,
                        )
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

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
        _selectedPins.value = emptyList()
    }

    fun onCategorySelected(categoryName: String?) {
        // TODO: 추후 CategoryButtons에서 PinCategory를 직접 전달하도록 변경
        val newCategory = when (categoryName) {
            "이슈" -> PinCategory.ISSUE
            "소통" -> PinCategory.COMMUNICATION
            "가게" -> PinCategory.SHOP
            "축제" -> PinCategory.FESTIVAL
            else -> null
        }
        _selectedCategory.value = newCategory
        if (newCategory != null && _selectedPin.value?.category != newCategory) {
            clearSelectedPin()
        }
        refreshMapImmediately()
    }

    fun openPinTypeSelector() {
        _showPinTypeSelector.value = true
    }

    fun closePinTypeSelector() {
        _showPinTypeSelector.value = false
    }

    private fun showResearchAreaButton() {
        _showResearchButton.value = true
    }

    private fun hideResearchAreaButton() {
        _showResearchButton.value = false
    }

    fun updateMapViewport(bounds: MapBounds, zoomLevel: Int) {
        val isInitialBounds = _currentBounds.value == null
        _currentBounds.value = bounds
        currentZoomLevel = zoomLevel
        if (isInitialBounds) {
            refreshMapImmediately()
        } else {
            showResearchAreaButton()
            scheduleAutomaticRefresh()
        }
    }

    fun refreshMapImmediately() {
        autoRefreshJob?.cancel()
        refreshMap()
    }

    private fun scheduleAutomaticRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            delay(MAP_AUTO_REFRESH_DEBOUNCE_MILLIS)
            refreshMap()
        }
    }

    private fun refreshMap() {
        val bounds = _currentBounds.value ?: return
        val zoomLevel = currentZoomLevel
        val category = _selectedCategory.value
        val selectedPinIdAtRequest = _selectedPin.value?.id
        val requestId = ++refreshRequestId

        viewModelScope.launch {
            _isMapRefreshing.value = true
            mapRepository.getMapPinsInBounds(
                bounds = bounds,
                zoomLevel = zoomLevel,
                category = category,
            ).onSuccess { result ->
                if (requestId != refreshRequestId) return@onSuccess
                _mapPins.value = result.pins
                _mapClusters.value = result.clusters
                clearSelectionIfOutsideResult(
                    selectedPinIdAtRequest = selectedPinIdAtRequest,
                    resultPinIds = buildSet {
                        result.pins.forEach { add(it.pinId) }
                        result.clusters.forEach { cluster ->
                            cluster.pins.forEach { add(it.pinId) }
                        }
                    },
                )
                hideResearchAreaButton()
            }.onFailure { error ->
                if (requestId != refreshRequestId) return@onFailure
                showResearchAreaButton()
                _messageEvents.emit(
                    error.message?.takeIf { it.isNotBlank() }
                        ?: "지도 핀을 불러오지 못했습니다."
                )
            }

            if (requestId == refreshRequestId) {
                _isMapRefreshing.value = false
            }
        }
    }

    private fun clearSelectionIfOutsideResult(
        selectedPinIdAtRequest: String?,
        resultPinIds: Set<String>,
    ) {
        if (selectedPinIdAtRequest == null) return
        if (_selectedPin.value?.id != selectedPinIdAtRequest) return
        if (selectedPinIdAtRequest in resultPinIds) return
        clearSelectedPin()
    }

    fun toggleSympathy(pinId: String) {
        val pin = _selectedPin.value?.takeIf { it.id == pinId } ?: return
        if (pin.isSympathizedByMe) return
        val numericPinId = pinId.toLongOrNull() ?: return

        viewModelScope.launch {
            pinRepository.likePin(numericPinId)
                .onSuccess { like ->
                    val currentPin = _selectedPin.value?.takeIf { it.id == pinId } ?: return@onSuccess
                    val updatedPin = currentPin.copy(
                        isSympathizedByMe = like.isLike,
                        sympathyCount = like.pinLikeCount,
                    )
                    _selectedPin.value = updatedPin
                    _selectedPins.value = _selectedPins.value.map { pin ->
                        if (pin.id == pinId) updatedPin else pin
                    }
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
                    val selectedPins = _selectedPins.value
                    val deletedPinIndex = selectedPins.indexOfFirst { it.id == pinId }
                    val remainingPins = selectedPins.filterNot { it.id == pinId }

                    _mapPins.value = _mapPins.value.filterNot { it.pinId == pinId }
                    _mapClusters.value = _mapClusters.value.mapNotNull { cluster ->
                        val remainingClusterPins = cluster.pins.filterNot { it.pinId == pinId }
                        if (remainingClusterPins.isEmpty()) {
                            null
                        } else {
                            cluster.copy(
                                pinCount = remainingClusterPins.size,
                                pins = remainingClusterPins,
                            )
                        }
                    }
                    _selectedPins.value = remainingPins

                    if (_selectedPin.value?.id == pinId) {
                        val nextIndex = deletedPinIndex.coerceAtMost(remainingPins.lastIndex)
                        _selectedPin.value = remainingPins.getOrNull(nextIndex)
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
            _selectedPins.value = listOf(pin)
            loadPinEmojis(pinId)
        }
    }

    fun selectClusterPins(pinIds: List<String>) {
        val distinctPinIds = pinIds.distinct()
        if (distinctPinIds.isEmpty()) return

        viewModelScope.launch {
            val pins = coroutineScope {
                distinctPinIds.map { pinId ->
                    async { mapRepository.getPinCard(pinId).getOrNull() }
                }.awaitAll().filterNotNull()
            }
            if (pins.isEmpty()) {
                _messageEvents.emit("클러스터의 핀 정보를 불러오지 못했습니다.")
                return@launch
            }

            _selectedPins.value = pins
            _selectedPin.value = pins.first()
            loadPinEmojis(pins.first().id)
        }
    }

    fun selectPinPage(index: Int) {
        val pin = _selectedPins.value.getOrNull(index) ?: return
        if (_selectedPin.value?.id == pin.id) return
        _selectedPin.value = pin
        viewModelScope.launch {
            loadPinEmojis(pin.id)
        }
    }

    fun focusPinById(pinId: String) {
        viewModelScope.launch {
            val pin = mapRepository.getPinCard(pinId).getOrNull() ?: return@launch
            _selectedPin.value = pin
            _selectedPins.value = listOf(pin)
            _focusPin.emit(pin)
            loadPinEmojis(pinId)
        }
    }

    private suspend fun loadPinEmojis(pinId: String) {
        val numericPinId = pinId.toLongOrNull() ?: return
        pinRepository.getPinEmojis(numericPinId)
            .onSuccess { pinEmojis ->
                val currentPin = _selectedPin.value?.takeIf { it.id == pinId } ?: return@onSuccess
                val updatedPin = currentPin.copy(
                    emojiReactions = pinEmojis.toEmojiReactions()
                )
                _selectedPin.value = updatedPin
                _selectedPins.value = _selectedPins.value.map { pin ->
                    if (pin.id == pinId) updatedPin else pin
                }
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
        _selectedPin.value = null
        _selectedPins.value = emptyList()
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

    private companion object {
        const val DEFAULT_MAP_ZOOM_LEVEL = 11
        const val MAP_AUTO_REFRESH_DEBOUNCE_MILLIS = 400L
    }
}
