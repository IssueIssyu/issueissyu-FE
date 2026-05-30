package com.issueissyu.fe.ui.screens.collection

import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.issueissyu.fe.R
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.screens.map.AutoScrollingNotice
import com.issueissyu.fe.ui.screens.map.NoticeUiModel
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.CommunicationContainer
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_4
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Text
import com.issueissyu.fe.ui.theme.White

@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onNavigateToNoticeDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 일회성 이벤트
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
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

    if (uiState.newUnlockNotice.isNotEmpty()) {
        NewUnlockDialog(
            unlockNames = uiState.newUnlockNotice,
            onDismiss = { viewModel.onEvent(CollectionEvent.DismissNewUnlockNotice) },
        )
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

        when {
            uiState.isLoading && uiState.pins.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null && uiState.pins.isEmpty() -> {
                CollectionErrorState(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = { viewModel.onEvent(CollectionEvent.RetryLoad) },
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                CollectionContent(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NewUnlockDialog(
    unlockNames: List<String>,
    onDismiss: () -> Unit,
) {
    val namesText = unlockNames.joinToString(", ")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "새 캐릭터 해금!", style = IssueTypo.Bold18)
        },
        text = {
            Text(
                text = "${namesText}을(를) 만날 수 있게 되었어요!",
                style = IssueTypo.Regular15,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "확인", style = IssueTypo.Bold12.copy(color = BrandColor))
            }
        },
    )
}

@Composable
private fun CollectionErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CommunicationContainer)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = IssueTypo.Regular15,
            textAlign = TextAlign.Center,
            color = Gray_5,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandColor),
        ) {
            Text(text = "다시 시도", style = IssueTypo.Bold12, color = White)
        }
    }
}

//나머지 내용
@Composable
private fun CollectionContent(
    uiState: CollectionUiState,
    onEvent: (CollectionEvent) -> Unit,
    modifier: Modifier = Modifier
) {

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
                notices = remember {
                    listOf(
                        NoticeUiModel(
                            id = "notice_1",
                            title = "오늘의 공지: 새로운 업데이트가 있습니다!"
                        ),
                        NoticeUiModel(
                            id = "notice_2",
                            title = "두 번째 공지: 버그 수정 및 성능 개선"
                        ),
                        NoticeUiModel(
                            id = "notice_3",
                            title = "세 번째 공지: 새로운 이벤트가 시작됩니다!"
                        )
                    )
                },
                iconResId = R.drawable.ic_megaphone,
                onClick = { _ -> },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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

                CollectionCharacterImage(
                    imageUrl = uiState.selectedPin?.imageUrl,
                    contentDescription = uiState.selectedPin?.name ?: "선택된 캐릭터",
                    modifier = Modifier
                        .size(180.dp)
                        .padding(bottom = 20.dp),
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
                            disabledContainerColor = Gray_4
                        ),
                        shape = RoundedCornerShape(20.dp)
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
                            isCurrentProfile = pin.collectionId == uiState.currentProfilePin?.collectionId,
                            onPinClick = { onEvent(CollectionEvent.SelectPin(pin.id)) },
                            onBookmarkClick = { onEvent(CollectionEvent.ToggleBookmark(pin.id)) }
                        )
                    }
                }
            }
        }

        if (uiState.isLoading && uiState.pins.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun CollectionCharacterImage(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val defaultPainter = painterResource(R.drawable.ic_character_default)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter,
    ) {
        if (imageUrl.isNullOrBlank()) {
            Image(
                painter = defaultPainter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                placeholder = defaultPainter,
                error = defaultPainter,
            )
        }
    }
}

@Composable
private fun PinCard(
    pin: PinItem,
    isSelected: Boolean,
    isCurrentProfile: Boolean,
    onPinClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultPainter = painterResource(R.drawable.ic_character_default)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(enabled = !pin.isLocked) { onPinClick() }
            .then(
                if (isSelected && !pin.isLocked) {
                    Modifier.border(
                        border = BorderStroke(3.dp, BrandColor),
                        shape = RoundedCornerShape(15.dp)
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
                        AsyncImage(
                            model = pin.imageUrl,
                            contentDescription = pin.name,
                            modifier = Modifier
                                .size(80.dp)
                                .alpha(0.3f),
                            contentScale = ContentScale.Fit,
                            placeholder = defaultPainter,
                            error = defaultPainter,
                        )
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "잠김",
                            modifier = Modifier.size(32.dp),
                            tint = Gray_6
                        )
                    }
                } else {
                    AsyncImage(
                        model = pin.imageUrl,
                        contentDescription = pin.name,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = defaultPainter,
                        error = defaultPainter,
                    )
                }

                Text(
                    text = pin.name,
                    style = IssueTypo.Bold18.copy(
                        color = if (pin.isLocked) Gray_5 else Text,
                        fontSize = 16.sp,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (
                    pin.isLocked &&
                    pin.unlockCondition.isNotBlank() &&
                    pin.unlockCondition != "없음"
                ) {
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
                        imageVector = if (pin.isBookmarked) {
                            Icons.Filled.Bookmark
                        } else {
                            Icons.Outlined.BookmarkBorder
                        },
                        contentDescription = if (pin.isBookmarked) "북마크 해제" else "북마크",
                        tint = if (pin.isBookmarked) BrandColor else Gray_5
                    )
                }
            }
        }
    }
}



//---프리뷰
@Preview(showBackground = true)
@Composable
fun CollectionScreenPreview() {
    CollectionScreen()
}