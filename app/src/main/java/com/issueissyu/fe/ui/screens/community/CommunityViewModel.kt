package com.issueissyu.fe.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.community.CommunityTab
import com.issueissyu.fe.domain.repository.LocationRepository
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
    private val getCommunityFeedUseCase: GetCommunityFeedUseCase,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        loadInitialRegionAndFeed()
    }

    fun onTabSelected(tab: CommunityTab) {
        if (_uiState.value.selectedTab == tab) return
        _uiState.update { it.copy(selectedTab = tab) }
        loadFeed()
    }

    fun onRefresh() {
        loadFeed(isRefreshing = true)
    }

    fun onRegionSelected(region: String) {
        if (_uiState.value.region == region) return
        _uiState.update {
            it.copy(
                region = region,
                nextCursor = null,
                hasNext = false,
                feedItems = emptyList(),
                isLoading = true
            )
        }
        loadFeed()
    }

    private fun loadFeed(isRefreshing: Boolean = false) {
        if (_uiState.value.region.isBlank()) return
        viewModelScope.launch {
            getCommunityFeedUseCase(
                tab = _uiState.value.selectedTab,
                region = _uiState.value.region
            )
                .onStart {
                    _uiState.update { it.copy(isLoading = !isRefreshing, isRefreshing = isRefreshing) }
                }
                .catch {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = "커뮤니티 소식을 불러오는 중 오류가 발생했습니다.") }
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

    private fun loadInitialRegionAndFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            locationRepository.getUserLocation()
                .onSuccess { region ->
                    _uiState.update { it.copy(region = region) }
                    loadFeed()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "동네 인증 정보를 불러오지 못했습니다."
                        )
                    }
                }
        }
    }
}
