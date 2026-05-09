package com.issueissyu.fe.ui.screens.pindetail

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class PinDetailTab {
    HOME,
    POST,
    RESOLUTION
}

data class PinDetailUiState(
    val isLoading: Boolean = false,
    val selectedTab: PinDetailTab = PinDetailTab.HOME,
    val errorMessage: String? = null
)

@HiltViewModel
class PinDetailViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PinDetailUiState())
    val uiState: StateFlow<PinDetailUiState> = _uiState.asStateFlow()

    fun loadPin(pinId: String) {
        // TODO: 다음 커밋에서 PinRepository를 연결해 pinId 기반 상세 데이터를 조회한다.
    }

    fun selectTab(tab: PinDetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
