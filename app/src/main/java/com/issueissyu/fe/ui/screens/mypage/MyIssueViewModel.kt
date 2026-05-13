package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.model.IssuePinDetail
import com.issueissyu.fe.data.model.ResolutionStatus
import com.issueissyu.fe.data.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 추후 data 연결 예정,,
data class MyIssueItem(
    val id: String,
    val title: String,
    val address: String,
    val pinType: PinType,
    val status: IssueStatus?,
    val latitude: Double,
    val longitude: Double
)

// 사용자 핀 타입
enum class PinType {
    ISSUE,
    COMMUNICATION
}

// 이슈 진척도
enum class IssueStatus {
    BEFORE_RESOLUTION,
    IN_PROGRESS,
    RESOLVED
}


data class MyIssueUiState(
    val isLoading: Boolean = false,     //추후 연결 시 사용,,
    val issues: List<MyIssueItem> = emptyList()
)

@HiltViewModel
class MyIssueViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyIssueUiState())
    val uiState: StateFlow<MyIssueUiState> = _uiState.asStateFlow()

    init {
        loadMyPins()
    }

    private fun loadMyPins() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val pins = pinRepository.getMyPins()
            
            // IssuePinDetail인 핀만 필터링하여 변환
            val issueItems = pins.filter { it.detail is IssuePinDetail }
                .map { pin ->
                    val detail = pin.detail as IssuePinDetail
                    MyIssueItem(
                        id = pin.id,
                        title = pin.title,
                        address = pin.address,
                        pinType = PinType.ISSUE,
                        status = when (detail.resolutionStatus) {
                            ResolutionStatus.BEFORE_RESOLUTION -> IssueStatus.BEFORE_RESOLUTION
                            ResolutionStatus.IN_PROGRESS -> IssueStatus.IN_PROGRESS
                            ResolutionStatus.RESOLVED -> IssueStatus.RESOLVED
                        },
                        latitude = pin.coordinate.latitude,
                        longitude = pin.coordinate.longitude
                    )
                }
            
            _uiState.value = MyIssueUiState(
                isLoading = false,
                issues = issueItems
            )
        }
    }
}