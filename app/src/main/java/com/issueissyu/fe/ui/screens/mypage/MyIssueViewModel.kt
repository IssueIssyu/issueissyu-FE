package com.issueissyu.fe.ui.screens.mypage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
class MyIssueViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MyIssueUiState())
    val uiState: StateFlow<MyIssueUiState> = _uiState.asStateFlow()

    init {
        loadDummyData()
    }

    private fun loadDummyData() {
        // 더미 데이터 (나중에 Repository로 교체)
        val dummyIssues = listOf(
            MyIssueItem(
                id = "issue_1",
                title = "쓰레기 무단투기",
                address = "서울특별시 강남구 역삼동",
                pinType = PinType.ISSUE,
                status = IssueStatus.BEFORE_RESOLUTION,
                latitude = 37.5665,
                longitude = 126.9780
            ),
            MyIssueItem(
                id = "issue_2",
                title = "보도 블럭 파손",
                address = "서울특별시 종로구 종로 1",
                pinType = PinType.ISSUE,
                status = IssueStatus.IN_PROGRESS,
                latitude = 37.5700,
                longitude = 126.9800
            ),
            MyIssueItem(
                id = "comm_2",
                title = "이x 토스트 사라짐 와",
                address = "서울특별시 중구 을지로 66",
                pinType = PinType.COMMUNICATION,
                status = null,
                latitude = 37.5600,
                longitude = 126.9900
            ),
            MyIssueItem(
                id = "comm_3",
                title = "어제 여기에서 GD 봄",
                address = "서울특별시 중구 을지로 66",
                pinType = PinType.COMMUNICATION,
                status = null,
                latitude = 37.5600,
                longitude = 126.9900
            ),
            MyIssueItem(
                id = "issue_3",
                title = "빙판 때문에 너무 미끄러움",
                address = "서울특별시 중구 을지로 66",
                pinType = PinType.ISSUE,
                status = IssueStatus.RESOLVED,
                latitude = 37.5600,
                longitude = 126.9900
            ),
            MyIssueItem(
                id = "issue_4",
                title = "불법 주정차",
                address = "서울 마포구 잔다리로 12",
                pinType = PinType.ISSUE,
                status = IssueStatus.BEFORE_RESOLUTION,
                latitude = 37.5600,
                longitude = 126.9900
            ),
            MyIssueItem(
                id = "issue_5",
                title = "불법 광고물 부착",
                address = "서울특별시 중구 을지로 66",
                pinType = PinType.ISSUE,
                status = IssueStatus.BEFORE_RESOLUTION,
                latitude = 37.5600,
                longitude = 126.9900
            ),
            MyIssueItem(
                id = "issue_6",
                title = "도로 배수구 막힘",
                address = "서울특별시 중구 을지로 66",
                pinType = PinType.ISSUE,
                status = IssueStatus.IN_PROGRESS,
                latitude = 37.5600,
                longitude = 126.9900
            ),
            MyIssueItem(
                id = "comm_4",
                title = "우리 동네 느좋 스팟",
                address = "서울특별시 중구 을지로 66",
                pinType = PinType.COMMUNICATION,
                status = null,
                latitude = 37.5600,
                longitude = 126.9900
            )
        )

        _uiState.value = _uiState.value.copy(issues = dummyIssues)
    }
}