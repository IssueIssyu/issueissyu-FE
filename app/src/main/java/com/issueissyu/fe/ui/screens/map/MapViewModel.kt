package com.issueissyu.fe.ui.screens.map

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.local.TokenManager
import com.issueissyu.fe.domain.model.billing.BillingPurchaseEvent
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.MapPinCluster
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
import kotlinx.coroutines.Job
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

data class SelectedPinPage(
    val pinId: String,
    val pin: Pin? = null,
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

data class SavedMapCamera(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double,
)

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

    private val _mapClusters = MutableStateFlow<List<MapPinCluster>>(emptyList())
    val mapClusters: StateFlow<List<MapPinCluster>> = _mapClusters.asStateFlow()

    private val _selectedPin = MutableStateFlow<Pin?>(null)
    val selectedPin: StateFlow<Pin?> = _selectedPin.asStateFlow()

    private val _selectedPinPages = MutableStateFlow<List<SelectedPinPage>>(emptyList())
    val selectedPinPages: StateFlow<List<SelectedPinPage>> = _selectedPinPages.asStateFlow()

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
    private var currentZoomLevel: Int = DEFAULT_MAP_ZOOM_LEVEL
    private var autoRefreshJob: Job? = null
    private var refreshJob: Job? = null
    private var researchButtonReshowJob: Job? = null
    private var clusterPinLoadJob: Job? = null
    private var refreshRequestId: Long = 0
    private var lastFetchedBounds: MapBounds? = null
    private var savedCameraPosition: SavedMapCamera? = null

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

    private val _emojiPickerUiState = MutableStateFlow(MapEmojiPickerUiState())
    val emojiPickerUiState: StateFlow<MapEmojiPickerUiState> = _emojiPickerUiState.asStateFlow()
    private var observedBillingProductId = billingRepository.pendingBillingProductId.value

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
        clusterPinLoadJob?.cancel()
        _selectedPin.value = null
        _selectedPinPages.value = emptyList()
    }

    fun onCategorySelected(category: PinCategory?) {
        _selectedCategory.value = category
        if (category != null && _selectedPin.value?.category != category) {
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

    private fun updateResearchButtonVisibility() {
        val currentBounds = _currentBounds.value
        val fetchedBounds = lastFetchedBounds
        _showResearchButton.value = currentBounds != null &&
            fetchedBounds != null &&
            currentBounds != fetchedBounds
    }

    private fun scheduleResearchButtonReshow(fetchedBounds: MapBounds) {
        researchButtonReshowJob?.cancel()
        researchButtonReshowJob = viewModelScope.launch {
            delay(MANUAL_RESEARCH_BUTTON_RESHOW_MILLIS)
            if (_isMapRefreshing.value) return@launch
            val currentBounds = _currentBounds.value ?: return@launch
            if (currentBounds == fetchedBounds) {
                _showResearchButton.value = true
            }
        }
    }

    fun updateMapViewport(bounds: MapBounds, zoomLevel: Int) {
        val isInitialBounds = _currentBounds.value == null
        _currentBounds.value = bounds
        currentZoomLevel = zoomLevel
        if (isInitialBounds) {
            refreshMapImmediately()
        } else {
            updateResearchButtonVisibility()
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

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _isMapRefreshing.value = true
            try {
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
                    lastFetchedBounds = bounds
                    updateResearchButtonVisibility()
                    scheduleResearchButtonReshow(bounds)
                }.onFailure { error ->
                    if (requestId != refreshRequestId) return@onFailure
                    showResearchAreaButton()
                    _messageEvents.emit(
                        error.message?.takeIf { it.isNotBlank() }
                            ?: "지도 핀을 불러오지 못했습니다."
                    )
                }
            } finally {
                if (requestId == refreshRequestId) {
                    _isMapRefreshing.value = false
                }
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
                    updatePinInPages(updatedPin)
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
                    val pinPages = _selectedPinPages.value
                    val deletedPinIndex = pinPages.indexOfFirst { it.pinId == pinId }
                    val remainingPages = pinPages.filterNot { it.pinId == pinId }

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
                    _selectedPinPages.value = remainingPages

                    if (_selectedPin.value?.id == pinId) {
                        val nextIndex = deletedPinIndex.coerceAtMost(remainingPages.lastIndex)
                        if (nextIndex < 0) {
                            _selectedPin.value = null
                        } else {
                            viewModelScope.launch {
                                val nextPin = ensurePinLoadedAt(nextIndex)
                                _selectedPin.value = nextPin
                                nextPin?.let { loadPinEmojis(it.id) }
                            }
                        }
                    }
                }
                .onFailure { e ->
                    _messageEvents.emit(e.message?.takeIf { it.isNotBlank() } ?: "핀 삭제에 실패했습니다.")
                }
        }
    }

    fun selectPinById(pinId: String) {
        clusterPinLoadJob?.cancel()
        viewModelScope.launch {
            val pin = mapRepository.getPinCard(pinId).getOrNull() ?: return@launch
            _selectedPinPages.value = listOf(SelectedPinPage(pinId = pin.id, pin = pin))
            _selectedPin.value = pin
            loadPinEmojis(pinId)
        }
    }

    fun selectClusterPins(pinIds: List<String>) {
        val distinctPinIds = pinIds.distinct()
        if (distinctPinIds.isEmpty()) return

        clusterPinLoadJob?.cancel()
        clusterPinLoadJob = viewModelScope.launch {
            _selectedPinPages.value = distinctPinIds.map { pinId -> SelectedPinPage(pinId = pinId) }

            val firstPinId = distinctPinIds.first()
            val firstPin = mapRepository.getPinCard(firstPinId).getOrNull()
            if (firstPin == null) {
                _selectedPinPages.value = emptyList()
                _messageEvents.emit("클러스터의 핀 정보를 불러오지 못했습니다.")
                return@launch
            }

            _selectedPinPages.value = distinctPinIds.map { pinId ->
                SelectedPinPage(
                    pinId = pinId,
                    pin = firstPin.takeIf { it.id == pinId },
                )
            }
            _selectedPin.value = firstPin
            loadPinEmojis(firstPin.id)
        }
    }

    suspend fun selectPinPage(index: Int) {
        val pin = ensurePinLoadedAt(index) ?: return
        if (_selectedPin.value?.id == pin.id) return
        _selectedPin.value = pin
        loadPinEmojis(pin.id)
    }

    private suspend fun ensurePinLoadedAt(index: Int): Pin? {
        val pages = _selectedPinPages.value
        val page = pages.getOrNull(index) ?: return null
        page.pin?.let { return it }

        val loadedPin = mapRepository.getPinCard(page.pinId).getOrNull()
        if (loadedPin == null) {
            _messageEvents.emit("핀 정보를 불러오지 못했습니다.")
            return null
        }

        _selectedPinPages.value = pages.map { currentPage ->
            if (currentPage.pinId == loadedPin.id) {
                currentPage.copy(pin = loadedPin)
            } else {
                currentPage
            }
        }
        return loadedPin
    }

    private fun updatePinInPages(updatedPin: Pin) {
        _selectedPinPages.value = _selectedPinPages.value.map { page ->
            if (page.pinId == updatedPin.id) page.copy(pin = updatedPin) else page
        }
    }

    fun focusPinById(pinId: String) {
        clusterPinLoadJob?.cancel()
        viewModelScope.launch {
            val pin = mapRepository.getPinCard(pinId).getOrNull() ?: return@launch
            _selectedPinPages.value = listOf(SelectedPinPage(pinId = pin.id, pin = pin))
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
                val updatedPin = currentPin.copy(
                    emojiReactions = pinEmojis.toEmojiReactions()
                )
                _selectedPin.value = updatedPin
                updatePinInPages(updatedPin)
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
        if (candidate.canReact || billingRepository.pendingBillingProductId.value != null) return
        val productId = candidate.productId ?: run {
            _messageEvents.tryEmit("구매 정보를 찾을 수 없습니다.")
            return
        }
        if (activity == null) {
            _messageEvents.tryEmit("결제 화면을 열 수 없습니다.")
            return
        }
        observedBillingProductId = productId
        viewModelScope.launch {
            billingRepository.purchaseProduct(activity, productId)
                .onFailure { error ->
                    observedBillingProductId = null
                    _messageEvents.emit(error.message ?: "결제창을 열지 못했습니다.")
                }
        }
    }

    private fun observeBillingPurchaseEvents() {
        viewModelScope.launch {
            billingRepository.purchaseEvents.collect { event ->
                if (event.productId != observedBillingProductId) return@collect
                when (event) {
                    is BillingPurchaseEvent.Verified -> {
                        refreshEmojiCandidates()
                        _messageEvents.emit("이모지를 구매했습니다.")
                        finishBillingPurchase(event.productId)
                    }
                    is BillingPurchaseEvent.Pending -> {
                        _messageEvents.emit("결제가 대기 중입니다.")
                    }
                    is BillingPurchaseEvent.Canceled -> {
                        finishBillingPurchase(event.productId)
                    }
                    is BillingPurchaseEvent.Failed -> {
                        _messageEvents.emit(event.message)
                        finishBillingPurchase(event.productId)
                    }
                }
            }
        }
    }

    private fun finishBillingPurchase(productId: String) {
        observedBillingProductId = null
        billingRepository.acknowledgePurchaseResult(productId)
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
        _selectedPinPages.value = emptyList()
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

    fun saveCameraPosition(latitude: Double, longitude: Double, zoom: Double) {
        savedCameraPosition = SavedMapCamera(
            latitude = latitude,
            longitude = longitude,
            zoom = zoom,
        )
    }

    fun getSavedCameraPosition(): SavedMapCamera? = savedCameraPosition

    private companion object {
        const val DEFAULT_MAP_ZOOM_LEVEL = 11
        const val MAP_AUTO_REFRESH_DEBOUNCE_MILLIS = 400L
        const val MANUAL_RESEARCH_BUTTON_RESHOW_MILLIS = 15_000L
    }
}
