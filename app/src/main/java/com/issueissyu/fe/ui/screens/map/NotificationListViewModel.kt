package com.issueissyu.fe.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.notification.Notification
import com.issueissyu.fe.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationListUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val hasNext: Boolean = false,
    val nextCursor: String? = null,
    val notifications: List<Notification> = emptyList(),
)

@HiltViewModel
class NotificationListViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationListUiState(isLoading = true))
    val uiState: StateFlow<NotificationListUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isLoadingMore = false,
                    errorMessage = null,
                    hasNext = false,
                    nextCursor = null,
                )
            }
            alarmRepository.getAlarmList().fold(
                onSuccess = { page ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            hasNext = page.hasNext,
                            nextCursor = page.nextCursor,
                            notifications = page.items,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message?.takeIf { msg -> msg.isNotBlank() }
                                ?: "알림 목록을 불러오지 못했습니다.",
                        )
                    }
                },
            )
        }
    }

    /** 상세 화면에서 돌아온 뒤 읽음 상태 등을 반영 (로딩 스피너 없음) */
    fun refreshNotifications() {
        viewModelScope.launch {
            alarmRepository.getAlarmList().fold(
                onSuccess = { page ->
                    _uiState.update { state ->
                        val mergedNotifications = if (state.notifications.size <= page.items.size) {
                            page.items
                        } else {
                            val refreshedById = page.items.associateBy { it.id }
                            state.notifications.map { existing ->
                                refreshedById[existing.id] ?: existing
                            }
                        }
                        state.copy(
                            hasNext = page.hasNext,
                            nextCursor = page.nextCursor,
                            notifications = mergedNotifications,
                        )
                    }
                },
                onFailure = { /* 목록 유지 */ },
            )
        }
    }

    fun markAsRead(notificationId: String) {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isUnread = false)
                    } else {
                        notification
                    }
                },
            )
        }
    }

    fun loadMoreNotifications() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isLoadingMore || !currentState.hasNext) {
            return
        }
        val cursor = currentState.nextCursor ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            alarmRepository.getAlarmList(cursor = cursor).fold(
                onSuccess = { page ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            hasNext = page.hasNext,
                            nextCursor = page.nextCursor,
                            notifications = it.notifications + page.items,
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            hasNext = false,
                            nextCursor = null,
                        )
                    }
                },
            )
        }
    }
}
