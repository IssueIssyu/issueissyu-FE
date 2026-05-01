package com.issueissyu.fe.ui.screens.patchnote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Gray_8
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.White
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5

@Composable
fun PatchNotesRoute(
    viewModel: PatchNotesViewModel = hiltViewModel()
) {
    val patchNotes by viewModel.patchNotes.collectAsStateWithLifecycle()

    PatchNotesScreen(
        patchNotes = patchNotes,
        onPatchNoteClick = viewModel::onPatchNoteClick
    )
}

data class PatchNoteItem(
    val id: String,
    val title: String,
    val viewCount: Int,
    val locationName: String,
    val writerName: String,
    val writerLevelText: String? = null,
    val writerImageUrl: String? = null,
    val resolutionStatus: ResolutionStatus
)

enum class ResolutionStatus {
    BEFORE_RESOLUTION, // 해결 전
    IN_PROGRESS,       // 해결 중
    RESOLVED           // 해결 완료
}

fun getResolutionStatusText(status: ResolutionStatus): String {
    return when (status) {
        ResolutionStatus.BEFORE_RESOLUTION -> "해결 전"
        ResolutionStatus.IN_PROGRESS -> "진행 중"
        ResolutionStatus.RESOLVED -> "해결 완료"
    }
}

fun getPatchNoteCardColor(status: ResolutionStatus): Color {
    return when (status) {
        ResolutionStatus.BEFORE_RESOLUTION -> White
        ResolutionStatus.IN_PROGRESS -> Issue.copy(alpha = 0.2f)
        ResolutionStatus.RESOLVED -> BrandColor.copy(alpha = 0.2f)
    }
}

fun getPatchNoteBadgeColor(status: ResolutionStatus): Color {
    return when (status) {
        ResolutionStatus.BEFORE_RESOLUTION -> Gray_8
        ResolutionStatus.IN_PROGRESS -> Issue
        ResolutionStatus.RESOLVED -> BrandColor
    }
}

@Composable
fun PatchNoteStatusBadge(
    resolutionStatus: ResolutionStatus,
    modifier: Modifier = Modifier
) {
    val badgeColor = getPatchNoteBadgeColor(resolutionStatus)
    val badgeTextColor = White

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(17.dp))
            .background(badgeColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getResolutionStatusText(resolutionStatus),
            style = MaterialTheme.typography.labelSmall,
            color = badgeTextColor
        )
    }
}


@Composable
fun PatchNoteCard(
    patchNote: PatchNoteItem,
    modifier: Modifier = Modifier
) {
    val cardBackgroundColor = getPatchNoteCardColor(patchNote.resolutionStatus)
    
    val writerInfoText = patchNote.writerLevelText?.let {
        "${patchNote.writerName} $it"
    } ?: patchNote.writerName

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(15.dp))
            .clip(RoundedCornerShape(15.dp))
            .background(cardBackgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = patchNote.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "조회 ${patchNote.viewCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray_7
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp),
                        tint = Gray_7
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = patchNote.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray_7,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(min = 88.dp, max = 120.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (patchNote.writerImageUrl != null) {
                        // TODO: Coil 또는 Glide 라이브러리로 이미지 로드
                        // AsyncImage(
                        //     model = patchNote.writerImageUrl,
                        //     contentDescription = "Writer Profile",
                        //     modifier = Modifier
                        //         .size(24.dp)
                        //         .clip(CircleShape)
                        // )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Gray_3)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Writer Profile",
                            modifier = Modifier.size(24.dp),
                            tint = Gray_5
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = writerInfoText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray_7,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                PatchNoteStatusBadge(resolutionStatus = patchNote.resolutionStatus)
            }
        }
    }
}

@Composable
fun PatchNotesScreen(
    patchNotes: List<PatchNoteItem>,
    onPatchNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "패치노트",
            style = MaterialTheme.typography.headlineMedium,
            color = Title,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items = patchNotes, key = { it.id }) { patchNote ->
                PatchNoteCard(
                    patchNote = patchNote,
                    modifier = Modifier.clickable {
                        onPatchNoteClick(patchNote.id)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPatchNotesScreenContent() {
    IssueissyuTheme {
        PatchNotesScreen(
            patchNotes = listOf(
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
                )
            ),
            onPatchNoteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPatchNoteCardStates() {
    IssueissyuTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("해결 완료", style = MaterialTheme.typography.titleSmall)
            PatchNoteCard(
                patchNote = PatchNoteItem(
                    id = "1",
                    title = "도로 침수 복구 완료",
                    viewCount = 125,
                    locationName = "역삼동 테헤란로 123",
                    writerName = "관리자",
                    writerLevelText = "Level 5",
                    resolutionStatus = ResolutionStatus.RESOLVED
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("진행 중", style = MaterialTheme.typography.titleSmall)
            PatchNoteCard(
                patchNote = PatchNoteItem(
                    id = "2",
                    title = "맨홀 파손 신고 접수",
                    viewCount = 30,
                    locationName = "강남대로 456",
                    writerName = "홍길동",
                    writerLevelText = "Level 2",
                    resolutionStatus = ResolutionStatus.IN_PROGRESS
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("해결 전", style = MaterialTheme.typography.titleSmall)
            PatchNoteCard(
                patchNote = PatchNoteItem(
                    id = "3",
                    title = "신호등 고장 발생",
                    viewCount = 200,
                    locationName = "선릉역 사거리",
                    writerName = "익명",
                    writerLevelText = null,
                    resolutionStatus = ResolutionStatus.BEFORE_RESOLUTION
                )
            )
        }
    }
}