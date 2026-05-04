package com.issueissyu.fe.ui.components.map

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap

@Composable
fun IssueissyuNaverMap(
    modifier: Modifier = Modifier,
    onMapReady: (NaverMap) -> Unit,
    onCameraIdle: ((NaverMap) -> Unit)? = null, // LatLng 대신 NaverMap 자체를 넘기도록 변경 (2단계 준비)
    onMapClick: ((LatLng) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

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
                onMapReady(map)
                onCameraIdle?.let { callback ->
                    map.addOnCameraIdleListener { callback(map) } // map 객체 자체를 넘기도록 변경
                }
                onMapClick?.let { callback ->
                    map.setOnMapClickListener { _, latLng -> callback(latLng) } // addOnMapClickListener -> setOnMapClickListener
                }
            }
            mapView
        }
    )
}