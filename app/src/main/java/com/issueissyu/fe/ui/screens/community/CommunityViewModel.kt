package com.issueissyu.fe.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.community.CommunityTab
import com.issueissyu.fe.domain.usecase.community.GetCommunityFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val getCommunityFeedUseCase: GetCommunityFeedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    fun onTabSelected(tab: CommunityTab) {
        if (_uiState.value.selectedTab == tab) return
        _uiState.update { it.copy(selectedTab = tab) }
        loadFeed()
    }

    fun onRefresh() {
        loadFeed(isRefreshing = true)
    }

    private fun loadFeed(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            getCommunityFeedUseCase(
                tab = _uiState.value.selectedTab,
                region = _uiState.value.region
            )
                .onStart {
                    _uiState.update { it.copy(isLoading = !isRefreshing, isRefreshing = isRefreshing) }
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = e.message) }
                }
                .collect { feed ->
                    _uiState.update {
                        it.copy(
                            feedItems = feed.items,
                            nextCursor = feed.nextCursor,
                            hasNext = feed.hasNext,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }
                }
        }
    }
}
