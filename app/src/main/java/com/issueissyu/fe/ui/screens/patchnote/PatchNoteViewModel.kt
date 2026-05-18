package com.issueissyu.fe.ui.screens.patchnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.domain.model.PatchNote
import com.issueissyu.fe.domain.repository.MapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatchNotesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val patchNotes: List<PatchNoteItem> = emptyList()
)

@HiltViewModel
class PatchNotesViewModel @Inject constructor(
    private val mapRepository: MapRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatchNotesUiState(isLoading = true))
    val uiState: StateFlow<PatchNotesUiState> = _uiState.asStateFlow()

    init {
        loadPatchNotes()
    }

    fun loadPatchNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            mapRepository.getPatchNotes(size = PatchNotesPageSize).fold(
                onSuccess = { page ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
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

    fun onPatchNoteClick(patchNoteId: String) {
        // TODO: 패치노트 아이템 클릭 시 상세 화면으로 이동 등의 로직 처리
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
