package com.issueissyu.fe.ui.components.location

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.issueissyu.fe.ui.components.CommonButton
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Gray_1
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

private object LocalAreaPickerDefaults {
    const val PIN_SIZE = 48
    const val CARD_RADIUS = 30
    const val BUTTON_RADIUS = 12
    const val ADDRESS_BOX_HEIGHT = 56
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalAreaPickerContent(
    titleText: String,
    addressText: String,
    isLoading: Boolean,
    isConfirmEnabled: Boolean,
    onConfirmClick: () -> Unit,
    onCurrentLocationReady: (latitude: Double, longitude: Double) -> Unit,
    onLocationUnavailable: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var naverMapInstance by remember { mutableStateOf<NaverMap?>(null) }
    var isLocationReported by remember { mutableStateOf(false) }
    var locationCts by remember { mutableStateOf<CancellationTokenSource?>(null) }

    fun reportLocation(lat: Double, lng: Double) {
        if (isLocationReported) return
        isLocationReported = true
        onCurrentLocationReady(lat, lng)
    }

    fun notifyLocationUnavailable(message: String) {
        isLocationReported = false
        onLocationUnavailable()
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun hasLocationPermission(): Boolean {
        return locationPermissions.any { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    fun moveToCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationCts?.cancel()
        val cts = CancellationTokenSource()
        locationCts = cts

        try {
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val target = LatLng(location.latitude, location.longitude)
                        naverMapInstance?.moveCamera(CameraUpdate.scrollTo(target))
                        reportLocation(target.latitude, target.longitude)
                        return@addOnSuccessListener
                    }

                    fusedLocationClient.lastLocation.addOnSuccessListener { lastKnown ->
                        if (lastKnown != null) {
                            val target = LatLng(lastKnown.latitude, lastKnown.longitude)
                            naverMapInstance?.moveCamera(CameraUpdate.scrollTo(target))
                            reportLocation(target.latitude, target.longitude)
                        } else {
                            notifyLocationUnavailable(
                                "현재 위치를 가져오지 못했습니다. GPS를 켠 뒤 다시 시도해 주세요.",
                            )
                        }
                    }
                }
        } catch (_: SecurityException) {
            notifyLocationUnavailable("현재 위치 접근에 실패했습니다. 위치 권한을 확인해 주세요.")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (permissions.values.any { it }) {
            isLocationReported = false
            moveToCurrentLocation()
        } else {
            notifyLocationUnavailable("위치 권한이 필요합니다. 권한 허용 후 다시 시도해 주세요.")
        }
    }

    fun onMapReady(map: NaverMap) {
        map.uiSettings.apply {
            isScrollGesturesEnabled = false
            isZoomGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
        }

        if (hasLocationPermission()) {
            moveToCurrentLocation()
        } else {
            permissionLauncher.launch(locationPermissions)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            IssueissyuTopAppBar(
                titleText = titleText,
                onBackClick = onBackClick,
            )
        },
        containerColor = White,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            LocalAreaPickerMapSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onMapReady = { map ->
                    naverMapInstance = map
                    onMapReady(map)
                },
            )

            LocalAreaPickerAddressCard(
                address = addressText,
                isLoading = isLoading,
                isConfirmEnabled = isConfirmEnabled,
                onConfirmClick = onConfirmClick,
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && hasLocationPermission() && !isLocationReported) {
                moveToCurrentLocation()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            locationCts?.cancel()
            locationCts = null
        }
    }
}

@Composable
private fun LocalAreaPickerMapSection(
    onMapReady: (NaverMap) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        LocalAreaPickerNaverMap(
            modifier = Modifier.fillMaxSize(),
            onMapReady = onMapReady,
        )

        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "현재 위치",
            modifier = Modifier
                .align(Alignment.Center)
                .size(LocalAreaPickerDefaults.PIN_SIZE.dp),
            tint = BrandColor,
        )
    }
}

@Composable
private fun LocalAreaPickerAddressCard(
    address: String,
    isLoading: Boolean,
    isConfirmEnabled: Boolean,
    onConfirmClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = LocalAreaPickerDefaults.CARD_RADIUS.dp,
                    topEnd = LocalAreaPickerDefaults.CARD_RADIUS.dp,
                ),
            )
            .background(White)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "우리 동네",
            style = IssueTypo.Regular15.copy(color = Gray_7),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(LocalAreaPickerDefaults.ADDRESS_BOX_HEIGHT.dp)
                .clip(RoundedCornerShape(LocalAreaPickerDefaults.BUTTON_RADIUS.dp))
                .background(Gray_1),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = address,
                style = IssueTypo.Bold18.copy(color = Title),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        CommonButton(
            onClick = onConfirmClick,
            modifier = Modifier.fillMaxWidth(),
            text = "확인",
            isEnabled = !isLoading && isConfirmEnabled,
        )
    }
}

@Composable
private fun LocalAreaPickerNaverMap(
    onMapReady: (NaverMap) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier.background(Gray_3),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "지도 프리뷰",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember(context) { MapView(context) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }

        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)

        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            mapView.onCreate(Bundle())
        }
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            mapView.onStart()
        }
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.onResume()
        }

        onDispose {
            lifecycle.removeObserver(observer)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                mapView.onPause()
                mapView.onStop()
            }
        }
    }

    // Composable이 composition에서 제거될 때만 MapView를 destroy한다.
    DisposableEffect(mapView) {
        onDispose {
            mapView.onDestroy()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                getMapAsync(onMapReady)
            }
        },
    )
}
