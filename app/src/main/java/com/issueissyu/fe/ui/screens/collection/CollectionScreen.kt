package com.issueissyu.fe.ui.screens.collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.PI
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
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
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onNavigateToPinDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CollectionEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is CollectionEffect.NavigateToPinDetail -> {
                    onNavigateToPinDetail(effect.pinId)
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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

        if (uiState.newlyUnlocked.isNotEmpty()) {
            NewUnlockCelebrationOverlay(
                unlockedCharacters = uiState.newlyUnlocked,
                onDismiss = { viewModel.onEvent(CollectionEvent.DismissNewUnlockNotice) },
            )
        }
    }
}

private const val UNLOCK_LOCK_HOLD_MS = 650
private const val UNLOCK_LOCK_FADE_DELAY_MS = 120

// 열쇠 흔들림 효과 (잠금 해제 느낌)
private val UnlockLockShakeAngles = listOf(10f, -10f, 7f, -5f, 0f)
private val UnlockLockShakeDurationsMs = listOf(70, 70, 60, 60, 80)

//새 캐릭터 해금
@Composable
private fun NewUnlockCelebrationOverlay(
    unlockedCharacters: List<NewUnlockItem>,
    onDismiss: () -> Unit,
) {
    if (unlockedCharacters.isEmpty()) return

    val primary = unlockedCharacters.first()
    val namesText = unlockedCharacters.joinToString(", ") { it.name }
    val cardShape = RoundedCornerShape(24.dp)

    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }

    // --- 카드 등장 애니메이션 ---
    val cardAlpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(320),
        label = "unlockCardAlpha",
    )
    val cardOffsetY by animateFloatAsState(
        targetValue = if (revealed) 0f else 48f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 300f),
        label = "unlockCardOffsetY",
    )
    val cardScale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.82f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 280f),
        label = "unlockCardScale",
    )

    // --- 테두리 펄스 ---
    val borderPulse = rememberInfiniteTransition(label = "unlockBorderPulse")
    val borderAlpha by borderPulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "borderAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .graphicsLayer {
                    alpha = cardAlpha
                    translationY = cardOffsetY
                    scaleX = cardScale
                    scaleY = cardScale
                }
                .clip(cardShape)
                .border(
                    width = 2.dp,
                    color = BrandColor.copy(alpha = borderAlpha),
                    shape = cardShape,
                )
                .background(White, cardShape)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_bubble),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "새로운 친구가 생겼어!",
                        textAlign = TextAlign.Center,
                        style = IssueTypo.Bold18.copy(color = Text),
                    )
                    Text(
                        text = "\"$namesText\"",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 30.dp),
                        style = IssueTypo.ExtraBold18.copy(
                            color = BrandColor,
                            fontSize = 22.sp,
                        ),
                    )
                }
            }

            UnlockCharacterReveal(
                name = primary.name,
                imageUrl = primary.imageUrl,
                revealed = revealed,
            )

            Text(
                text = "${namesText}을(를) 만날 수 있게 되었어요!",
                style = IssueTypo.Regular15.copy(color = Gray_5),
                textAlign = TextAlign.Center,
            )

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandColor),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(text = "확인", style = IssueTypo.Bold12, color = White)
            }
        }
    }
}

@Composable
private fun UnlockCharacterReveal(
    name: String,
    imageUrl: String,
    revealed: Boolean,
) {
    var characterUnlocked by remember { mutableStateOf(false) }
    val lockRotation = remember { Animatable(0f) }

    LaunchedEffect(revealed) {
        if (!revealed) return@LaunchedEffect
        delay(UNLOCK_LOCK_HOLD_MS.toLong())
        UnlockLockShakeAngles.zip(UnlockLockShakeDurationsMs).forEach { (angle, durationMs) ->
            lockRotation.animateTo(angle, tween(durationMs))
        }
        delay(UNLOCK_LOCK_FADE_DELAY_MS.toLong())
        characterUnlocked = true
    }

    // --- 해금 후 반복 이펙트 (글로우 · 펄스 · 스파클) ---
    val infiniteTransition = rememberInfiniteTransition(label = "unlockReveal")
    val softPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "softPulse",
    )
    val innerGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "innerGlowAlpha",
    )
    val outerGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "outerGlowAlpha",
    )
    val sparkleTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sparkleTwinkle",
    )

    // --- 캐릭터 등장 ---
    val entryScale by animateFloatAsState(
        targetValue = if (characterUnlocked) 1f else 0.88f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = 260f),
        label = "entryScale",
    )
    val characterAlpha by animateFloatAsState(
        targetValue = if (characterUnlocked) 1f else 0.3f,
        animationSpec = tween(durationMillis = 400),
        label = "characterAlpha",
    )

    // --- 잠금 아이콘 ---
    val lockAlpha by animateFloatAsState(
        targetValue = if (characterUnlocked) 0f else 1f,
        animationSpec = tween(durationMillis = 350),
        label = "lockAlpha",
    )
    val lockScale by animateFloatAsState(
        targetValue = if (characterUnlocked) 1.35f else 1f,
        animationSpec = tween(durationMillis = 350),
        label = "lockScale",
    )
    val lockOffsetY by animateFloatAsState(
        targetValue = if (characterUnlocked) -28f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "lockOffsetY",
    )

    // --- 리플 이펙트 ---
    val rippleScale by animateFloatAsState(
        targetValue = if (characterUnlocked) 1.45f else 0.75f,
        animationSpec = tween(durationMillis = 1_000),
        label = "rippleScale",
    )
    val rippleAlpha by animateFloatAsState(
        targetValue = if (characterUnlocked) 0f else 0.55f,
        animationSpec = tween(durationMillis = 1_000),
        label = "rippleAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (characterUnlocked) {
            Box(
                modifier = Modifier
                    .size(248.dp)
                    .graphicsLayer {
                        scaleX = rippleScale
                        scaleY = rippleScale
                        alpha = rippleAlpha
                    }
                    .border(width = 2.dp, color = BrandColor.copy(alpha = 0.6f), shape = CircleShape),
            )
            Box(
                modifier = Modifier
                    .size(228.dp)
                    .alpha(outerGlowAlpha)
                    .background(BrandColor.copy(alpha = 0.18f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .size(204.dp)
                    .alpha(innerGlowAlpha)
                    .background(CommunicationContainer, CircleShape),
            )
            UnlockSparkles(
                twinkle = sparkleTwinkle,
                modifier = Modifier.size(240.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(204.dp)
                    .background(Gray_3, CircleShape),
            )
        }

        CollectionCharacterImage(
            imageUrl = imageUrl,
            contentDescription = name,
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.Center)
                .alpha(characterAlpha)
                .graphicsLayer {
                    scaleX = entryScale * if (characterUnlocked) softPulse else 1f
                    scaleY = entryScale * if (characterUnlocked) softPulse else 1f
                },
        )

        if (!characterUnlocked || lockAlpha > 0f) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "잠김",
                tint = Gray_6,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        alpha = lockAlpha
                        scaleX = lockScale
                        scaleY = lockScale
                        translationY = lockOffsetY
                        rotationZ = lockRotation.value
                    }
                    .size(52.dp),
            )
        }
    }
}

@Composable
private fun UnlockSparkles(
    twinkle: Float,
    modifier: Modifier = Modifier,
) {
    val sparkleAngles = listOf(0f, 55f, 110f, 165f, 220f, 275f, 330f)
    val radiusDp = 108f

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        sparkleAngles.forEachIndexed { index, angleDeg ->
            val phase = ((twinkle + index * 0.14f) % 1f).coerceIn(0f, 1f)
            val alpha = 0.35f + phase * 0.65f
            val angleRad = angleDeg * PI.toFloat() / 180f
            val x = (cos(angleRad) * radiusDp).dp
            val y = (sin(angleRad) * radiusDp).dp
            Text(
                text = "✦",
                style = IssueTypo.Bold18.copy(
                    color = BrandColor.copy(alpha = alpha),
                    fontSize = if (index % 2 == 0) 16.sp else 12.sp,
                ),
                modifier = Modifier.offset(x = x, y = y),
            )
        }
    }
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
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var expandPx by remember { mutableFloatStateOf(0f) }
    var isPanelDragging by remember { mutableStateOf(false) }
    var gridWasScrolled by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()
    val panelExpandAnim = remember { Animatable(0f) }

    val panelSnapSpec = spring<Float>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioNoBouncy,
    )

    var wasUpdatingProfile by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isUpdatingProfile, uiState.canUpdateProfile) {
        if (wasUpdatingProfile && !uiState.isUpdatingProfile && !uiState.canUpdateProfile) {
            isPanelDragging = false
            panelExpandAnim.snapTo(expandPx)
            panelExpandAnim.animateTo(0f, panelSnapSpec)
            expandPx = panelExpandAnim.value
        }
        wasUpdatingProfile = uiState.isUpdatingProfile
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(CommunicationContainer)
    ) {
        val basePanelHeightPx = constraints.maxHeight * 0.45f
        val maxExpandPx = constraints.maxHeight * 0.40f
        val maxPanelHeightPx = basePanelHeightPx + maxExpandPx

        val nestedScrollConnection = object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y

                    // 위로 스크롤: 패널이 완전히 펼쳐질 때까지 그리드 스크롤 대신 패널 확장
                    if (delta < 0 && expandPx < maxExpandPx) {
                        if (source == NestedScrollSource.Drag) isPanelDragging = true
                        val oldOffset = expandPx
                        expandPx = (expandPx - delta).coerceIn(0f, maxExpandPx)
                        return Offset(0f, oldOffset - expandPx)
                    }

                    if (source != NestedScrollSource.Drag) return Offset.Zero

                    if (delta > 0 && expandPx > 0f) {
                        val canCollapsePanel = expandPx < maxExpandPx ||
                            (gridState.firstVisibleItemIndex == 0 &&
                                gridState.firstVisibleItemScrollOffset == 0)
                        if (canCollapsePanel) {
                            isPanelDragging = true
                            val oldOffset = expandPx
                            expandPx = (expandPx - delta).coerceIn(0f, maxExpandPx)
                            return Offset(0f, oldOffset - expandPx)
                        }
                    }

                    return Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    val consumeFling = expandPx < maxExpandPx - 1f
                    val targetPx = if (consumeFling) {
                        if (available.y < 0f) maxExpandPx else 0f
                    } else {
                        if (expandPx > maxExpandPx / 2f) maxExpandPx else 0f
                    }
                    isPanelDragging = false
                    val startPx = expandPx
                    scope.launch {
                        panelExpandAnim.snapTo(startPx)
                        if (abs(targetPx - startPx) > 0.5f) {
                            panelExpandAnim.animateTo(targetPx, panelSnapSpec)
                        }
                        expandPx = panelExpandAnim.value
                    }
                    return if (consumeFling) available else Velocity.Zero
                }
            }

        LaunchedEffect(gridState) {
            snapshotFlow {
                Triple(
                    gridState.firstVisibleItemIndex,
                    gridState.firstVisibleItemScrollOffset,
                    gridState.isScrollInProgress,
                )
            }.collect { (index, scrollOffset, scrolling) ->
                if (index > 0 || scrollOffset > 0) {
                    gridWasScrolled = true
                }
                if (
                    !scrolling &&
                    gridWasScrolled &&
                    index == 0 &&
                    scrollOffset == 0 &&
                    expandPx > 0f
                ) {
                    isPanelDragging = false
                    panelExpandAnim.snapTo(expandPx)
                    panelExpandAnim.animateTo(0f, panelSnapSpec)
                    expandPx = panelExpandAnim.value
                    gridWasScrolled = false
                }
            }
        }

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

        Column(modifier = Modifier.fillMaxSize()) {
            AutoScrollingNotice(
                notices = uiState.notices.map { notice ->
                    NoticeUiModel(
                        id = notice.id,
                        title = notice.content,
                        pinId = notice.pinId,
                    )
                },
                iconResId = R.drawable.ic_megaphone,
                onClick = { clickedNotice ->
                    clickedNotice.pinId
                        ?.takeIf { it.isNotBlank() }
                        ?.let { pinId -> onEvent(CollectionEvent.NoticeClicked(pinId)) }
                },
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
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(with(density) { maxPanelHeightPx.toDp() })
                .offset {
                    val currentLayoutExpandPx =
                        if (isPanelDragging) expandPx else panelExpandAnim.value
                    IntOffset(0, (maxExpandPx - currentLayoutExpandPx).roundToInt())
                }
                .background(
                    color = White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .nestedScroll(nestedScrollConnection)
        ) {
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
                    enabled = uiState.canUpdateProfile && !uiState.isUpdatingProfile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandColor,
                        disabledContainerColor = Gray_4
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (uiState.isUpdatingProfile) "업데이트 중..." else "프로필 업데이트",
                        style = IssueTypo.Bold12,
                        color = White
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
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
                        isBookmarkLoading = pin.collectionId == uiState.bookmarkingCollectionId,
                        onPinClick = { onEvent(CollectionEvent.SelectPin(pin.id)) },
                        onBookmarkClick = { onEvent(CollectionEvent.ToggleBookmark(pin.id)) }
                    )
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
    isBookmarkLoading: Boolean = false,
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
                    enabled = !isBookmarkLoading,
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
@Preview(showBackground = true, name = "Collection · new unlock")
@Composable
private fun CollectionNewUnlockPreview() {
    val unlockedPin = PinItem(
        collectionId = 5L,
        name = "음흉씨",
        imageUrl = "",
        isLocked = false,
    )
    val uiState = CollectionUiState(
        isLoading = false,
        pins = listOf(
            unlockedPin,
            PinItem(
                collectionId = 1L,
                name = "감자빵",
                imageUrl = "",
                isBookmarked = true,
            ),
        ),
        selectedPin = unlockedPin,
        currentProfilePin = PinItem(
            collectionId = 1L,
            name = "감자빵",
            imageUrl = "",
        ),
        characterMessage = "새로운 친구가 생겼어!\n음흉씨",
        newlyUnlocked = listOf(
            NewUnlockItem(
                name = "음흉씨",
                imageUrl = "",
            ),
        ),
    )

    IssueissyuTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                IssueissyuTopAppBar(titleText = "컬렉션")
                CollectionContent(
                    uiState = uiState,
                    onEvent = {},
                    modifier = Modifier.weight(1f),
                )
            }
            NewUnlockCelebrationOverlay(
                unlockedCharacters = uiState.newlyUnlocked,
                onDismiss = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionScreenPreview() {
    CollectionScreen()
}