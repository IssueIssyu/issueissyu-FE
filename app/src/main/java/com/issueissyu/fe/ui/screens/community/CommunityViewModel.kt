package com.issueissyu.fe.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.LocationRegionItem
import com.issueissyu.fe.domain.model.community.CommunityFeedItem
import com.issueissyu.fe.domain.model.community.CommunityItemKind
import com.issueissyu.fe.domain.model.community.CommunityTab
import com.issueissyu.fe.domain.repository.LocationRepository
import com.issueissyu.fe.domain.usecase.community.GetCommunityFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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
        if (_uiState.value.locationId == null) {
            loadInitialRegionAndFeed()
        } else {
            loadFeed(isRefreshing = true)
        }
    }

    fun onRegionSelected(region: LocationRegionItem) {
        if (_uiState.value.locationId == region.locationId) return
        _uiState.update {
            it.copy(
                region = region.location,
                locationId = region.locationId,
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
        viewModelScope.launch {
            val resolvedRegion = locationRepository.getRegionName(region.locationId)
                .getOrNull()
                ?: region.location
            _uiState.update { it.copy(region = resolvedRegion) }
            loadFeed()
        }
    }

    private fun loadFeed(isRefreshing: Boolean = false) {
        val tab = _uiState.value.selectedCategory
        val locationId = tab.feedLocationId(_uiState.value.locationId)
        viewModelScope.launch {
            getCommunityFeedUseCase(
                tab = tab,
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
                                storePromotions = feed.storePromotions.withoutCardNews(),
                                hotPreviews = feed.hotPreviews.withoutCardNews(),
                                recentNews = feed.recentNews.withoutCardNews(),
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
            val regionsDeferred = async { locationRepository.getRegionList() }
            val userLocationDeferred = async { locationRepository.getUserLocation() }

            val regionsResult = regionsDeferred.await()
            val userLocationResult = userLocationDeferred.await()

            val regions = regionsResult
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

            userLocationResult
                .onSuccess { region ->
                    val defaultLocationId = regions?.userRegion?.locationId
                    val defaultRegion = defaultLocationId
                        ?.let { locationRepository.getRegionName(it).getOrNull() }
                        ?: region
                    _uiState.update {
                        it.copy(
                            region = defaultRegion,
                            locationId = defaultLocationId,
                            isRegionSelectedByUser = false
                        )
                    }
                    loadFeed()
                }
                .onFailure {
                    val fallbackLocationId = regions?.userRegion?.locationId
                    val fallbackRegion = fallbackLocationId
                        ?.let { locationRepository.getRegionName(it).getOrNull() }
                        ?: regions?.userRegion?.location.orEmpty()
                    if (fallbackLocationId != null && fallbackRegion.isNotBlank()) {
                        _uiState.update {
                            it.copy(
                                region = fallbackRegion,
                                locationId = fallbackLocationId,
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
}

private fun List<CommunityFeedItem>.withoutCardNews(): List<CommunityFeedItem> {
    return filter { it.kind != CommunityItemKind.CARDNEWS }
}

private fun CommunityTab.feedLocationId(locationId: Long?): Long? {
    return when (this) {
        CommunityTab.POLICY,
        CommunityTab.CONTEST,
        CommunityTab.CARDNEWS -> null
        else -> locationId
    }
}
