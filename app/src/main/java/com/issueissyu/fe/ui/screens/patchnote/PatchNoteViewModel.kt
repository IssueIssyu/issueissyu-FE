package com.issueissyu.fe.ui.screens.patchnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.issueissyu.fe.data.model.IssuePinDetail
import com.issueissyu.fe.data.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatchNotesViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _patchNotes = MutableStateFlow<List<PatchNoteItem>>(emptyList())
    val patchNotes: StateFlow<List<PatchNoteItem>> = _patchNotes.asStateFlow()

    init {
        loadPatchNotes()
    }

    private fun loadPatchNotes() {
        viewModelScope.launch {
            val issuePins = pinRepository.getIssuePins()

            _patchNotes.value = issuePins.mapNotNull { pin ->
                val detail = pin.detail as? IssuePinDetail ?: return@mapNotNull null

                PatchNoteItem(
                    id = pin.id,
                    title = pin.title,
                    viewCount = pin.viewCount,
                    locationName = pin.locationName ?: pin.address,
                    writerName = detail.writer.name,
                    writerImageUrl = null, // TODO: 작성자 프로필 이미지 필드 확정 시 연결
                    resolutionStatus = detail.resolutionStatus
                )
            }
        }
    }

    fun onPatchNoteClick(patchNoteId: String) {
        // TODO: 패치노트 아이템 클릭 시 상세 화면으로 이동 등의 로직 처리
    }
}
