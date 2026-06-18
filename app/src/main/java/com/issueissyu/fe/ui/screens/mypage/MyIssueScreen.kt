package com.issueissyu.fe.ui.screens.mypage

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.issueissyu.fe.domain.model.mypage.MyIssuePin
import com.issueissyu.fe.domain.model.pin.PinCategory
import com.issueissyu.fe.domain.model.pin.ResolutionStatus
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.CommunicationContainerLight
import com.issueissyu.fe.ui.theme.Gray_2
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueContainerLight
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
import com.issueissyu.fe.ui.theme.suiteFontFamily

@Composable
fun MyIssueScreen(
    onBackClick: () -> Unit,
    onPinClick: (pinId: String) -> Unit,
    viewModel: MyIssueViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    MyPinListContent(
        title = "내 이슈",
        uiState = uiState,
        emptyTitle = "아직 생성한 핀이 없어요",
        emptyDescription = "지도에서 우리 동네를 기록해보세요!",
        loadMoreThreshold = MyIssueViewModel.LOAD_MORE_THRESHOLD,
        onBackClick = onBackClick,
        onRetry = { viewModel.loadIssues() },
        onLoadMore = { viewModel.loadIssues(append = true) },
        onPinClick = onPinClick,
        onErrorConsumed = viewModel::clearErrorMessage,
    )
}

@Composable
fun MySolverParticipationScreen(
    onBackClick: () -> Unit,
    onPinClick: (pinId: String) -> Unit,
    viewModel: MySolverParticipationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    MyPinListContent(
        title = "해결 참여",
        uiState = uiState,
        emptyTitle = "아직 참여한 이슈가 없어요",
        emptyDescription = "시민해결사로 이웃의 이슈를 함께 해결해보세요!",
        loadMoreThreshold = MySolverParticipationViewModel.LOAD_MORE_THRESHOLD,
        onBackClick = onBackClick,
        onRetry = { viewModel.loadIssues() },
        onLoadMore = { viewModel.loadIssues(append = true) },
        onPinClick = onPinClick,
        onErrorConsumed = viewModel::clearErrorMessage,
    )
}

@Composable
internal fun MyPinListContent(
    title: String,
    uiState: MyIssueUiState,
    emptyTitle: String,
    emptyDescription: String,
    loadMoreThreshold: Int,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onPinClick: (pinId: String) -> Unit,
    onErrorConsumed: () -> Unit,
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false

            uiState.hasNext &&
                !uiState.isLoadingMore &&
                uiState.issues.isNotEmpty() &&
                lastVisibleIndex >= uiState.issues.lastIndex - loadMoreThreshold
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    ObservePinListLoadMoreError(
        errorMessage = uiState.errorMessage,
        hasItems = uiState.issues.isNotEmpty(),
        onErrorConsumed = onErrorConsumed,
    )

    Column(
        modifier = Modifier
            .background(White)
            .fillMaxSize(),
    ) {
        IssueissyuTopAppBar(
            titleText = title,
            onBackClick = onBackClick,
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null && uiState.issues.isEmpty() -> {
                PinListErrorState(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = onRetry,
                )
            }

            uiState.issues.isEmpty() -> {
                PinListEmptyState(
                    title = emptyTitle,
                    description = emptyDescription,
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 31.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.issues, key = { it.pinId }) { issue ->
                        MyIssueCard(
                            issue = issue,
                            onClick = { onPinClick(issue.pinId.toString()) },
                        )
                    }

                    if (uiState.isLoadingMore) {
                        item(key = "loading_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ObservePinListLoadMoreError(
    errorMessage: String?,
    hasItems: Boolean,
    onErrorConsumed: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(errorMessage) {
        val message = errorMessage ?: return@LaunchedEffect
        if (hasItems) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onErrorConsumed()
        }
    }
}

@Composable
private fun PinListErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = IssueTypo.Regular16.copy(color = Text),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}

@Composable
private fun PinListEmptyState(
    title: String,
    description: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = IssueTypo.Bold18.copy(color = Title),
            )
            Text(
                text = description,
                style = IssueTypo.Regular15.copy(color = Text),
            )
        }
    }
}

@Composable
fun MyIssueCard(
    issue: MyIssuePin,
    onClick: () -> Unit,
    showPinType: Boolean = true,
) {
    val cardShape = RoundedCornerShape(15.dp)
    val (pinTypeLabel, pinTypeColor) = when (issue.pinType) {
        PinCategory.ISSUE -> "이슈" to Issue
        PinCategory.COMMUNICATION -> "소통" to Communication
        else -> "핀" to Text
    }

    val cardBackgroundColor = when (issue.resolutionStatus) {
        ResolutionStatus.BEFORE_RESOLUTION -> Gray_2
        ResolutionStatus.IN_PROGRESS -> IssueContainerLight
        ResolutionStatus.RESOLVED -> CommunicationContainerLight
        null -> White
    }

    val borderModifier = if (issue.resolutionStatus == null) {
        Modifier.border(1.dp, Communication, cardShape)
    } else {
        Modifier
    }

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(borderModifier)
            .background(cardBackgroundColor, cardShape)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = issue.title,
                fontFamily = suiteFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                color = Title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            if (showPinType) {
                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.wrapContentWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = pinTypeLabel,
                        tint = pinTypeColor,
                        modifier = Modifier.size(25.dp),
                    )
                    Text(
                        text = pinTypeLabel,
                        style = IssueTypo.ExtraBold15.copy(color = Text),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "위치",
                tint = Title,
                modifier = Modifier.size(25.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = issue.address,
                style = IssueTypo.Regular15.copy(color = Text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            issue.resolutionStatus?.let { status ->
                Spacer(modifier = Modifier.width(8.dp))
                MyIssueStatusChip(status = status)
            }
        }
    }
}

@Composable
private fun MyIssueStatusChip(status: ResolutionStatus) {
    val chipShape = RoundedCornerShape(15.dp)
    val (label, backgroundColor) = when (status) {
        ResolutionStatus.BEFORE_RESOLUTION -> "해결 전" to Title
        ResolutionStatus.IN_PROGRESS -> "진행중" to Issue
        ResolutionStatus.RESOLVED -> "해결 완료" to BrandColor
    }

    Box(
        modifier = Modifier
            .wrapContentWidth(unbounded = true)
            .background(backgroundColor, chipShape)
            .padding(horizontal = 18.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = IssueTypo.Regular16.copy(
                color = White,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
