package com.issueissyu.fe.ui.screens.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collectLatest
import com.issueissyu.fe.R
import com.issueissyu.fe.domain.model.MapBounds
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.ui.components.CategoryButtons
import com.issueissyu.fe.ui.components.CategoryItem
import com.issueissyu.fe.ui.components.map.IssueissyuNaverMap
import com.issueissyu.fe.ui.components.map.toLatLng
import com.issueissyu.fe.ui.navigation.AppDestinations
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Shop
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.issueissyu.fe.ui.theme.White

// 위치 권한 요청 코드 상수
private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

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
    viewModel: MapViewModel = hiltViewModel()
) {
    val showResearchButton by viewModel.showResearchButton.collectAsStateWithLifecycle()
    val showPinTypeSelector by viewModel.showPinTypeSelector.collectAsStateWithLifecycle()
    val mapPins by viewModel.mapPins.collectAsStateWithLifecycle()
    val selectedPin by viewModel.selectedPin.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val notices by viewModel.notices.collectAsStateWithLifecycle()
    val isLocationSelectionMode by viewModel.isLocationSelectionMode.collectAsStateWithLifecycle()
    val selectedPinCategory by viewModel.selectedPinCategory.collectAsStateWithLifecycle()

    // TODO: ViewModel에서 combine(_mapPins, _selectedCategory)로 visibleMapPins StateFlow를 노출하고, UI는 collect만 하도록 정리

    val visibleMapPins = if (selectedCategory == null) {
        mapPins
    } else {
        mapPins.filter { it.category == selectedCategory }
    }

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
            moveToCurrentLocation()
        }
    }

    LaunchedEffect(naverMapInstance, visibleMapPins) {
        val naverMap = naverMapInstance ?: return@LaunchedEffect

        mapMarkers.forEach { it.map = null }
        mapMarkers.clear()

        visibleMapPins.forEach { mapPin ->
            val marker = Marker().apply {
                position = mapPin.coordinate.toLatLng()
                icon = OverlayImage.fromResource(mapPin.category.toMarkerIconRes())
                this.map = naverMap
                setOnClickListener {
                    viewModel.selectPinById(mapPin.pinId)
                    true
                }
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
                    "&userLng=${event.userCoordinate.longitude}"
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.messageEvents.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
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
                    viewModel.updateMapBounds(
                        MapBounds(
                            swLat = bounds.southWest.latitude,
                            swLng = bounds.southWest.longitude,
                            neLat = bounds.northEast.latitude,
                            neLng = bounds.northEast.longitude
                        )
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
                        title = notice.content
                    )
                },
                iconResId = R.drawable.ic_megaphone,
                onClick = { _ ->
                    // TODO: clickedNotice.id 기준으로 공지 상세 보기 또는 이동
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (showResearchButton) {
            Button(
                onClick = {
                    viewModel.fetchPinsInBounds()
                },
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
                    text = "이 지역 내 재탐색",
                    style = IssueTypo.Bold12.copy(color = White)
                )
            }
        }

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
                    .size(56.dp)
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
                    .size(48.dp)
                    .clickable {
                        moveToCurrentLocation()
                    },
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.height(8.dp))

            FloatingActionButton(
                onClick = { viewModel.openPinTypeSelector() },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                containerColor = BrandColor
            ) {
                Icon(
                    imageVector = Icons.Default.AddLocation,
                    contentDescription = "핀 생성",
                    tint = White
                )
            }
        }

        if (selectedPin != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { viewModel.clearSelectedPin() }
            )
        }

        selectedPin?.let { pin ->
            PinSummaryCard(
                pin = pin,
                currentUserId = "user1_id", // TODO: 로그인 연동 후 실제 currentUserId로 교체
                onDetailClick = { pinId ->
                    viewModel.clearSelectedPin()
                    navController.navigate(AppDestinations.pinDetailRoute(pinId))
                },
                onCommunityClick = { communityId ->
                    val numericCommunityId = communityId.toLongOrNull() ?: return@PinSummaryCard
                    viewModel.clearSelectedPin()
                    navController.navigate(AppDestinations.communityDetailRoute(numericCommunityId))
                },
                onEditClick = { _ ->
                },
                onDeleteClick = { pinId ->
                    viewModel.deletePin(pinId)
                },
                onSympathyClick = { pinId ->
                    viewModel.toggleSympathy(pinId)
                },
                onEmojiClick = { pinId ->
                    viewModel.openEmojiSelector(pinId)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            )
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
                    bottom = if (selectedPin != null) 288.dp else 24.dp
                )
        )
    }
}
