package com.issueissyu.fe.ui.screens.map

// ==============================================================================================
// 1. Android 권한 및 Jetpack Compose 관련 Import
//    - 위치 권한 처리를 위한 Android Manifest, Activity, Context, PackageManager 관련 클래스 임포트
//    - Jetpack Compose UI 및 상태 관리를 위한 다양한 컴포넌트 임포트
//    - 네이버 지도 SDK의 LocationTrackingMode, FusedLocationSource 등 지도 관련 클래스 임포트
// ==============================================================================================

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.util.FusedLocationSource
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.ui.screens.map.MapViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.components.CategoryButtons
import com.issueissyu.fe.ui.components.CategoryItem
import com.issueissyu.fe.ui.navigation.AppDestinations
import com.issueissyu.fe.ui.screens.map.AutoScrollingNotice
import com.issueissyu.fe.ui.screens.map.PinSummaryCard
import com.issueissyu.fe.ui.screens.map.PinTypeSelector
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.CommunicationContainerLight
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.FestivalContainer
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueContainer
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Shop
import com.issueissyu.fe.ui.theme.ShopContainer
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
import com.naver.maps.map.NaverMap

import com.issueissyu.fe.data.model.Pin
import com.issueissyu.fe.data.model.PinCategory
import com.issueissyu.fe.data.model.PinCoordinate
import androidx.compose.runtime.mutableStateListOf
import com.naver.maps.map.overlay.Marker

import com.issueissyu.fe.ui.components.map.IssueissyuNaverMap
import com.issueissyu.fe.ui.components.map.toLatLng
import com.issueissyu.fe.data.model.MapBounds
import com.naver.maps.map.overlay.OverlayImage
import com.issueissyu.fe.ui.screens.map.toMarkerIconRes


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
// 2. Enum 및 Data Class 정의
//    - PinCategory: 지도에 표시될 핀의 카테고리 (이슈, 소통, 가게, 축제)를 정의
//    - MapPin: 지도 핀의 데이터 구조 (ID, 위치, 제목, 카테고리, 설명 등)를 정의
// ==============================================================================================

// 제거된 부분: private enum class ResolutionStatus, private enum class PinCategory, private data class MapPin

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
    val pins by viewModel.pins.collectAsStateWithLifecycle()
    val selectedPin by viewModel.selectedPin.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val visiblePins = if (selectedCategory == null) {
        pins
    } else {
        pins.filter { it.category == selectedCategory }
    }

    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }

    val mapMarkers = remember { mutableStateListOf<Marker>() }

    val context = LocalContext.current
    val activity = context.findActivity()

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

    var hasRequestedInitialLocation by remember { mutableStateOf(false) }

    LaunchedEffect(naverMapInstance) {
        if (naverMapInstance != null && !hasRequestedInitialLocation) {
            hasRequestedInitialLocation = true
            moveToCurrentLocation()
        }
    }

    LaunchedEffect(naverMapInstance, visiblePins) {
        val naverMap = naverMapInstance ?: return@LaunchedEffect

        mapMarkers.forEach { it.map = null }
        mapMarkers.clear()

        visiblePins.forEach { pin ->
            val marker = Marker().apply {
                position = pin.coordinate.toLatLng()
                icon = OverlayImage.fromResource(pin.category.toMarkerIconRes())
                this.map = naverMap
                setOnClickListener {
                    viewModel.selectPin(pin)
                    true
                }
            }
            mapMarkers.add(marker)
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
                map.contentBounds?.let { bounds ->
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
            onMapClick = { _ ->
                viewModel.clearSelectedPin()
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
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
                notices = remember { listOf("오늘의 공지: 새로운 업데이트가 있습니다!", "두 번째 공지: 버그 수정 및 성능 개선", "세 번째 공지: 새로운 이벤트가 시작됩니다!") },
                iconResId = R.drawable.ic_megaphone,
                onClick = { clickedNotice ->
                    // TODO: 클릭된 공지 상세 보기 또는 이동
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
                currentUserId = "user1", // TODO: 실제 로그인 사용자 ID로 교체
                onDismiss = { viewModel.clearSelectedPin() },
                onDetailClick = { pinId ->
                    // TODO: 핀 상세 route 확정 후 이동
                    viewModel.clearSelectedPin()
                },
                onCommunityClick = { communityPostId ->
                    // TODO: 커뮤니티 상세 route 확정 후 이동
                    viewModel.clearSelectedPin()
                },
                onEditClick = { pinId ->
                    // TODO: 핀 수정 화면 route 확정 후 이동
                },
                onDeleteClick = { pinId ->
                    viewModel.deletePinLocally(pinId)
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

        if (showPinTypeSelector) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { viewModel.closePinTypeSelector() }
            )
        }

        if (showPinTypeSelector) {
            PinTypeSelector(
                onIssueClick = {
                    viewModel.closePinTypeSelector()
                    navController.navigate("${AppDestinations.PIN_CREATION_ROUTE}?type=issue")
                },
                onCommunicationClick = {
                    viewModel.closePinTypeSelector()
                    navController.navigate("${AppDestinations.PIN_CREATION_ROUTE}?type=communication")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 88.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMapScreen() {
    IssueissyuTheme {
        MapScreen(navController = rememberNavController())
    }
}