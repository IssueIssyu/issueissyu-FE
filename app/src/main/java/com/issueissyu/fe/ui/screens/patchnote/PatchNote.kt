package com.issueissyu.fe.ui.screens.patchnote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.IssueContainerLight
import com.issueissyu.fe.ui.theme.CommunicationContainerLight
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.White
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.domain.model.pin.ResolutionStatus
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar

@Composable
fun PatchNotesRoute(
    viewModel: PatchNotesViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onPatchNoteClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PatchNotesScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = viewModel::loadPatchNotes,
        onLoadMore = viewModel::loadMorePatchNotes,
        onPatchNoteClick = onPatchNoteClick
    )
}

data class PatchNoteItem(
    val id: String,
    val title: String,
    val viewCount: Int,
    val locationName: String,
    val writerName: String,
    val writerImageUrl: String? = null,
    val resolutionStatus: ResolutionStatus
)

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
        ResolutionStatus.IN_PROGRESS -> IssueContainerLight
        ResolutionStatus.RESOLVED -> CommunicationContainerLight
    }
}

fun getPatchNoteBadgeColor(status: ResolutionStatus): Color {
    return when (status) {
        ResolutionStatus.BEFORE_RESOLUTION -> Color.Black
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

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 80.dp, minHeight = 30.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(badgeColor)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getResolutionStatusText(resolutionStatus),
            style = IssueTypo.Bold18,
            color = White,
            maxLines = 1
        )
    }
}


@Composable
fun PatchNoteCard(
    patchNote: PatchNoteItem,
    modifier: Modifier = Modifier
) {
    val cardBackgroundColor = getPatchNoteCardColor(patchNote.resolutionStatus)
    val cardShape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(128.dp)
            .shadow(elevation = 4.dp, shape = cardShape)
            .clip(cardShape)
            .background(cardBackgroundColor)
            .padding(horizontal = 26.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = patchNote.title,
                        style = IssueTypo.Bold18.copy(color = Title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "조회 ${patchNote.viewCount}",
                        style = IssueTypo.Regular15,
                        color = Gray_6
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.widthIn(max = 168.dp)
                ) {
                    if (!patchNote.writerImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = patchNote.writerImageUrl,
                            contentDescription = "Writer Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Gray_3)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Writer Profile",
                            modifier = Modifier.size(32.dp),
                            tint = Gray_5
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = patchNote.writerName,
                        style = IssueTypo.Bold12,
                        fontSize = 14.sp,
                        color = Gray_7,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 120.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(30.dp),
                        tint = Gray_7
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = patchNote.locationName,
                        style = IssueTypo.Regular16,
                        color = Gray_7,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                PatchNoteStatusBadge(resolutionStatus = patchNote.resolutionStatus)
            }
        }
    }
}

@Composable
fun PatchNotesScreen(
    uiState: PatchNotesUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onPatchNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember(
        listState,
        uiState.hasNext,
        uiState.isLoadingMore,
        uiState.patchNotes.size
    ) {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false

            uiState.hasNext &&
                !uiState.isLoadingMore &&
                uiState.patchNotes.isNotEmpty() &&
                lastVisibleIndex >= uiState.patchNotes.lastIndex - LoadMoreThreshold
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IssueissyuTopAppBar(
            titleText = "패치노트",
            onBackClick = onBackClick
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            style = IssueTypo.Regular16,
                            color = Gray_7
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Text("다시 시도")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(items = uiState.patchNotes, key = { it.id }) { patchNote ->
                            PatchNoteCard(
                                patchNote = patchNote,
                                modifier = Modifier.clickable {
                                    onPatchNoteClick(patchNote.id)
                                }
                            )
                        }

                        if (uiState.isLoadingMore) {
                            item(key = "patch_note_loading_more") {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .size(28.dp)
                                )
                            }
                        }

                        uiState.paginationErrorMessage?.let { message ->
                            item(key = "patch_note_pagination_error") {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = message,
                                        style = IssueTypo.Regular16,
                                        color = Gray_7
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = onLoadMore) {
                                        Text("다시 시도")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPatchNotesScreenContent() {
    IssueissyuTheme {
        PatchNotesScreen(
            uiState = PatchNotesUiState(
                patchNotes = listOf(
                    PatchNoteItem(
                        id = "1",
                        title = "도로 침수 복구 완료",
                        viewCount = 125,
                        locationName = "역삼동 테헤란로 123",
                        writerName = "관리자",
                        resolutionStatus = ResolutionStatus.RESOLVED
                    ),
                    PatchNoteItem(
                        id = "2",
                        title = "맨홀 파손 신고 접수",
                        viewCount = 30,
                        locationName = "강남대로 456",
                        writerName = "홍길동",
                        resolutionStatus = ResolutionStatus.IN_PROGRESS
                    ),
                    PatchNoteItem(
                        id = "3",
                        title = "신호등 고장 발생",
                        viewCount = 200,
                        locationName = "선릉역 사거리",
                        writerName = "익명",
                        resolutionStatus = ResolutionStatus.BEFORE_RESOLUTION
                    )
                )
            ),
            onBackClick = {},
            onRetry = {},
            onLoadMore = {},
            onPatchNoteClick = {}
        )
    }
}

private const val LoadMoreThreshold = 3

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
                    resolutionStatus = ResolutionStatus.BEFORE_RESOLUTION
                )
            )
        }
    }
}
