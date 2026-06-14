package com.issueissyu.fe.ui.screens.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import com.issueissyu.fe.R
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.MapPinCluster
import com.issueissyu.fe.domain.model.MapPinMarker
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.ui.components.CategoryButtons
import com.issueissyu.fe.ui.components.CategoryItem
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
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.issueissyu.fe.ui.theme.White
import kotlin.math.roundToInt

// 위치 권한 요청 코드 상수
private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
private const val DEFAULT_MARKER_SCALE = 1.5f
private const val SELECTED_MARKER_SCALE = 2f
private const val SELECTED_MARKER_Z_INDEX = 1
private const val CLUSTER_MARKER_Z_INDEX = 2
const val PIN_CREATE_MAP_REFRESH_KEY = "pin_create_map_refresh"
const val PIN_CREATE_FOCUS_PIN_ID_KEY = "pin_create_focus_pin_id"

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
    naverMap: NaverMap,
    onClick: () -> Unit,
): Marker {
    val iconRes = pin.category.toMarkerIconRes()
    return Marker().apply {
        position = cluster.coordinate.toLatLng()
        icon = OverlayImage.fromResource(iconRes)
        ContextCompat.getDrawable(context, iconRes)?.let { drawable ->
            width = (drawable.intrinsicWidth * DEFAULT_MARKER_SCALE).toInt()
            height = (drawable.intrinsicHeight * DEFAULT_MARKER_SCALE).toInt()
        }
        map = naverMap
        setOnClickListener {
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
        icon = OverlayImage.fromBitmap(createClusterBitmap(context, cluster))
        anchor = PointF(0.5f, 0.5f)
        zIndex = CLUSTER_MARKER_Z_INDEX
        map = naverMap
        setOnClickListener {
            onClick()
            true
        }
    }
}

private fun createClusterBitmap(context: Context, cluster: MapPinCluster): Bitmap {
    val density = context.resources.displayMetrics.density
    val markerSize = (52 * density).toInt()
    val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = markerSize / 2f

    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(72, 119, 255)
        style = Paint.Style.FILL
        setShadowLayer(4 * density, 0f, 2 * density, android.graphics.Color.argb(70, 0, 0, 0))
    }
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4 * density
        strokeCap = Paint.Cap.BUTT
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 17 * density
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    val radius = center - 5 * density
    canvas.drawCircle(center, center, radius, circlePaint)
    val categoryCounts = cluster.pins
        .groupingBy { it.category }
        .eachCount()
        .toSortedMap(compareBy(PinCategory::ordinal))
    val totalCategoryPins = categoryCounts.values.sum()
    val borderBounds = RectF(
        center - radius,
        center - radius,
        center + radius,
        center + radius,
    )
    if (totalCategoryPins == 0) {
        borderPaint.color = android.graphics.Color.WHITE
        canvas.drawCircle(center, center, radius, borderPaint)
    } else {
        var startAngle = -90f
        categoryCounts.forEach { (category, count) ->
            val sweepAngle = 360f * count / totalCategoryPins
            borderPaint.color = category.toClusterBorderColor()
            canvas.drawArc(borderBounds, startAngle, sweepAngle, false, borderPaint)
            startAngle += sweepAngle
        }
    }
    val textY = center - (textPaint.ascent() + textPaint.descent()) / 2f
    canvas.drawText(cluster.pinCount.toString(), center, textY, textPaint)
    return bitmap
}

private fun PinCategory.toClusterBorderColor(): Int {
    return when (this) {
        PinCategory.ISSUE -> Issue.toArgb()
        PinCategory.COMMUNICATION -> Communication.toArgb()
        PinCategory.SHOP -> Shop.toArgb()
        PinCategory.FESTIVAL -> Festival.toArgb()
    }
}

// Context에서 Activity를 찾는 헬퍼 함수
private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
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
    val mapPins by viewModel.mapPins.collectAsStateWithLifecycle()
    val mapClusters by viewModel.mapClusters.collectAsStateWithLifecycle()
    val selectedPin by viewModel.selectedPin.collectAsStateWithLifecycle()
    val selectedPins by viewModel.selectedPins.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val notices by viewModel.notices.collectAsStateWithLifecycle()
    val isLocationSelectionMode by viewModel.isLocationSelectionMode.collectAsStateWithLifecycle()
    val selectedPinCategory by viewModel.selectedPinCategory.collectAsStateWithLifecycle()
    val currentUserId = viewModel.currentUserId

    LaunchedEffect(isLocationSelectionMode) {
        onLocationSelectionModeChanged(isLocationSelectionMode)
    }

    DisposableEffect(Unit) {
        onDispose {
            onLocationSelectionModeChanged(false)
        }
    }

    // TODO: ViewModel에서 combine(_mapPins, _selectedCategory)로 visibleMapPins StateFlow를 노출하고, UI는 collect만 하도록 정리

    val visibleMapPins = when {
        isLocationSelectionMode -> emptyList()
        selectedCategory == null -> mapPins
        else -> mapPins.filter { it.category == selectedCategory }
    }
    val visibleMapClusters = if (isLocationSelectionMode) emptyList() else mapClusters

    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }

    val mapMarkers = remember { mutableStateListOf<Marker>() }

    val context = LocalContext.current
    val activity = context.findActivity()
    val snackbarHostState = remember { SnackbarHostState() }

    val locationSource = remember(activity) {
        activity?.let {
            FusedLocationSource(it, LOCATION_PERMISSION_REQUEST_CODE)
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

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            naverMapInstance?.locationTrackingMode = LocationTrackingMode.Follow
            naverMapInstance?.locationOverlay?.isVisible = true
        } else {
            naverMapInstance?.locationTrackingMode = LocationTrackingMode.None
        }
    }

    fun moveToCurrentLocation() {
        val map = naverMapInstance ?: return

        if (hasLocationPermission()) {
            map.locationTrackingMode = LocationTrackingMode.Follow
            map.locationOverlay.isVisible = true
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(naverMapInstance) {
        if (naverMapInstance != null) {
            if (focusPinId == null) {
                moveToCurrentLocation()
            }
        }
    }

    LaunchedEffect(focusPinId, naverMapInstance) {
        if (focusPinId != null && naverMapInstance != null) {
            viewModel.focusPinById(focusPinId)
        }
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
            val isSelected = mapPin.pinId == selectedPin?.id
            val marker = Marker().apply {
                position = mapPin.coordinate.toLatLng()
                icon = OverlayImage.fromResource(iconRes)
                val markerScale = if (isSelected) SELECTED_MARKER_SCALE else DEFAULT_MARKER_SCALE
                ContextCompat.getDrawable(context, iconRes)?.let { drawable ->
                    width = (drawable.intrinsicWidth * markerScale).toInt()
                    height = (drawable.intrinsicHeight * markerScale).toInt()
                }
                if (isSelected) {
                    zIndex = SELECTED_MARKER_Z_INDEX
                }
                this.map = naverMap
                setOnClickListener {
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
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle.getStateFlow(PIN_CREATE_MAP_REFRESH_KEY, false).collectLatest { shouldRefresh ->
            if (!shouldRefresh) return@collectLatest
            viewModel.refreshMapImmediately()
            val createdPinId = savedStateHandle.get<String>(PIN_CREATE_FOCUS_PIN_ID_KEY).orEmpty()
            if (createdPinId.isNotBlank()) {
                viewModel.selectPinById(createdPinId)
            }
            savedStateHandle[PIN_CREATE_MAP_REFRESH_KEY] = false
            savedStateHandle[PIN_CREATE_FOCUS_PIN_ID_KEY] = ""
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        IssueissyuNaverMap(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                naverMapInstance = map

                locationSource?.let {
                    map.locationSource = it
                }

                if (hasLocationPermission()) {
                    map.locationOverlay.isVisible = true
                    map.locationTrackingMode = LocationTrackingMode.Follow
                } else {
                    map.locationOverlay.isVisible = false
                    map.locationTrackingMode = LocationTrackingMode.None
                }
            },
            onCameraIdle = { map ->
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

                    val currentLatLng = naverMapInstance?.locationOverlay?.position
                    if (currentLatLng == null) {
                        // TODO: 현재 위치를 가져오지 못한 경우 안내 UI 표시
                        return@IssueissyuNaverMap
                    }

                    val currentCoordinate = PinCoordinate(
                        latitude = currentLatLng.latitude,
                        longitude = currentLatLng.longitude
                    )

                    viewModel.onMapCoordinateSelected(
                        selectedCoordinate = selectedCoordinate,
                        currentCoordinate = currentCoordinate
                    )
                } else {
                    viewModel.clearSelectedPin()
                }
            }
        )

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
                        CategoryItem("이슈", R.drawable.issue, Issue, errorContainer),
                        CategoryItem("소통", R.drawable.communicate, Communication, secondaryContainer),
                        CategoryItem("가게", R.drawable.shop, Shop, primaryContainer),
                        CategoryItem("축제", R.drawable.festival, Festival, tertiaryContainer)
                    )
                }

                val selectedCategoryLabel = when (selectedCategory) {
                    PinCategory.ISSUE -> "이슈"
                    PinCategory.COMMUNICATION -> "소통"
                    PinCategory.SHOP -> "가게"
                    PinCategory.FESTIVAL -> "축제"
                    null -> null
                }

                CategoryButtons(
                    categories = sampleCategories,
                    selectedCategory = selectedCategoryLabel,
                    onCategorySelected = { category ->
                        viewModel.onCategorySelected(category)
                    },
                    onNotificationClick = {
                        // TODO: 알림 목록 UI 또는 알림 화면 연결
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
                Icon(
                    painter = painterResource(id = R.drawable.patchnotebutton),
                    contentDescription = "패치노트",
                    modifier = Modifier
                        .size(70.dp)
                        .clickable {
                            navController.navigate(AppDestinations.PATCH_NOTE_ROUTE)
                        },
                    tint = Color.Unspecified
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

        if (selectedPins.isNotEmpty() && !isLocationSelectionMode) {
            val pagerState = rememberPagerState(pageCount = { selectedPins.size })

            LaunchedEffect(pagerState, selectedPins.map { it.id }) {
                snapshotFlow { pagerState.currentPage }
                    .distinctUntilChanged()
                    .drop(1)
                    .collectLatest { page ->
                        val pin = selectedPins.getOrNull(page) ?: return@collectLatest
                        viewModel.selectPinPage(page)
                        naverMapInstance?.moveCamera(
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
                key = { page -> selectedPins[page].id },
            ) { page ->
                val pin = selectedPins[page]
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
                    onEditClick = {},
                    onDeleteClick = viewModel::deletePin,
                    onSympathyClick = viewModel::toggleSympathy,
                    onEmojiClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
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
                    .padding(end = 4.dp, bottom = 88.dp)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = if (selectedPins.isNotEmpty()) 288.dp else 24.dp
                )
        )
    }
}
