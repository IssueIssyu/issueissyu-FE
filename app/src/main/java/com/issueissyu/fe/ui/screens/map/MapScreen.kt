package com.issueissyu.fe.ui.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PointF
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import com.issueissyu.fe.R
import com.issueissyu.fe.core.extensions.findActivity
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.MapPinCluster
import com.issueissyu.fe.domain.model.MapPinMarker
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.ui.components.CategoryButtons
import com.issueissyu.fe.ui.components.CategoryItem
import com.issueissyu.fe.ui.components.EmojiReactionBottomSheet
import com.issueissyu.fe.ui.components.map.IssueissyuNaverMap
import com.issueissyu.fe.ui.components.map.toLatLng
import com.issueissyu.fe.ui.navigation.AppDestinations
import com.issueissyu.fe.ui.navigation.navigateToPinDetail
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Shop
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.issueissyu.fe.ui.theme.White
import kotlin.coroutines.resume
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

// 위치 권한 요청 코드 상수
private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
private const val INITIAL_USER_LOCATION_ZOOM = 16.0
private const val DEFAULT_MARKER_SCALE = 1.3f
private const val SELECTED_MARKER_SCALE = 1.7f
private const val SELECTED_MARKER_Z_INDEX = 3
private const val CLUSTER_MARKER_Z_INDEX = 2
private const val PIN_CREATION_AVAILABLE_RADIUS_METERS = 100.0
const val PIN_CREATE_MAP_REFRESH_KEY = "pin_create_map_refresh"
const val PIN_CREATE_FOCUS_PIN_ID_KEY = "pin_create_focus_pin_id"
const val MAP_FOCUS_USER_LOCATION_KEY = "map_focus_user_location"

private sealed interface MapInitialLocationState {
    data object Pending : MapInitialLocationState
    data object Loading : MapInitialLocationState
    data object Default : MapInitialLocationState
    data class Ready(val latLng: LatLng) : MapInitialLocationState
    data class Restored(val latLng: LatLng, val zoom: Double) : MapInitialLocationState
}

private suspend fun resolveInitialMapLocation(context: Context): LatLng? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    return suspendCancellableCoroutine { continuation ->
        val cancellationTokenSource = CancellationTokenSource()
        continuation.invokeOnCancellation { cancellationTokenSource.cancel() }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { lastKnown ->
                if (lastKnown != null) {
                    if (continuation.isActive) {
                        continuation.resume(LatLng(lastKnown.latitude, lastKnown.longitude))
                    }
                    return@addOnSuccessListener
                }

                fusedLocationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
                    .addOnSuccessListener { location ->
                        if (!continuation.isActive) return@addOnSuccessListener
                        continuation.resume(
                            location?.let { LatLng(it.latitude, it.longitude) },
                        )
                    }
            }
        } catch (_: SecurityException) {
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
    }
}

private suspend fun resolveAccurateMapLocation(context: Context): LatLng? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    return suspendCancellableCoroutine { continuation ->
        val cancellationTokenSource = CancellationTokenSource()
        continuation.invokeOnCancellation { cancellationTokenSource.cancel() }

        try {
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
                .addOnSuccessListener { location ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    continuation.resume(
                        location?.let { LatLng(it.latitude, it.longitude) },
                    )
                }
        } catch (_: SecurityException) {
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
    }
}

private fun NaverMap.moveToClusterBounds(
    context: Context,
    cluster: MapPinCluster,
) {
    val bounds = LatLngBounds.Builder()
        .include(cluster.pins.map { it.coordinate.toLatLng() })
        .build()
    val density = context.resources.displayMetrics.density
    val horizontalPadding = (48 * density).roundToInt()
    val topPadding = (72 * density).roundToInt()
    val bottomPadding = (320 * density).roundToInt()

    moveCamera(
        CameraUpdate
            .fitBounds(
                bounds,
                horizontalPadding,
                topPadding,
                horizontalPadding,
                bottomPadding,
            )
            .animate(CameraAnimation.Easing)
    )
}

private fun createSinglePinClusterMarker(
    context: Context,
    cluster: MapPinCluster,
    pin: MapPinMarker,
    isSelected: Boolean,
    naverMap: NaverMap,
    onClick: () -> Unit,
): Marker {
    val iconRes = pin.category.toMarkerIconRes()
    val markerStyle = PinMarkerBitmapCache.get(
        context = context,
        iconRes = iconRes,
        hasDiscount = pin.hasDiscount,
    )
    val categoryScale = pin.category.markerScaleMultiplier()
    val markerScale = (if (isSelected) SELECTED_MARKER_SCALE else DEFAULT_MARKER_SCALE) * categoryScale
    return Marker().apply {
        position = cluster.coordinate.toLatLng()
        icon = OverlayImage.fromBitmap(markerStyle.bitmap)
        anchor = PointF(0.5f, markerStyle.anchorY)
        width = (markerStyle.bitmap.width * markerScale).toInt()
        height = (markerStyle.bitmap.height * markerScale).toInt()
        if (isSelected) {
            zIndex = SELECTED_MARKER_Z_INDEX
        }
        map = naverMap
        setOnClickListener {
            width = (markerStyle.bitmap.width * SELECTED_MARKER_SCALE * categoryScale).toInt()
            height = (markerStyle.bitmap.height * SELECTED_MARKER_SCALE * categoryScale).toInt()
            zIndex = SELECTED_MARKER_Z_INDEX
            onClick()
            true
        }
    }
}

private fun createCountClusterMarker(
    context: Context,
    cluster: MapPinCluster,
    naverMap: NaverMap,
    onClick: () -> Unit,
): Marker {
    return Marker().apply {
        position = cluster.coordinate.toLatLng()
        icon = OverlayImage.fromBitmap(ClusterMarkerBitmapCache.get(context, cluster))
        anchor = PointF(0.5f, 0.5f)
        zIndex = CLUSTER_MARKER_Z_INDEX
        map = naverMap
        setOnClickListener {
            onClick()
            true
        }
    }
}

// ==============================================================================================
// 3. MapScreen Composable 함수
//    - 지도 화면 전체의 UI 구성을 담당하는 메인 Composable
//    - 지도, 상단 카테고리 버튼, 공지사항, 하단 버튼 (패치노트, 현재 위치, 핀 생성), 핀 카드 등의 UI 요소를 포함
//    - 지도 인스턴스, 핀 목록, 선택된 핀, 현재 지도 범위 등의 상태를 관리
// ==============================================================================================
@Composable
fun MapScreen(
    navController: NavHostController,
    focusPinId: String? = null,
    savedStateHandle: SavedStateHandle,
    onLocationSelectionModeChanged: (Boolean) -> Unit = {},
    viewModel: MapViewModel = hiltViewModel()
) {
    val showResearchButton by viewModel.showResearchButton.collectAsStateWithLifecycle()
    val isMapRefreshing by viewModel.isMapRefreshing.collectAsStateWithLifecycle()
    val showPinTypeSelector by viewModel.showPinTypeSelector.collectAsStateWithLifecycle()
    val visibleMapPins by viewModel.visibleMapPins.collectAsStateWithLifecycle()
    val visibleMapClusters by viewModel.visibleMapClusters.collectAsStateWithLifecycle()
    val selectedPin by viewModel.selectedPin.collectAsStateWithLifecycle()
    val selectedPinPages by viewModel.selectedPinPages.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val notices by viewModel.notices.collectAsStateWithLifecycle()
    val isLocationSelectionMode by viewModel.isLocationSelectionMode.collectAsStateWithLifecycle()
    val selectedPinCategory by viewModel.selectedPinCategory.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val isInCertifiedNeighborhood by viewModel.isInCertifiedNeighborhood.collectAsStateWithLifecycle()
    val emojiPickerUiState by viewModel.emojiPickerUiState.collectAsStateWithLifecycle()
    val currentUserId = viewModel.currentUserId
    val context = LocalContext.current
    val activity = context.findActivity()

    LaunchedEffect(isLocationSelectionMode) {
        onLocationSelectionModeChanged(isLocationSelectionMode)
    }

    DisposableEffect(Unit) {
        onDispose {
            onLocationSelectionModeChanged(false)
        }
    }

    if (emojiPickerUiState.isVisible) {
        EmojiReactionBottomSheet(
            candidates = emojiPickerUiState.candidates,
            selectedEmojiId = emojiPickerUiState.selectedEmojiId,
            isLoading = emojiPickerUiState.isLoading,
            isSubmitting = emojiPickerUiState.isSubmitting,
            errorMessage = emojiPickerUiState.errorMessage,
            onDismiss = {
                if (!emojiPickerUiState.isSubmitting) {
                    viewModel.closeEmojiPicker()
                }
            },
            onEmojiClick = viewModel::selectEmojiCandidate,
            onLockedEmojiClick = { emojiId -> viewModel.purchaseEmoji(activity, emojiId) },
            onApplyClick = viewModel::applySelectedEmoji,
            allowApplyWithoutSelection = true,
        )
    }
    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }
    var initialLocationState by remember { mutableStateOf<MapInitialLocationState>(MapInitialLocationState.Pending) }
    var locationCts by remember { mutableStateOf<CancellationTokenSource?>(null) }
    var hasFocusedRoutePin by rememberSaveable(focusPinId) { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val mapMarkers = remember { mutableStateListOf<Marker>() }
    val pinCreationRangeOverlay = remember {
        CircleOverlay().apply {
            zIndex = -1
        }
    }

    val locationSource = remember(activity) {
        activity?.let {
            FusedLocationSource(it, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locationCts?.cancel()
            pinCreationRangeOverlay.map = null
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun applyLocationToMap(
        map: NaverMap,
        latLng: LatLng,
        animate: Boolean,
    ) {
        val cameraUpdate = CameraUpdate
            .scrollAndZoomTo(latLng, INITIAL_USER_LOCATION_ZOOM)
            .let { update ->
                if (animate) {
                    update.animate(CameraAnimation.Easing)
                } else {
                    update
                }
            }
        map.moveCamera(cameraUpdate)
        map.locationOverlay.isVisible = true
        map.locationTrackingMode = LocationTrackingMode.Follow
        viewModel.updateCurrentLocation(latLng)
    }

    fun focusOnCurrentLocation(
        map: NaverMap,
        animate: Boolean,
    ) {
        locationCts?.cancel()
        val cts = CancellationTokenSource()
        locationCts = cts
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { lastKnown ->
                if (lastKnown != null) {
                    applyLocationToMap(
                        map = map,
                        latLng = LatLng(lastKnown.latitude, lastKnown.longitude),
                        animate = animate,
                    )
                }

                fusedLocationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            applyLocationToMap(
                                map = map,
                                latLng = LatLng(location.latitude, location.longitude),
                                animate = animate,
                            )
                        }
                    }
            }
        } catch (_: SecurityException) {
            map.locationTrackingMode = LocationTrackingMode.None
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            coroutineScope.launch {
                val latLng = resolveInitialMapLocation(context)
                if (latLng != null) {
                    initialLocationState = MapInitialLocationState.Ready(latLng)
                    naverMapInstance?.let { map ->
                        applyLocationToMap(map, latLng, animate = false)
                    }
                } else {
                    naverMapInstance?.let { map ->
                        focusOnCurrentLocation(map, animate = false)
                    }
                }
            }
        } else {
            naverMapInstance?.locationTrackingMode = LocationTrackingMode.None
        }
    }

    fun moveToCurrentLocation() {
        val map = naverMapInstance ?: return
        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
            return
        }
        focusOnCurrentLocation(
            map = map,
            animate = true,
        )
    }

    LaunchedEffect(focusPinId) {
        val focusUserLocation = savedStateHandle.get<Boolean>(MAP_FOCUS_USER_LOCATION_KEY) == true
        if (focusUserLocation) {
            savedStateHandle[MAP_FOCUS_USER_LOCATION_KEY] = false
        }

        val pendingPinFocus = savedStateHandle.get<String>(PIN_CREATE_FOCUS_PIN_ID_KEY).orEmpty().isNotBlank()

        if (focusPinId != null || pendingPinFocus) {
            initialLocationState = MapInitialLocationState.Default
            return@LaunchedEffect
        }

        val savedCamera = viewModel.getSavedCameraPosition()
        if (!focusUserLocation && savedCamera != null) {
            initialLocationState = MapInitialLocationState.Restored(
                latLng = LatLng(savedCamera.latitude, savedCamera.longitude),
                zoom = savedCamera.zoom,
            )
            return@LaunchedEffect
        }

        if (!hasLocationPermission()) {
            initialLocationState = MapInitialLocationState.Default
            return@LaunchedEffect
        }

        initialLocationState = MapInitialLocationState.Loading
        val latLng = resolveInitialMapLocation(context)
        initialLocationState = latLng?.let { MapInitialLocationState.Ready(it) }
            ?: MapInitialLocationState.Default
        latLng?.let(viewModel::updateCurrentLocation)
    }

    val focusUserLocationRequest by savedStateHandle
        .getStateFlow(MAP_FOCUS_USER_LOCATION_KEY, false)
        .collectAsStateWithLifecycle()

    LaunchedEffect(focusUserLocationRequest) {
        if (!focusUserLocationRequest || focusPinId != null) return@LaunchedEffect

        savedStateHandle[MAP_FOCUS_USER_LOCATION_KEY] = false

        if (!hasLocationPermission()) {
            initialLocationState = MapInitialLocationState.Default
            naverMapInstance?.let { map ->
                focusOnCurrentLocation(map, animate = false)
            }
            return@LaunchedEffect
        }

        initialLocationState = MapInitialLocationState.Loading
        val latLng = resolveInitialMapLocation(context)
        if (latLng != null) {
            initialLocationState = MapInitialLocationState.Ready(latLng)
            viewModel.updateCurrentLocation(latLng)
            naverMapInstance?.let { map ->
                applyLocationToMap(map, latLng, animate = false)
            }
        } else {
            initialLocationState = MapInitialLocationState.Default
            naverMapInstance?.let { map ->
                focusOnCurrentLocation(map, animate = false)
            }
        }
    }

    LaunchedEffect(focusPinId, naverMapInstance) {
        if (focusPinId != null && naverMapInstance != null && !hasFocusedRoutePin) {
            viewModel.focusPinById(focusPinId)
            hasFocusedRoutePin = true
        }
    }

    LaunchedEffect(naverMapInstance, initialLocationState) {
        val map = naverMapInstance ?: return@LaunchedEffect
        if (initialLocationState !is MapInitialLocationState.Restored) return@LaunchedEffect
        if (!hasLocationPermission()) return@LaunchedEffect

        map.locationTrackingMode = LocationTrackingMode.None
        viewModel.currentLocation.value?.let { coordinate ->
            map.locationOverlay.position = LatLng(coordinate.latitude, coordinate.longitude)
        }
        locationSource?.let { map.locationSource = it }
        map.locationOverlay.isVisible = true
        map.locationTrackingMode = LocationTrackingMode.None
    }

    LaunchedEffect(
        naverMapInstance,
        isLocationSelectionMode,
        currentLocation,
        isInCertifiedNeighborhood,
    ) {
        val map = naverMapInstance
        val location = currentLocation
        val shouldShowPinCreationRange =
            isLocationSelectionMode &&
                location != null &&
                isInCertifiedNeighborhood == false
        if (map == null || !shouldShowPinCreationRange) {
            pinCreationRangeOverlay.map = null
            return@LaunchedEffect
        }

        pinCreationRangeOverlay.center = LatLng(location.latitude, location.longitude)
        pinCreationRangeOverlay.radius = PIN_CREATION_AVAILABLE_RADIUS_METERS
        pinCreationRangeOverlay.color = android.graphics.Color.argb(48, 255, 72, 0)
        pinCreationRangeOverlay.outlineWidth = 0
        pinCreationRangeOverlay.map = map
    }

    LaunchedEffect(Unit) {
        viewModel.focusPin.collectLatest { pin ->
            val map = naverMapInstance ?: return@collectLatest
            val cameraUpdate = CameraUpdate
                .scrollAndZoomTo(pin.coordinate.toLatLng(), 16.0)
                .animate(CameraAnimation.Easing)
            map.moveCamera(cameraUpdate)
        }
    }

    LaunchedEffect(naverMapInstance, visibleMapPins, visibleMapClusters, selectedPin?.id) {
        val naverMap = naverMapInstance ?: return@LaunchedEffect

        mapMarkers.forEach { it.map = null }
        mapMarkers.clear()

        visibleMapPins.forEach { mapPin ->
            val iconRes = mapPin.category.toMarkerIconRes()
            val markerStyle = PinMarkerBitmapCache.get(
                context = context,
                iconRes = iconRes,
                hasDiscount = mapPin.hasDiscount,
            )
            val isSelected = mapPin.pinId == selectedPin?.id
            val categoryScale = mapPin.category.markerScaleMultiplier()
            val marker = Marker().apply {
                position = mapPin.coordinate.toLatLng()
                icon = OverlayImage.fromBitmap(markerStyle.bitmap)
                anchor = PointF(0.5f, markerStyle.anchorY)
                val markerScale = (if (isSelected) SELECTED_MARKER_SCALE else DEFAULT_MARKER_SCALE) * categoryScale
                width = (markerStyle.bitmap.width * markerScale).toInt()
                height = (markerStyle.bitmap.height * markerScale).toInt()
                if (isSelected) {
                    zIndex = SELECTED_MARKER_Z_INDEX
                }
                this.map = naverMap
                setOnClickListener {
                    width = (markerStyle.bitmap.width * SELECTED_MARKER_SCALE * categoryScale).toInt()
                    height = (markerStyle.bitmap.height * SELECTED_MARKER_SCALE * categoryScale).toInt()
                    zIndex = SELECTED_MARKER_Z_INDEX
                    viewModel.selectPinById(mapPin.pinId)
                    naverMap.moveCamera(
                        CameraUpdate
                            .scrollTo(mapPin.coordinate.toLatLng())
                            .animate(CameraAnimation.Easing)
                    )
                    true
                }
            }
            mapMarkers.add(marker)
        }

        visibleMapClusters.forEach { cluster ->
            val singlePin = cluster.pins.singleOrNull()
                ?.takeIf { cluster.pinCount == 1 }
            val marker = if (singlePin != null) {
                createSinglePinClusterMarker(
                    context = context,
                    cluster = cluster,
                    pin = singlePin,
                    isSelected = singlePin.pinId == selectedPin?.id,
                    naverMap = naverMap,
                    onClick = {
                        viewModel.selectPinById(singlePin.pinId)
                        naverMap.moveCamera(
                            CameraUpdate
                                .scrollTo(singlePin.coordinate.toLatLng())
                                .animate(CameraAnimation.Easing)
                        )
                    },
                )
            } else {
                createCountClusterMarker(
                    context = context,
                    cluster = cluster,
                    naverMap = naverMap,
                    onClick = {
                        viewModel.selectClusterPins(cluster.pins.map { it.pinId })
                        naverMap.moveToClusterBounds(context, cluster)
                    },
                )
            }
            mapMarkers.add(marker)
        }
    }

    // 핀 생성 화면으로 내비게이션 트리거
    LaunchedEffect(viewModel.navigateToPinCreation) {
        viewModel.navigateToPinCreation.collectLatest { event ->
            navController.navigate(
                AppDestinations.PIN_CREATION_ROUTE +
                    "?type=${event.category.name.lowercase()}" +
                    "&pinLat=${event.pinCoordinate.latitude}" +
                    "&pinLng=${event.pinCoordinate.longitude}" +
                    "&userLat=${event.userCoordinate.latitude}" +
                    "&userLng=${event.userCoordinate.longitude}" +
                    "&address=${Uri.encode(event.address)}"
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.messageEvents.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle.getStateFlow(PIN_CREATE_MAP_REFRESH_KEY, false).collectLatest { shouldRefresh ->
            if (!shouldRefresh) return@collectLatest
            viewModel.refreshMapImmediately()
            val createdPinId = savedStateHandle.get<String>(PIN_CREATE_FOCUS_PIN_ID_KEY).orEmpty()
            if (createdPinId.isNotBlank()) {
                viewModel.focusPinById(createdPinId)
            }
            savedStateHandle[PIN_CREATE_MAP_REFRESH_KEY] = false
            savedStateHandle[PIN_CREATE_FOCUS_PIN_ID_KEY] = ""
        }
    }

    val shouldWaitForInitialLocation = focusPinId == null &&
        when (initialLocationState) {
            MapInitialLocationState.Pending -> true
            MapInitialLocationState.Loading -> hasLocationPermission()
            else -> false
        }
    val initialCameraPosition = when (val state = initialLocationState) {
        is MapInitialLocationState.Ready -> CameraPosition(state.latLng, INITIAL_USER_LOCATION_ZOOM)
        is MapInitialLocationState.Restored -> CameraPosition(state.latLng, state.zoom)
        else -> null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!shouldWaitForInitialLocation) {
            IssueissyuNaverMap(
                modifier = Modifier.fillMaxSize(),
                initialCameraPosition = initialCameraPosition,
                onMapReady = { map ->
                    naverMapInstance = map

                    when {
                        focusPinId != null -> {
                            locationSource?.let { map.locationSource = it }
                            map.locationOverlay.isVisible = hasLocationPermission()
                            map.locationTrackingMode = if (hasLocationPermission()) {
                                LocationTrackingMode.Follow
                            } else {
                                LocationTrackingMode.None
                            }
                        }

                        initialLocationState is MapInitialLocationState.Ready -> {
                            locationSource?.let { map.locationSource = it }
                            map.locationOverlay.isVisible = true
                            map.locationTrackingMode = LocationTrackingMode.Follow
                            coroutineScope.launch {
                                val refinedLocation = resolveAccurateMapLocation(context)
                                refinedLocation?.let { latLng ->
                                    applyLocationToMap(map, latLng, animate = false)
                                }
                            }
                        }

                        initialLocationState is MapInitialLocationState.Restored -> {
                            map.locationTrackingMode = LocationTrackingMode.None
                            map.locationOverlay.isVisible = false
                        }

                        hasLocationPermission() -> {
                            locationSource?.let { map.locationSource = it }
                            map.locationOverlay.isVisible = true
                            map.locationTrackingMode = LocationTrackingMode.Follow
                            focusOnCurrentLocation(map, animate = false)
                        }

                        else -> {
                            map.locationOverlay.isVisible = false
                            map.locationTrackingMode = LocationTrackingMode.None
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                ),
                            )
                        }
                    }
                },
            onCameraIdle = { map ->
                viewModel.saveCameraPosition(
                    latitude = map.cameraPosition.target.latitude,
                    longitude = map.cameraPosition.target.longitude,
                    zoom = map.cameraPosition.zoom,
                )
                map.contentBounds.let { bounds ->
                    viewModel.updateMapViewport(
                        bounds = MapBounds(
                            swLat = bounds.southWest.latitude,
                            swLng = bounds.southWest.longitude,
                            neLat = bounds.northEast.latitude,
                            neLng = bounds.northEast.longitude
                        ),
                        zoomLevel = map.cameraPosition.zoom.toInt(),
                    )
                }
            },
            onMapClick = { clickedLatLng ->
                if (isLocationSelectionMode) {
                    val selectedCoordinate = PinCoordinate(
                        latitude = clickedLatLng.latitude,
                        longitude = clickedLatLng.longitude
                    )

                    val currentCoordinate = naverMapInstance
                        ?.locationOverlay
                        ?.position
                        ?.let { currentLatLng ->
                            PinCoordinate(
                                latitude = currentLatLng.latitude,
                                longitude = currentLatLng.longitude
                            )
                        }

                    viewModel.onMapCoordinateSelected(
                        selectedCoordinate = selectedCoordinate,
                        currentCoordinate = currentCoordinate
                    )
                } else {
                    viewModel.clearSelectedPin()
                }
            }
        )
        }

        if (shouldWaitForInitialLocation) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )
        }

        if (isLocationSelectionMode) {
            val guideText = when (selectedPinCategory) {
                PinCategory.ISSUE -> "이슈 핀을 생성할 위치를 선택해주세요"
                PinCategory.COMMUNICATION -> "소통 핀을 생성할 위치를 선택해주세요"
                else -> "핀을 생성할 위치를 선택해주세요"
            }
            val interactionSource = remember { MutableInteractionSource() }

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 124.dp)
                    .background(
                        color = Gray_7.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        // 배너 영역 클릭이 지도 클릭으로 처리되지 않게 소비
                    }
                    .padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = guideText,
                    style = IssueTypo.Bold12.copy(color = White)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { viewModel.exitLocationSelectionMode() },
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White,
                        contentColor = BrandColor
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "취소",
                        style = IssueTypo.Bold12.copy(color = BrandColor)
                    )
                }
            }
        }

        if (!isLocationSelectionMode) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
            ) {
                val errorContainer = MaterialTheme.colorScheme.errorContainer
                val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
                val primaryContainer = MaterialTheme.colorScheme.primaryContainer
                val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer

                val sampleCategories = remember(
                    errorContainer,
                    secondaryContainer,
                    primaryContainer,
                    tertiaryContainer
                ) {
                    listOf(
                        CategoryItem(PinCategory.ISSUE, "이슈", R.drawable.ic_issue, Issue, errorContainer),
                        CategoryItem(PinCategory.COMMUNICATION, "소통", R.drawable.communicate, Communication, secondaryContainer),
                        CategoryItem(PinCategory.SHOP, "가게", R.drawable.shop, Shop, primaryContainer),
                        CategoryItem(PinCategory.FESTIVAL, "축제", R.drawable.festival, Festival, tertiaryContainer)
                    )
                }

                CategoryButtons(
                    categories = sampleCategories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = viewModel::onCategorySelected,
                    onNotificationClick = {
                        navController.navigate(AppDestinations.NOTIFICATION_ROUTE)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                AutoScrollingNotice(
                    notices = notices.map { notice ->
                        NoticeUiModel(
                            id = notice.id,
                            title = notice.content,
                            pinId = notice.pinId
                        )
                    },
                    iconResId = R.drawable.ic_megaphone,
                    onClick = { clickedNotice ->
                        clickedNotice.pinId
                            ?.takeIf { it.isNotBlank() }
                            ?.let { pinId -> navController.navigateToPinDetail(pinId) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        if (showResearchButton && !isLocationSelectionMode) {
            Button(
                onClick = {
                    viewModel.refreshMapImmediately()
                },
                enabled = !isMapRefreshing,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 192.dp)
                    .size(width = 153.dp, height = 31.dp),
                shape = RoundedCornerShape(15.5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gray_7,
                    contentColor = White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "재탐색",
                    tint = White,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = if (isMapRefreshing) "불러오는 중" else "이 지역 내 재탐색",
                    style = IssueTypo.Bold12.copy(color = White)
                )
            }
        }

        if (!isLocationSelectionMode) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PatchNoteMapButton(
                    onClick = {
                        navController.navigate(AppDestinations.PATCH_NOTE_ROUTE)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Icon(
                    painter = painterResource(id = R.drawable.findspot),
                    contentDescription = "내 위치 찾기",
                    modifier = Modifier
                        .size(60.dp)
                        .clickable {
                            moveToCurrentLocation()
                        },
                    tint = Color.Unspecified
                )

                Spacer(modifier = Modifier.height(8.dp))

                Icon(
                    painter = painterResource(id = R.drawable.pinbutton),
                    contentDescription = "핀 생성",
                    modifier = Modifier
                        .size(width = 70.dp, height = 81.dp)
                        .clickable { viewModel.openPinTypeSelector() },
                    tint = Color.Unspecified
                )
            }
        }

        if (selectedPinPages.isNotEmpty() && !isLocationSelectionMode) {
            val pagerState = rememberPagerState(pageCount = { selectedPinPages.size })
            val latestSelectedPinPages by rememberUpdatedState(selectedPinPages)
            val latestNaverMap by rememberUpdatedState(naverMapInstance)

            LaunchedEffect(selectedPin?.id, selectedPinPages.map { it.pinId }) {
                val selectedPage = selectedPinPages.indexOfFirst { it.pinId == selectedPin?.id }
                if (selectedPage < 0 || pagerState.settledPage == selectedPage) {
                    return@LaunchedEffect
                }

                pagerState.scrollToPage(selectedPage)
            }

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.settledPage }
                    .distinctUntilChanged()
                    .collectLatest { page ->
                        viewModel.selectPinPage(page)
                        val pin = viewModel.selectedPin.value
                            ?: latestSelectedPinPages.getOrNull(page)?.pin
                            ?: return@collectLatest
                        latestNaverMap?.moveCamera(
                            CameraUpdate
                                .scrollTo(pin.coordinate.toLatLng())
                                .animate(CameraAnimation.Easing)
                        )
                    }
            }

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 12.dp),
                pageSpacing = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                key = { page -> selectedPinPages[page].pinId },
            ) { page ->
                val pinPage = selectedPinPages[page]
                val pin = pinPage.pin
                if (pin != null) {
                    PinSummaryCard(
                        pin = pin,
                        currentUserId = currentUserId,
                        onDetailClick = { pinId ->
                            viewModel.clearSelectedPin()
                            navController.navigateToPinDetail(pinId)
                        },
                        onCommunityClick = { communityId ->
                            val numericCommunityId = communityId.toLongOrNull()
                                ?: return@PinSummaryCard
                            viewModel.clearSelectedPin()
                            navController.navigate(
                                AppDestinations.communityDetailRoute(numericCommunityId)
                            )
                        },
                        onEditClick = { pinId ->
                            viewModel.clearSelectedPin()
                            navController.navigateToPinDetail(pinId, startHomeEdit = true)
                        },
                        onDeleteClick = viewModel::deletePin,
                        onSympathyClick = viewModel::toggleSympathy,
                        onEmojiClick = viewModel::openEmojiPicker,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        if (showPinTypeSelector && !isLocationSelectionMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { viewModel.closePinTypeSelector() }
            )
        }

        if (showPinTypeSelector && !isLocationSelectionMode) {
            PinTypeSelector(
                onIssueClick = {
                    viewModel.enterLocationSelectionMode(PinCategory.ISSUE)
                },
                onCommunicationClick = {
                    viewModel.enterLocationSelectionMode(PinCategory.COMMUNICATION)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 95.dp)
            )
        }
    }
}
