// app/src/main/java/com/issueissyu/fe/ui/screens/patchnote/PatchNoteViewModel.kt

package com.issueissyu.fe.ui.screens.patchnote

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// TODO: 핀 담당자가 공용 Pin 모델을 완성하면 MapPin, PinCategory 임포트
// import com.issueissyu.fe.data.model.MapPin // 예시

@HiltViewModel
class PatchNotesViewModel @Inject constructor(
    // TODO: 핀 데이터를 가져올 Repository 주입 (예: private val pinRepository: PinRepository)
) : ViewModel() {

    // 패치노트 화면에 표시될 PatchNoteItem 목록 상태
    private val _patchNotes = MutableStateFlow<List<PatchNoteItem>>(emptyList())
    val patchNotes: StateFlow<List<PatchNoteItem>> = _patchNotes.asStateFlow()

    init {
        // ViewModel 초기화 시 임시 Mock 데이터를 로드
        loadMockPatchNotes()
        // TODO: 실제 데이터 연동 시에는 fetchPatchNotes() 호출
    }

    // UI 개발을 위한 임시 Mock 데이터 로딩 함수 (이미 요청에 맞게 수정됨)
    private fun loadMockPatchNotes() {
        val mockData = listOf(
            PatchNoteItem(
                id = "1",
                title = "도로 침수 복구 완료",
                viewCount = 125,
                locationName = "역삼동 테헤란로 123",
                writerName = "관리자",
                writerLevelText = "Level 5",
                resolutionStatus = ResolutionStatus.RESOLVED
            ),
            PatchNoteItem(
                id = "2",
                title = "맨홀 파손 신고 접수",
                viewCount = 30,
                locationName = "강남대로 456",
                writerName = "홍길동",
                writerLevelText = "Level 2",
                resolutionStatus = ResolutionStatus.IN_PROGRESS
            ),
            PatchNoteItem(
                id = "3",
                title = "신호등 고장 발생",
                viewCount = 200,
                locationName = "선릉역 사거리",
                writerName = "익명",
                writerLevelText = null,
                resolutionStatus = ResolutionStatus.BEFORE_RESOLUTION
            ),
            PatchNoteItem(
                id = "4",
                title = "가로등 교체 완료",
                viewCount = 80,
                locationName = "테헤란로 789",
                writerName = "관리자",
                writerLevelText = "Level 5",
                resolutionStatus = ResolutionStatus.RESOLVED
            ),
            PatchNoteItem(
                id = "5",
                title = "보도블록 파손 신고",
                viewCount = 50,
                locationName = "역삼로 101",
                writerName = "김철수",
                writerLevelText = "Level 1",
                resolutionStatus = ResolutionStatus.BEFORE_RESOLUTION
            )
        )
        _patchNotes.value = mockData
    }

    // TODO: 실제 핀 데이터를 가져와 PatchNoteItem으로 변환하고 _patchNotes에 업데이트하는 함수
    /*
    private fun fetchPatchNotes() {
        // viewModelScope.launch {
        //     val allPins = pinRepository.getAllPins() // 모든 핀 가져오기 (예시)
        //     // TODO: 최종 Pin 모델 완성 후 ISSUE 카테고리 핀만 PatchNoteItem으로 변환하기
        //     val issuePins = allPins.filter { pin ->
        //         // pin.category == PinCategory.ISSUE 또는 pin.type.category == PinCategory.ISSUE
        //         true // 임시 로직
        //     }
        //     val convertedPatchNotes = issuePins.mapNotNull { pin ->
        //         // TODO: MapPin을 PatchNoteItem으로 변환하는 로직 구현 (toPatchNoteItemOrNull 함수 사용)
        //         // 현재는 임시 데이터 변환 예시
        //         PatchNoteItem(
        //             id = pin.id,
        //             title = pin.title,
        //             viewCount = 0, // TODO: 실제 조회수 필드 연결
        //             locationName = pin.locationName,
        //             writerName = "임시 작성자", // TODO: 실제 작성자 필드 연결
        //             writerLevelText = "Level ?", // TODO: 실제 레벨 필드 연결
        //             writerImageUrl = null, // TODO: 실제 이미지 URL 필드 연결
        //             resolutionStatus = ResolutionStatus.BEFORE_RESOLUTION // TODO: 실제 상태 연결
        //         )
        //     }
        //     _patchNotes.value = convertedPatchNotes
        // }
    }
    */

    fun onPatchNoteClick(patchNoteId: String) {
        // TODO: 패치노트 아이템 클릭 시 상세 화면으로 이동 등의 로직 처리
        // 네비게이션 로직 또는 다른 액션 처리 (예: navController.navigate("patch_note_detail/$patchNoteId"))
    }
}