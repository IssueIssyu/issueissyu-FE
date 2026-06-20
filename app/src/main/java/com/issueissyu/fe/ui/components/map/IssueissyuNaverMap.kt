package com.issueissyu.fe.ui.components.map

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions

@Composable
fun IssueissyuNaverMap(
    modifier: Modifier = Modifier,
    initialCameraPosition: CameraPosition? = null,
    onMapReady: (NaverMap) -> Unit,
    onCameraIdle: ((NaverMap) -> Unit)? = null, // LatLng 대신 NaverMap 자체를 넘기도록 변경 (2단계 준비)
    onMapClick: ((LatLng) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember(initialCameraPosition) {
        if (initialCameraPosition != null) {
            MapView(context, NaverMapOptions().camera(initialCameraPosition))
        } else {
            MapView(context)
        }
    }

    val currentOnMapReady by rememberUpdatedState(onMapReady)
    val currentOnCameraIdle by rememberUpdatedState(onCameraIdle)
    val currentOnMapClick by rememberUpdatedState(onMapClick)

    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
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

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.getMapAsync { map ->
                currentOnMapReady(map)
                map.addOnCameraIdleListener {
                    currentOnCameraIdle?.invoke(map)
                }
                map.setOnMapClickListener { _, latLng ->
                    currentOnMapClick?.invoke(latLng)
                }
            }
            mapView
        }
    )
}