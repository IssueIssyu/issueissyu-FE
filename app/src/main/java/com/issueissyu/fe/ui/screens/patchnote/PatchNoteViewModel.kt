package com.issueissyu.fe.ui.screens.patchnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.LocationRegionGroup
import com.issueissyu.fe.domain.model.LocationRegions
import com.issueissyu.fe.domain.model.PatchNote
import com.issueissyu.fe.domain.repository.LocationRepository
import com.issueissyu.fe.domain.repository.MapRepository
import com.issueissyu.fe.ui.screens.community.toCommunityRegion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatchNotesUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val paginationErrorMessage: String? = null,
    val hasNext: Boolean = false,
    val nextCursor: String? = null,
    val patchNotes: List<PatchNoteItem> = emptyList()
)

@HiltViewModel
class PatchNotesViewModel @Inject constructor(
    private val mapRepository: MapRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatchNotesUiState(isLoading = true))
    val uiState: StateFlow<PatchNotesUiState> = _uiState.asStateFlow()
    private var currentRegion: String? = null

    init {
        loadPatchNotes()
    }

    fun loadPatchNotes() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isLoadingMore = false,
                    errorMessage = null,
                    paginationErrorMessage = null,
                    hasNext = false,
                    nextCursor = null
                )
            }
            val region = resolvePatchNoteRegion().getOrElse { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message?.takeIf { msg -> msg.isNotBlank() }
                            ?: "인증된 동네를 불러오지 못했습니다"
                    )
                }
                return@launch
            }
            mapRepository.getPatchNotes(region = region, size = PatchNotesPageSize).fold(
                onSuccess = { page ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            paginationErrorMessage = null,
                            hasNext = page.hasNext,
                            nextCursor = page.nextCursor,
                            patchNotes = page.items.map { item -> item.toUiItem() }
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message?.takeIf { msg -> msg.isNotBlank() }
                                ?: "목록을 불러오지 못했습니다"
                        )
                    }
                }
            )
        }
    }

    fun loadMorePatchNotes() {
        val currentState = _uiState.value
        val cursor = currentState.nextCursor

        if (
            currentState.isLoading ||
            currentState.isLoadingMore ||
            !currentState.hasNext ||
            cursor == null
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingMore = true, paginationErrorMessage = null)
            }
            val region = currentRegion ?: resolvePatchNoteRegion().getOrElse { e ->
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        paginationErrorMessage = e.message?.takeIf { msg -> msg.isNotBlank() }
                            ?: "인증된 동네를 불러오지 못했습니다"
                    )
                }
                return@launch
            }
            mapRepository.getPatchNotes(region = region, size = PatchNotesPageSize, cursor = cursor).fold(
                onSuccess = { page ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            paginationErrorMessage = null,
                            hasNext = page.hasNext,
                            nextCursor = page.nextCursor,
                            patchNotes = it.patchNotes + page.items.map { item -> item.toUiItem() }
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            paginationErrorMessage = e.message?.takeIf { msg -> msg.isNotBlank() }
                                ?: "목록을 더 불러오지 못했습니다"
                        )
                    }
                }
            )
        }
    }

    fun onPatchNoteClick(patchNoteId: String) {
        // TODO: 패치노트 아이템 클릭 시 상세 화면으로 이동 등의 로직 처리
    }

    private suspend fun resolvePatchNoteRegion(): Result<String> {
        currentRegion?.takeIf { it.isNotBlank() }?.let { return Result.success(it) }

        val regions = locationRepository.getRegionList().getOrNull()
        val region = regions?.defaultPatchNoteRegion()
            ?: locationRepository.getUserLocation().getOrNull()
                ?.toPatchNoteRegion(regions)

        return if (region.isNullOrBlank()) {
            Result.failure(Exception("인증된 동네를 찾을 수 없습니다."))
        } else {
            currentRegion = region
            Result.success(region)
        }
    }

    private fun LocationRegions.defaultPatchNoteRegion(): String? {
        val userRegion = userRegion ?: return null
        return groups.toPatchNoteRegion(userRegion.locationId)
            ?: userRegion.location.toPatchNoteRegion(this)
    }

    private fun List<LocationRegionGroup>.toPatchNoteRegion(locationId: Long): String? {
        forEach { group ->
            val location = group.subLocations.firstOrNull { it.locationId == locationId }
            if (location != null) {
                return group.toCommunityRegion(location.location)
            }
        }
        return null
    }

    private fun String.toPatchNoteRegion(regions: LocationRegions?): String {
        val source = trim()
        if (source.isBlank()) return source

        return regions?.groups
            ?.firstNotNullOfOrNull { group ->
                group.subLocations.firstOrNull { region ->
                    region.location == source || group.toCommunityRegion(region.location) == source
                }?.let { region -> group.toCommunityRegion(region.location) }
            }
            ?: source
    }

    private fun PatchNote.toUiItem(): PatchNoteItem {
        return PatchNoteItem(
            id = id,
            title = title,
            viewCount = viewCount,
            locationName = locationName,
            writerName = writerName,
            writerImageUrl = writerImageUrl,
            resolutionStatus = resolutionStatus,
        )
    }

    private companion object {
        const val PatchNotesPageSize = 20
    }
}
