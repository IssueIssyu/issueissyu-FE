package com.issueissyu.fe.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.LocationRegionGroup
import com.issueissyu.fe.domain.model.LocationRegions
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
        if (_uiState.value.region.isBlank()) {
            loadInitialRegionAndFeed()
        } else {
            loadFeed(isRefreshing = true)
        }
    }

    fun onRegionSelected(region: String) {
        val communityRegion = region.toCommunityRegion()
        if (_uiState.value.region == communityRegion) return
        _uiState.update {
            it.copy(
                region = communityRegion,
                isRegionSelectedByUser = true,
                nextCursor = null,
                hasNext = false,
                feedItems = emptyList(),
                isLoading = true
            )
        }
        loadFeed()
    }

    private fun loadFeed(isRefreshing: Boolean = false) {
        val apiRegion = _uiState.value.region.toCommunityRegion()
        if (apiRegion.isBlank()) return
        if (_uiState.value.region != apiRegion) {
            _uiState.update { it.copy(region = apiRegion) }
        }
        viewModelScope.launch {
            getCommunityFeedUseCase(
                tab = _uiState.value.selectedTab,
                region = apiRegion
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
            _uiState.update { it.copy(isLoading = true, isRegionLoading = true, error = null) }
            val regions = locationRepository.getRegionList()
                .onSuccess { regions ->
                    _uiState.update {
                        it.copy(
                            regionGroups = regions.groups,
                            isRegionLoading = false,
                            regionError = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isRegionLoading = false,
                            regionError = throwable.message ?: "지역 목록을 불러오지 못했습니다."
                        )
                    }
                }
                .getOrNull()

            locationRepository.getUserLocation()
                .onSuccess { region ->
                    _uiState.update {
                        it.copy(
                            region = regions?.defaultCommunityRegion()
                                ?: region.toCommunityRegion(),
                            isRegionSelectedByUser = false
                        )
                    }
                    loadFeed()
                }
                .onFailure {
                    val fallbackRegion = regions?.userRegion?.location.orEmpty()
                    val communityRegion = regions?.defaultCommunityRegion()
                        ?: fallbackRegion.toCommunityRegion()
                    if (communityRegion.isNotBlank()) {
                        _uiState.update {
                            it.copy(
                                region = communityRegion,
                                isRegionSelectedByUser = false
                            )
                        }
                        loadFeed()
                    } else {
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

    private fun LocationRegions.defaultCommunityRegion(): String? {
        val userRegion = userRegion ?: return null
        return groups.toCommunityRegion(userRegion.locationId)
            ?: userRegion.location.toCommunityRegion()
                .takeIf { it.isNotBlank() }
    }

    private fun List<LocationRegionGroup>.toCommunityRegion(locationId: Long): String? {
        forEach { group ->
            val location = group.subLocations.firstOrNull { it.locationId == locationId }
            if (location != null) {
                return group.toCommunityRegion(location.location)
            }
        }
        return null
    }

    private fun String.toCommunityRegion(): String {
        val source = trim()
        if (source.isBlank()) return source

        return _uiState.value.regionGroups
            .firstNotNullOfOrNull { group ->
                group.subLocations.firstOrNull { region ->
                    region.location == source || group.toCommunityRegion(region.location) == source
                }?.let { region -> group.toCommunityRegion(region.location) }
            }
            ?: source
    }
}
