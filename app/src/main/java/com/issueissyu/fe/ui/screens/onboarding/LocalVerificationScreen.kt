package com.issueissyu.fe.ui.screens.onboarding

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.issueissyu.fe.ui.components.CommonButton
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.*
import com.issueissyu.fe.ui.viewmodels.LocalVerificationViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap

private object MapDefaults {
    val SEOUL_CITY_HALL = LatLng(37.5665, 126.9780)
    const val PIN_SIZE = 48
    const val CARD_RADIUS = 30
    const val BUTTON_RADIUS = 12
    const val BUTTON_HEIGHT = 56
    const val ADDRESS_BOX_HEIGHT = 56
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalVerificationScreen(
    onBackClick: () -> Unit = {},
    onCompleteRegisterClick: () -> Unit = {},
    viewModel: LocalVerificationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }

    // 권한 체크
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    // 현재 위치로 이동
    fun moveToCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val targetLocation = if (location != null) {
                    LatLng(location.latitude, location.longitude)
                } else {
                    MapDefaults.SEOUL_CITY_HALL
                }

                naverMapInstance?.moveCamera(CameraUpdate.scrollTo(targetLocation))
                viewModel.onMapMoved(targetLocation.latitude, targetLocation.longitude)
            }
        } catch (e: SecurityException) {
            // 권한 있어도 예외 발생 가능
            naverMapInstance?.moveCamera(CameraUpdate.scrollTo(MapDefaults.SEOUL_CITY_HALL))
            viewModel.onMapMoved(
                MapDefaults.SEOUL_CITY_HALL.latitude,
                MapDefaults.SEOUL_CITY_HALL.longitude
            )
        }
    }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            moveToCurrentLocation()
        } else {
            // 권한 거부 - 기본 위치로
            naverMapInstance?.moveCamera(CameraUpdate.scrollTo(MapDefaults.SEOUL_CITY_HALL))
            viewModel.onMapMoved(
                MapDefaults.SEOUL_CITY_HALL.latitude,
                MapDefaults.SEOUL_CITY_HALL.longitude
            )
        }
    }

    // 지도 준비시 권한 확인
    fun onMapReady(map: NaverMap) {
        // 카메라 이동 리스너
        map.addOnCameraIdleListener {
            val center = map.cameraPosition.target
            viewModel.onMapMoved(center.latitude, center.longitude)
        }

        // 권한 확인 후 초기 위치 설정
        if (hasLocationPermission()) {
            moveToCurrentLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is LocalVerificationViewModel.UiEvent.NavigateNext -> {
                    onCompleteRegisterClick()
                }
                is LocalVerificationViewModel.UiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            IssueissyuTopAppBar(
                titleText = "동네 설정",
                onBackClick = onBackClick
            )
        },
        containerColor = White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 지도 영역
                MapSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onMapReady = { map ->
                        naverMapInstance = map
                        onMapReady(map)
                    }
                )

                // 하단 정보 카드
                AddressInfoCard(
                    address = uiState.currentAddress.ifEmpty { "위치를 불러오는 중..." },
                    isLoading = uiState.isLoading,
                    isConfirmEnabled = !uiState.isLoading && uiState.currentAddress.isNotEmpty(),
                    onConfirmClick = viewModel::registerLocation
                )
            }
        }
    }
}

@Composable
private fun MapSection(
    modifier: Modifier = Modifier,
    onMapReady: (NaverMap) -> Unit
) {
    Box(modifier = modifier) {
        NaverMapComposable(
            modifier = Modifier.fillMaxSize(),
            onMapReady = onMapReady
        )

        // 중앙 고정 핀
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "선택된 위치",
            modifier = Modifier
                .align(Alignment.Center)
                .size(MapDefaults.PIN_SIZE.dp),
            tint = BrandColor
        )
    }
}

@Composable
private fun AddressInfoCard(
    address: String,
    isLoading: Boolean,
    isConfirmEnabled: Boolean,
    onConfirmClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = MapDefaults.CARD_RADIUS.dp, topEnd = MapDefaults.CARD_RADIUS.dp))
            .background(White)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 레이블
        Text(
            text = "우리 동네",
            style = IssueTypo.Regular15.copy(color = Gray_7),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 주소 표시
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MapDefaults.ADDRESS_BOX_HEIGHT.dp)
                .clip(RoundedCornerShape(MapDefaults.BUTTON_RADIUS.dp))
                .background(Gray_1),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = address,
                style = IssueTypo.Bold18.copy(color = Title)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        //확인 버튼

        CommonButton(
            onClick = onConfirmClick,
            modifier =Modifier
                .fillMaxWidth(),
            text = "확인",
            isEnabled = isConfirmEnabled
        )
    }
}

@Composable
private fun NaverMapComposable(
    modifier: Modifier = Modifier,
    onMapReady: (NaverMap) -> Unit
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier.background(Gray_3),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "지도 프리뷰",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                getMapAsync(onMapReady)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLocalVerificationScreen() {
    IssueissyuTheme {
        LocalVerificationScreen()
    }
}