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

    fun onCategorySelected(category: CommunityTab) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update {
            it.copy(
                selectedCategory = category,
                nextCursor = null,
                hasNext = false
            )
        }
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
        val locationId = communityRegion.toLocationId()
        if (_uiState.value.region == communityRegion && _uiState.value.locationId == locationId) return
        _uiState.update {
            it.copy(
                region = communityRegion,
                locationId = locationId,
                isRegionSelectedByUser = true,
                nextCursor = null,
                hasNext = false,
                storePromotions = emptyList(),
                hotPreviews = emptyList(),
                recentNews = emptyList(),
                feedItems = emptyList(),
                isLoading = true
            )
        }
        loadFeed()
    }

    private fun loadFeed(isRefreshing: Boolean = false) {
        val apiRegion = _uiState.value.region.toCommunityRegion()
        val locationId = _uiState.value.locationId
        if (_uiState.value.region != apiRegion) {
            _uiState.update { it.copy(region = apiRegion) }
        }
        viewModelScope.launch {
            getCommunityFeedUseCase(
                tab = _uiState.value.selectedCategory,
                locationId = locationId
            )
                .onStart {
                    _uiState.update { it.copy(isLoading = !isRefreshing, isRefreshing = isRefreshing) }
                }
                .catch {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = "커뮤니티 소식을 불러오는 중 오류가 발생했습니다.") }
                }
                .collect { feed ->
                    _uiState.update {
                        if (it.selectedCategory == CommunityTab.ALL) {
                            it.copy(
                                storePromotions = feed.storePromotions,
                                hotPreviews = feed.hotPreviews,
                                recentNews = feed.recentNews,
                                feedItems = emptyList(),
                                region = feed.region.ifBlank { it.region },
                                nextCursor = feed.nextCursor,
                                hasNext = feed.hasNext,
                                isLoading = false,
                                isRefreshing = false,
                                error = null
                            )
                        } else {
                            it.copy(
                                feedItems = feed.items,
                                region = feed.region.ifBlank { it.region },
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
                    val defaultRegion = regions?.defaultCommunityRegion()
                        ?: region.toCommunityRegion()
                    _uiState.update {
                        it.copy(
                            region = defaultRegion,
                            locationId = regions?.userRegion?.locationId
                                ?: defaultRegion.toLocationId(),
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
                                locationId = regions?.userRegion?.locationId
                                    ?: communityRegion.toLocationId(),
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

    private fun String.toLocationId(): Long? {
        val source = trim()
        if (source.isBlank()) return null

        return _uiState.value.regionGroups
            .firstNotNullOfOrNull { group ->
                group.subLocations.firstOrNull { region ->
                    region.location == source || group.toCommunityRegion(region.location) == source
                }?.locationId
            }
    }
}
