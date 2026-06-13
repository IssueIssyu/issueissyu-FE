package com.issueissyu.fe.ui.screens.map

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.issueissyu.fe.R
import com.issueissyu.fe.domain.model.notification.Notification
import com.issueissyu.fe.domain.model.notification.NotificationType
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Shop
import com.issueissyu.fe.ui.theme.White

@Composable
fun NotificationRoute(
    viewModel: NotificationListViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onItemClick: (Notification) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var shouldRefreshOnResume by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> shouldRefreshOnResume = true
                Lifecycle.Event.ON_RESUME -> {
                    if (shouldRefreshOnResume) {
                        shouldRefreshOnResume = false
                        viewModel.refreshNotifications()
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    NotificationScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadNotifications,
        onLoadMore = viewModel::loadMoreNotifications,
        onItemClick = { notification ->
            viewModel.markAsRead(notification.alarmId)
            onItemClick(notification)
        },
    )
}

@Composable
fun NotificationScreen(
    uiState: NotificationListUiState,
    onBack: () -> Unit = {},
    onRetry: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onItemClick: (Notification) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember(
        listState,
        uiState.hasNext,
        uiState.isLoadingMore,
        uiState.notifications.size,
    ) {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false

            uiState.hasNext &&
                !uiState.isLoadingMore &&
                uiState.notifications.isNotEmpty() &&
                lastVisibleIndex >= uiState.notifications.lastIndex - LoadMoreThreshold
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White),
    ) {
        IssueissyuTopAppBar(
            titleText = "알림",
            onBackClick = onBack,
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.errorMessage != null && uiState.notifications.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            style = IssueTypo.Regular16,
                            color = Gray_7,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Text("다시 시도")
                        }
                    }
                }

                uiState.notifications.isEmpty() -> {
                    Text(
                        text = "알림이 없습니다.",
                        style = IssueTypo.Regular16,
                        color = Gray_7,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items = uiState.notifications,
                            key = { it.alarmId },
                        ) { notification ->
                            NotificationRow(
                                notification = notification,
                                onClick = { onItemClick(notification) },
                            )
                            HorizontalDivider(color = Color(0xFFF0F0F0))
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
}

@Composable
fun NotificationRow(
    notification: Notification,
    onClick: () -> Unit,
) {
    val visual = notification.type.toVisual()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(visual.iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            when (val icon = visual.icon) {
                is NotificationIcon.Drawable -> Icon(
                    painter = painterResource(id = icon.resId),
                    contentDescription = visual.description,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
                is NotificationIcon.Vector -> Icon(
                    imageVector = icon.imageVector,
                    contentDescription = visual.description,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1A1A1A),
                )
                if (notification.isUnread) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF3B30)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.body,
                fontSize = 13.sp,
                color = Color(0xFF555555),
                lineHeight = 18.sp,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notification.timeAgo,
                fontSize = 12.sp,
                color = Color(0xFFAAAAAA),
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFAAAAAA),
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.CenterVertically),
        )
    }
}

private sealed interface NotificationIcon {
    data class Drawable(@DrawableRes val resId: Int) : NotificationIcon
    data class Vector(val imageVector: ImageVector) : NotificationIcon
}

private data class NotificationTypeVisual(
    val icon: NotificationIcon,
    val iconBackground: Color,
    val description: String,
)

private fun NotificationType.toVisual(): NotificationTypeVisual = when (this) {
    NotificationType.LIKE -> NotificationTypeVisual(
        icon = NotificationIcon.Vector(Icons.Default.ThumbUp),
        iconBackground = BrandColor,
        description = "공감",
    )
    NotificationType.EVENT -> NotificationTypeVisual(
        icon = NotificationIcon.Drawable(R.drawable.festival),
        iconBackground = Festival,
        description = "이벤트",
    )
    NotificationType.HOT -> NotificationTypeVisual(
        icon = NotificationIcon.Drawable(R.drawable.ic_fire),
        iconBackground = Issue,
        description = "인기",
    )
    NotificationType.STORE -> NotificationTypeVisual(
        icon = NotificationIcon.Drawable(R.drawable.shop),
        iconBackground = Shop,
        description = "가게",
    )
}

private const val LoadMoreThreshold = 3
