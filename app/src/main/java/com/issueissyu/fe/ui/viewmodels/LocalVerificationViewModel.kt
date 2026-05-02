package com.issueissyu.fe.ui.viewmodels

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalVerificationViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val event = _event.asSharedFlow()

    private val locationFlow = MutableSharedFlow<Pair<Double, Double>>(extraBufferCapacity = 1)

    private var lastLat: Double? = null
    private var lastLng: Double? = null

    private val cache = mutableMapOf<String, String>()

    init {
        observeLocation()
    }

    data class UiState(
        val currentAddress: String = "",
        val latitude: Double? = null,
        val longitude: Double? = null,
        val isLoading: Boolean = false
    )

    sealed interface UiEvent {
        data object NavigateNext : UiEvent
        data class ShowError(val message: String) : UiEvent
    }

    //위치
    private fun observeLocation() {
        viewModelScope.launch {
            locationFlow
                .debounce(400)  //0.4초 동안 멈춰야 측정되게끔 함. 너무 많이 호출되면 비용이...
                .collect { (lat, lng) ->
                    fetchAddress(lat, lng)
                }
        }
    }

    fun onMapMoved(lat: Double, lng: Double) {

        // 거리 필터 (30m 이하는 무시)
        if (lastLat != null && lastLng != null) {
            val distance = distanceBetween(lastLat!!, lastLng!!, lat, lng)
            if (distance < 30) return
        }

        lastLat = lat
        lastLng = lng


        _uiState.update {
            it.copy(
                currentAddress = "주소 찾는 중...",
                latitude = lat,
                longitude = lng
            )
        }

        locationFlow.tryEmit(lat to lng)
    }

    private suspend fun fetchAddress(lat: Double, lng: Double) {
        try {
            val key = "%.3f,%.3f".format(lat, lng)

            // 캐싱
            cache[key]?.let { cached ->
                _uiState.update { it.copy(currentAddress = cached) }
                return
            }

            // API 연결
            delay(300)

            val address = when {
                lat in 37.4..37.7 && lng in 126.8..127.2 ->
                    "서울시 마포구"
                else ->
                    "위도: %.4f, 경도: %.4f".format(lat, lng)
            }

            cache[key] = address

            _uiState.update { it.copy(currentAddress = address) }

        } catch (e: Exception) {
            _event.emit(UiEvent.ShowError("주소를 불러오지 못했습니다"))
        }
    }

    fun registerLocation() {
        val state = _uiState.value

        if (state.latitude == null || state.longitude == null) {
            viewModelScope.launch {
                _event.emit(UiEvent.ShowError("위치 정보가 없습니다"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                delay(1000)
                _event.emit(UiEvent.NavigateNext)
            } catch (e: Exception) {
                _event.emit(UiEvent.ShowError("등록 실패"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    //거리 차이 계산
    private fun distanceBetween(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val result = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, result)
        return result[0].toDouble()
    }
}