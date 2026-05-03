package com.issueissyu.fe.ui.screens.collection

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.CommunicationContainer
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/// 컬렉션 화면
@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onNavigateToNoticeDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 일회성 이벤트
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is CollectionEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is CollectionEffect.NavigateToNoticeDetail -> {
                    onNavigateToNoticeDetail(effect.notice)
                }
                is CollectionEffect.ProfileUpdatedSuccess -> {}
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
    ) {
        // 상단 바
        IssueissyuTopAppBar(
            titleText = "컬렉션",
            modifier = Modifier.background(color = White)
        )

        // 나머지 내용
        CollectionContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = Modifier.weight(1f)
        )
    }
}

//나머지 내용
@Composable
private fun CollectionContent(
    uiState: CollectionUiState,
    onEvent: (CollectionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    //지도 브랜치에서 가져온 공지 관련 데이터들
    val mockNotices = remember {
        listOf(
            "[공지] 지연 없이 어제가 지켜지는 오늘을 만들어요",
            "두 번째 공지: 버그 수정 및 성능 개선",
            "세 번째 공지: 새로운 이벤트가 시작됩니다!"
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CommunicationContainer)
    ) {
        // 배경 (벚꽃 + 덤불)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_cherryblossom),
                contentDescription = "벚꽃 배경",
                modifier = Modifier.fillMaxWidth()
            )
            Image(
                painter = painterResource(id = R.drawable.img_bush_l),
                contentDescription = "덤불 배경",
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 앞쪽 요소들
        Column(modifier = Modifier.fillMaxSize()) {

            // 공지사항
            AutoScrollingNotice(
                notices = mockNotices,
                iconResId = R.drawable.ic_megaphone,
                onClick = { onEvent(CollectionEvent.NoticeClicked(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 말풍선
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.ic_bubble),
                        contentDescription = "말풍선",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = uiState.characterMessage,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        style = IssueTypo.Bold18.copy(color = Text)
                    )
                }

                // 캐릭터
                Image(
                    painter = painterResource(
                        id = uiState.selectedPin?.imageResId ?: R.drawable.ic_character_default
                    ),
                    contentDescription = "선택된 핀 캐릭터",
                    modifier = Modifier
                        .size(180.dp)
                        .padding(bottom = 20.dp),
                    alignment = Alignment.BottomCenter
                )
            }

            // 핀 컬렉션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp)
                    .background(
                        color = White,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                // 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 20.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "내 핀들", style = IssueTypo.Bold18)

                    Button(
                        onClick = { onEvent(CollectionEvent.UpdateProfile) },
                        enabled = uiState.canUpdateProfile,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandColor,
                            disabledContainerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "프로필 업데이트",
                            style = IssueTypo.Bold12,
                            color = White
                        )
                    }
                }


                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 24.dp,
                        end = 24.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.pins, key = { it.id }) { pin ->
                        PinCard(
                            pin = pin,
                            isSelected = pin.id == uiState.selectedPin?.id,
                            isCurrentProfile = pin.id == uiState.currentProfilePin?.id,
                            isBookmarked = pin.id in uiState.bookmarkedPinIds,
                            onPinClick = { onEvent(CollectionEvent.SelectPin(pin.id)) },
                            onBookmarkClick = { onEvent(CollectionEvent.ToggleBookmark(pin.id)) }
                        )
                    }
                }
            }
        }
    }
}

//핀 카드
@Composable
private fun PinCard(
    pin: PinItem,
    isSelected: Boolean,
    isCurrentProfile: Boolean,
    isBookmarked: Boolean,
    onPinClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(enabled = !pin.isLocked) { onPinClick() }
            .then(
                if (isSelected && !pin.isLocked) {
                    Modifier.border(
                        border = BorderStroke(3.dp, BrandColor),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                pin.isLocked -> Gray_3
                isCurrentProfile -> CommunicationContainer
                else -> White
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 핀 이미지
                if (pin.isLocked) {
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = pin.imageResId),
                            contentDescription = pin.name,
                            modifier = Modifier.size(80.dp),
                            alpha = 0.3f
                        )
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "잠김",
                            modifier = Modifier.size(32.dp),
                            tint = Gray_6
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = pin.imageResId),
                        contentDescription = pin.name,
                        modifier = Modifier.size(100.dp)
                    )
                }

                Text(
                    text = pin.name,
                    style = IssueTypo.Bold12,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                    color = if (pin.isLocked) Gray_5 else Color.Black,
                    maxLines = 1
                )

                if (pin.isLocked && pin.unlockCondition != null) {
                    Text(
                        text = pin.unlockCondition,
                        style = IssueTypo.Regular12,
                        textAlign = TextAlign.Center,
                        color = Gray_5,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2
                    )
                }
            }

            // 북마크 버튼
            if (!pin.isLocked) {
                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isBookmarked) "북마크 해제" else "북마크",
                        tint = if (isBookmarked) BrandColor else Gray_5
                    )
                }
            }
        }
    }
}

// 공지사항 - 자동 스크롤 관련
@Composable
fun AutoScrollingNotice(
    notices: List<String>,
    iconResId: Int,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (notices.isEmpty()) return

    var currentNoticeIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = notices) {
        if (notices.size > 1) {
            while (true) {
                delay(5000)
                currentNoticeIndex = (currentNoticeIndex + 1) % notices.size
            }
        }
    }

    val currentNotice = notices[currentNoticeIndex]

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Gray_7.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick(currentNotice) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = "공지",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(32.dp)
                .padding(end = 8.dp)
        )

        AnimatedContent(
            targetState = currentNotice,
            transitionSpec = {
                (slideInVertically(
                    animationSpec = tween(durationMillis = 300)
                ) { height -> height } + fadeIn(
                    animationSpec = tween(durationMillis = 300)
                )).togetherWith(
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 300)
                    ) { height -> -height } + fadeOut(
                        animationSpec = tween(durationMillis = 300)
                    )
                )
            },
            label = "Notice Animation"
        ) { targetNotice ->
            Text(
                text = targetNotice,
                style = IssueTypo.Regular15.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
        }
    }
}

//---프리뷰
@Preview(showBackground = true)
@Composable
fun CollectionScreenPreview() {
    CollectionScreen()
}