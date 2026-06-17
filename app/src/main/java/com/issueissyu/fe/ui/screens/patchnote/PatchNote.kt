package com.issueissyu.fe.ui.screens.patchnote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.issueissyu.fe.domain.model.pin.ResolutionStatus
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.components.ProfileImageFrame

private enum class PatchNoteWidthClass {
    Compact, Medium, Expanded
}

private data class PatchNoteCardLayoutSpec(
    val cardHorizontalPadding: Dp,
    val cardVerticalPadding: Dp,
    val cardCornerRadius: Dp,
    val rowSpacing: Dp,
    val titleFontSize: TextUnit,
    val viewCountFontSize: TextUnit,
    val writerNameFontSize: TextUnit,
    val profileSize: Dp,
    val profileBorderWidth: Dp,
    val locationIconSize: Dp,
    val locationBadgeGap: Dp,
    val writerSectionMaxWidth: Dp,
    val writerNameMaxWidth: Dp,
    val badgeMinWidth: Dp,
    val badgeHorizontalPadding: Dp,
    val badgeVerticalPadding: Dp,
    val badgeFontSize: TextUnit,
    val badgeLineHeight: TextUnit,
)

@Composable
private fun rememberPatchNoteCardLayoutSpec(maxWidth: Dp): PatchNoteCardLayoutSpec {
    val widthClass = when {
        maxWidth < 360.dp -> PatchNoteWidthClass.Compact
        maxWidth < 600.dp -> PatchNoteWidthClass.Medium
        else -> PatchNoteWidthClass.Expanded
    }

    return remember(widthClass) {
        when (widthClass) {
            PatchNoteWidthClass.Compact -> PatchNoteCardLayoutSpec(
                cardHorizontalPadding = 20.dp,
                cardVerticalPadding = 16.dp,
                cardCornerRadius = 22.dp,
                rowSpacing = 8.dp,
                titleFontSize = 18.sp,
                viewCountFontSize = 11.sp,
                writerNameFontSize = 11.sp,
                profileSize = 22.dp,
                profileBorderWidth = 1.dp,
                locationIconSize = 20.dp,
                locationBadgeGap = 12.dp,
                writerSectionMaxWidth = 132.dp,
                writerNameMaxWidth = 88.dp,
                badgeMinWidth = 72.dp,
                badgeHorizontalPadding = 14.dp,
                badgeVerticalPadding = 8.dp,
                badgeFontSize = 13.sp,
                badgeLineHeight = 20.sp,
            )

            PatchNoteWidthClass.Medium -> PatchNoteCardLayoutSpec(
                cardHorizontalPadding = 24.dp,
                cardVerticalPadding = 20.dp,
                cardCornerRadius = 26.dp,
                rowSpacing = 10.dp,
                titleFontSize = 20.sp,
                viewCountFontSize = 12.sp,
                writerNameFontSize = 12.sp,
                profileSize = 24.dp,
                profileBorderWidth = 1.5.dp,
                locationIconSize = 23.dp,
                locationBadgeGap = 16.dp,
                writerSectionMaxWidth = 168.dp,
                writerNameMaxWidth = 120.dp,
                badgeMinWidth = 80.dp,
                badgeHorizontalPadding = 18.dp,
                badgeVerticalPadding = 10.dp,
                badgeFontSize = 14.sp,
                badgeLineHeight = 22.sp,
            )

            PatchNoteWidthClass.Expanded -> PatchNoteCardLayoutSpec(
                cardHorizontalPadding = 28.dp,
                cardVerticalPadding = 22.dp,
                cardCornerRadius = 28.dp,
                rowSpacing = 12.dp,
                titleFontSize = 22.sp,
                viewCountFontSize = 13.sp,
                writerNameFontSize = 13.sp,
                profileSize = 28.dp,
                profileBorderWidth = 2.dp,
                locationIconSize = 26.dp,
                locationBadgeGap = 20.dp,
                writerSectionMaxWidth = 220.dp,
                writerNameMaxWidth = 160.dp,
                badgeMinWidth = 88.dp,
                badgeHorizontalPadding = 20.dp,
                badgeVerticalPadding = 11.dp,
                badgeFontSize = 15.sp,
                badgeLineHeight = 24.sp,
            )
        }
    }
}

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
private fun PatchNoteStatusBadge(
    resolutionStatus: ResolutionStatus,
    layout: PatchNoteCardLayoutSpec,
    modifier: Modifier = Modifier
) {
    val badgeColor = getPatchNoteBadgeColor(resolutionStatus)

    Box(
        modifier = modifier
            .heightIn(min = layout.badgeVerticalPadding * 2 + 18.dp)
            .defaultMinSize(minWidth = layout.badgeMinWidth)
            .clip(RoundedCornerShape(50))
            .background(badgeColor)
            .padding(
                horizontal = layout.badgeHorizontalPadding,
                vertical = layout.badgeVerticalPadding,
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getResolutionStatusText(resolutionStatus),
            style = IssueTypo.Bold12.copy(
                fontSize = layout.badgeFontSize,
                lineHeight = layout.badgeLineHeight,
                platformStyle = PlatformTextStyle(includeFontPadding = true),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.None,
                ),
            ),
            color = White,
            maxLines = 1
        )
    }
}


private const val LoadMoreThreshold = 3
private val ExpandedPatchNoteCardMaxWidth = 560.dp

@Composable
fun PatchNoteCard(
    patchNote: PatchNoteItem,
    maxWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val cardBackgroundColor = getPatchNoteCardColor(patchNote.resolutionStatus)
    val layout = rememberPatchNoteCardLayoutSpec(maxWidth)
    val cardShape = RoundedCornerShape(layout.cardCornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(elevation = 4.dp, shape = cardShape)
            .clip(cardShape)
            .background(cardBackgroundColor)
            .padding(
                horizontal = layout.cardHorizontalPadding,
                vertical = layout.cardVerticalPadding,
            )
    ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(layout.rowSpacing)
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
                            style = IssueTypo.Bold18.copy(
                                color = Title,
                                fontSize = layout.titleFontSize,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "조회 ${patchNote.viewCount}",
                            style = IssueTypo.Regular15,
                            color = Gray_6,
                            fontSize = layout.viewCountFontSize
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.widthIn(max = layout.writerSectionMaxWidth)
                    ) {
                        ProfileImageFrame(
                            size = layout.profileSize,
                            imageUrl = patchNote.writerImageUrl,
                            borderWidth = layout.profileBorderWidth,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = patchNote.writerName,
                            style = IssueTypo.Bold12,
                            fontSize = layout.writerNameFontSize,
                            color = Gray_7,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = layout.writerNameMaxWidth)
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
                            .padding(end = layout.locationBadgeGap),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(layout.locationIconSize),
                            tint = Gray_7
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = patchNote.locationName,
                            style = IssueTypo.Regular16,
                            color = Gray_7,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    PatchNoteStatusBadge(
                        resolutionStatus = patchNote.resolutionStatus,
                        layout = layout,
                    )
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
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val isExpanded = maxWidth >= 600.dp
                        val listHorizontalPadding = if (isExpanded) 24.dp else 16.dp
                        val contentWidth = maxWidth - (listHorizontalPadding * 2)
                        val cardLayoutWidth = if (isExpanded) {
                            minOf(contentWidth, ExpandedPatchNoteCardMaxWidth)
                        } else {
                            contentWidth
                        }
                        val cardWidthModifier = if (isExpanded) {
                            Modifier
                                .widthIn(max = ExpandedPatchNoteCardMaxWidth)
                                .fillMaxWidth()
                        } else {
                            Modifier.fillMaxWidth()
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    horizontal = listHorizontalPadding,
                                    vertical = 16.dp,
                                ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(items = uiState.patchNotes, key = { it.id }) { patchNote ->
                                PatchNoteCard(
                                    patchNote = patchNote,
                                    maxWidth = cardLayoutWidth,
                                    modifier = cardWidthModifier.clickable {
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
                ),
                maxWidth = 360.dp,
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
                ),
                maxWidth = 360.dp,
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
                ),
                maxWidth = 360.dp,
            )
        }
    }
}
