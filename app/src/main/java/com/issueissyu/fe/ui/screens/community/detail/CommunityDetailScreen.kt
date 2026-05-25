package com.issueissyu.fe.ui.screens.community.detail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.issueissyu.fe.R
import com.issueissyu.fe.domain.model.community.CommunityComment
import com.issueissyu.fe.domain.model.community.CommunityDetail
import com.issueissyu.fe.domain.model.community.CommunityItemKind
import com.issueissyu.fe.ui.components.ActionState
import com.issueissyu.fe.ui.components.CompactSympathyButton
import com.issueissyu.fe.ui.components.GoNowButton
import com.issueissyu.fe.ui.components.SignButton
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.Gray_1
import com.issueissyu.fe.ui.theme.Gray_2
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.Orange
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// TODO: 백엔드/기획 기준 확정 후 사유 노출 임계값 조정
private const val RELIABILITY_REASON_VISIBLE_THRESHOLD = 70
private val COMMUNITY_DETAIL_ACTION_TOUCH_SIZE = 48.dp
private val COMMUNITY_DETAIL_ACTION_BUTTON_SIZE = 30.dp

@Composable
fun CommunityDetailScreen(
    viewModel: CommunityDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onMapClick: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    CommunityDetailScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = viewModel::loadDetail,
        onGoNowClick = viewModel::goNow,
        onPetitionClick = viewModel::submitPetition,
        onMapClick = onMapClick,
        onCommentSubmit = viewModel::createComment,
        onCommentUpdate = viewModel::updateComment,
        onCommunityLikeClick = viewModel::likeCommunity,
        onCommunityDeclareClick = viewModel::declareCommunity,
    )
}

@Composable
fun CommunityDetailScreenContent(
    uiState: CommunityDetailUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit = {},
    onGoNowClick: () -> Unit = {},
    onPetitionClick: () -> Unit = {},
    onMapClick: (Long) -> Unit = {},
    onCommentSubmit: (String) -> Unit = {},
    onCommentUpdate: (Long, String) -> Unit = { _, _ -> },
    onCommunityLikeClick: () -> Unit = {},
    onCommunityDeclareClick: (Int) -> Unit = {},
) {
    var showDeclarationDialog by remember { mutableStateOf(false) }
    var editingComment by remember { mutableStateOf<CommunityComment?>(null) }

    if (showDeclarationDialog) {
        CommunityDeclarationDialog(
            isSubmitting = uiState.isCommunityDeclarationSubmitting,
            onDismiss = { showDeclarationDialog = false },
            onReasonClick = { reasonIndex ->
                showDeclarationDialog = false
                onCommunityDeclareClick(reasonIndex)
            },
        )
    }

    Scaffold(
        topBar = {
            CommunityDetailTopBar(
                detail = uiState.detail,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            if (uiState.detail != null) {
                CommunityDetailBottomBar(
                    isSubmitting = uiState.isCommentSubmitting,
                    editingComment = editingComment,
                    onEditComplete = { editingComment = null },
                    onCommentSubmit = onCommentSubmit,
                    onCommentUpdate = onCommentUpdate,
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandColor)
                    }
                }
                uiState.errorMessage != null -> {
                    CommunityDetailErrorState(
                        message = uiState.errorMessage,
                        onRetry = onRetry
                    )
                }
                uiState.detail == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "게시글 정보를 찾을 수 없습니다.",
                            style = IssueTypo.Regular15.copy(color = Gray_6)
                        )
                    }
                }
                else -> {
                CommunityDetailBody(
                    detail = uiState.detail,
                    onMapClick = onMapClick,
                    onReportClick = { showDeclarationDialog = true },
                    onGoNowClick = onGoNowClick,
                    onPetitionClick = onPetitionClick,
                        comments = uiState.comments,
                        isCommentLoading = uiState.isCommentLoading,
                        isCommunityLikeSubmitting = uiState.isCommunityLikeSubmitting,
                        onCommunityLikeClick = onCommunityLikeClick,
                        onCommentEditClick = { editingComment = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityDetailErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.communicate),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Gray_3
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = IssueTypo.Regular15.copy(color = Gray_6),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = BrandColor),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "다시 시도", style = IssueTypo.Bold12.copy(color = White))
        }
    }
}

@Composable
private fun CommunityDetailBody(
    detail: CommunityDetail,
    onMapClick: (Long) -> Unit,
    onReportClick: () -> Unit,
    onGoNowClick: () -> Unit,
    onPetitionClick: () -> Unit,
    comments: List<CommunityComment>,
    isCommentLoading: Boolean,
    isCommunityLikeSubmitting: Boolean,
    onCommunityLikeClick: () -> Unit,
    onCommentEditClick: (CommunityComment) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. 카테고리 / 신고 / 위치 Row
        item {
            CommunityDetailCategoryRow(
                detail = detail,
                onMapClick = onMapClick,
                onReportClick = onReportClick,
            )
        }

        // 2. 작성자 및 메타 정보 (ISSUE, COMMUNICATION 등)
        if (detail.writerNickname != null) {
            item {
                CommunityDetailMetaSection(detail = detail)
            }
        }

        // 3. 제목 및 주소
        item {
            CommunityDetailTitleSection(
                detail = detail,
                isLikeSubmitting = isCommunityLikeSubmitting,
                onLikeClick = onCommunityLikeClick,
            )
        }

        // 4. 이미지 섹션
        if (detail.imageUrls.isNotEmpty()) {
            item {
                CommunityDetailImageSection(imageUrls = detail.imageUrls)
            }
        }

        // 5. 본문 내용
        item {
            CommunityDetailContentSection(content = detail.content)
        }

        // 6. 반응 영역 (ISSUE, COMMUNICATION 등)
        if (detail.kind == CommunityItemKind.ISSUE || detail.kind == CommunityItemKind.COMMUNICATION) {
            item {
                ReactionPlaceholderSection()
            }
        }

        // 7. 청원 영역 (ISSUE 전용)
        if (detail.kind == CommunityItemKind.ISSUE) {
            item {
                PetitionPlaceholderSection(detail = detail)
            }
            item {
                CommunityDetailIssueActionSection(
                    detail = detail,
                    onGoNowClick = onGoNowClick,
                    onPetitionClick = onPetitionClick,
                )
            }
        }

        // 8. 댓글 영역
        item {
            CommunityCommentSection(
                comments = comments,
                isLoading = isCommentLoading,
                onCommentEditClick = onCommentEditClick,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityDetailTopBar(
    detail: CommunityDetail?,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
        },
        actions = {
            if (detail != null && detail.isMine) {
                if (detail.kind == CommunityItemKind.ISSUE || detail.kind == CommunityItemKind.COMMUNICATION) {
                    CommunityDetailOwnerActionMenu(detail = detail)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Composable
private fun CommunityDetailOwnerActionMenu(detail: CommunityDetail) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "더보기",
                tint = Gray_6
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(White)
        ) {
            if (detail.kind == CommunityItemKind.COMMUNICATION) {
                DropdownMenuItem(
                    text = { Text("삭제", style = IssueTypo.Regular15) },
                    onClick = {
                        showMenu = false
                        // TODO: 삭제 API 연결 필요
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("글 내리기", style = IssueTypo.Regular15) },
                onClick = {
                    showMenu = false
                    // TODO: 글 내리기 API 연결 필요
                }
            )
        }
    }
}

@Composable
private fun CommunityDetailCategoryRow(
    detail: CommunityDetail,
    onMapClick: (Long) -> Unit,
    onReportClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Chip
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Orange.copy(alpha = 0.1f)
        ) {
            Text(
                text = "#${getKindDisplayName(detail.kind)}",
                style = IssueTypo.Bold12,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = Orange
            )
        }

        // Report Button (if not mine)
        if (!detail.isMine) {
            Spacer(modifier = Modifier.width(8.dp))
            CommunityDetailOutlinedIconButton(
                onClick = {
                    if (!detail.isReported) {
                        onReportClick()
                    }
                },
                enabled = !detail.isReported
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_report),
                    contentDescription = if (detail.isReported) "신고됨" else "신고",
                    modifier = Modifier.size(24.dp),
                    tint = if (detail.isReported) Gray_5 else Orange
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (detail.canOpenMap) {
            CommunityDetailOutlinedIconButton(
                onClick = {
                    detail.pinId?.let(onMapClick)
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "지도보기",
                    tint = BrandColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private val CommunityDetail.canOpenMap: Boolean
    get() = pinId != null && kind in mapVisibleKinds

private val mapVisibleKinds = setOf(
    CommunityItemKind.ISSUE,
    CommunityItemKind.COMMUNICATION,
    CommunityItemKind.FESTIVAL,
    CommunityItemKind.STORE,
)

@Composable
private fun CommunityDetailOutlinedIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(COMMUNITY_DETAIL_ACTION_TOUCH_SIZE)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(COMMUNITY_DETAIL_ACTION_BUTTON_SIZE)
                .border(
                    width = 0.8.dp,
                    color = if (enabled) Gray_3 else Gray_2,
                    shape = CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

private val CommunityDeclarationReasons = listOf(
    "거짓 정보를 포함한 글이에요",
    "욕설 또는 비하 표현이 포함된 글이에요",
    "스팸 또는 도배성 글이에요",
    "불쾌감을 주는 글이에요",
    "종교 포교 목적의 글이에요",
)

@Composable
private fun CommunityDeclarationDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onReasonClick: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        title = {
            Text(
                text = "게시글 신고",
                style = IssueTypo.Bold18.copy(color = Title)
            )
        },
        text = {
            Column {
                CommunityDeclarationReasons.forEachIndexed { index, reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isSubmitting) {
                                onReasonClick(index + 1)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reason,
                            style = IssueTypo.Regular15.copy(color = Title),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting,
            ) {
                Text(text = "취소", style = IssueTypo.Regular15.copy(color = Gray_6))
            }
        },
        containerColor = White,
    )
}

@Composable
private fun CommunityDetailMetaSection(detail: CommunityDetail) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WriterAvatar(imageUrl = detail.writerProfileUrl, size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = detail.writerNickname ?: "익명",
                    style = IssueTypo.Bold18.copy(color = Title, fontSize = 16.sp)
                )
                Text(
                    text = "${formatTimestamp(detail.createdAt)} · 조회 ${detail.viewCount} · 공감 ${detail.likeCount}",
                    style = IssueTypo.Regular15.copy(color = Gray_6, fontSize = 14.sp)
                )
            }
        }

        if (detail.kind == CommunityItemKind.ISSUE && detail.issueStatusText != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Orange
            ) {
                Text(
                    text = detail.issueStatusText,
                    style = IssueTypo.Bold12.copy(color = White, fontSize = 13.sp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun CommunityDetailTitleSection(
    detail: CommunityDetail,
    isLikeSubmitting: Boolean,
    onLikeClick: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = detail.title,
                style = IssueTypo.Bold18.copy(color = Title, fontSize = 22.sp),
                modifier = Modifier.weight(1f)
            )
            CommunityDetailTitleActions(
                detail = detail,
                isLikeSubmitting = isLikeSubmitting,
                onLikeClick = onLikeClick,
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (detail.address != null) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = BrandColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = detail.address,
                        style = IssueTypo.Regular12.copy(color = Gray_6),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (detail.kind == CommunityItemKind.ISSUE) {
                IssueReliabilityInline(
                    score = detail.reliabilityScore,
                    reason = detail.reliabilityReason,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        // 가게/행사 전용 정보
        if ((detail.kind == CommunityItemKind.STORE || detail.kind == CommunityItemKind.FESTIVAL) && detail.discount != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Orange.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "🎁 ${detail.discount}",
                    style = IssueTypo.Bold12.copy(color = Orange),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CommunityDetailTitleActions(
    detail: CommunityDetail,
    isLikeSubmitting: Boolean,
    onLikeClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CompactSympathyButton(
            sympathyCount = detail.likeCount,
            isSympathizedByMe = detail.isLikedByMe,
            onClick = onLikeClick,
            enabled = !detail.isLikedByMe && !isLikeSubmitting,
            height = 28.dp,
            horizontalPadding = 10.dp,
            iconSize = 15.dp,
        )

        if (detail.isMine) {
            CompactCircleIconButton(
                imageVector = Icons.Filled.Edit,
                contentDescription = "수정",
                onClick = {},
            )
            CompactCircleIconButton(
                imageVector = Icons.Filled.Delete,
                contentDescription = "삭제",
                onClick = {},
            )
        }
    }
}

@Composable
private fun CompactCircleIconButton(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(White)
            .border(1.dp, Gray_3, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = Gray_5,
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun IssueReliabilityInline(
    score: Int?,
    reason: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(100.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI 신뢰도",
                style = IssueTypo.Bold12.copy(color = Title, fontSize = 10.sp)
            )
            Text(
                text = if (score != null) "$score%" else "검사중",
                style = IssueTypo.Bold12.copy(
                    color = if (score != null) getReliabilityColor(score) else Gray_5,
                    fontSize = 10.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Gray_2)
        ) {
            if (score != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(score / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(getReliabilityColor(score))
                )
            }

            // 33%, 66% dividers
            Row(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.weight(0.33f))
                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(White.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.weight(0.33f))
                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(White.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.weight(0.34f))
            }
        }

        if (score != null &&
            score < RELIABILITY_REASON_VISIBLE_THRESHOLD &&
            !reason.isNullOrBlank()
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reason,
                style = IssueTypo.Regular12.copy(color = Gray_6, fontSize = 10.sp),
                textAlign = TextAlign.End,
                lineHeight = 14.sp
            )
        }
    }
}

private fun getReliabilityColor(score: Int): Color {
    val percentage = score.coerceIn(0, 100)
    return when {
        percentage <= 33 -> Orange
        percentage <= 66 -> Festival // TODO: AI 신뢰도 중간 구간 색상 theme 토큰 확정 필요
        else -> BrandColor
    }
}

@Composable
private fun CommunityDetailImageSection(imageUrls: List<String>) {
    val listState = rememberLazyListState()
    
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(imageUrls) { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "게시글 이미지",
                    modifier = Modifier
                        .width(110.dp)
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gray_3),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        if (imageUrls.size > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Gray_3)
            ) {
                // TODO: 실제 스크롤 위치에 연동된 인디케이터 구현 필요
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .fillMaxHeight()
                        .background(BrandColor)
                )
            }
        }
    }
}

@Composable
private fun CommunityDetailContentSection(content: String) {
    Text(
        text = content,
        style = IssueTypo.Regular15.copy(color = com.issueissyu.fe.ui.theme.Text),
        lineHeight = 24.sp,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

@Composable
private fun ReactionPlaceholderSection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(text = "반응", style = IssueTypo.Bold12.copy(color = Gray_6))
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("👍", "😮", "🔥", "📍").forEach { emoji ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gray_3),
                    color = White
                ) {
                    Text(
                        text = "$emoji 0",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = IssueTypo.Regular12.copy(color = Title)
                    )
                }
            }
        }
    }
}

@Composable
private fun PetitionPlaceholderSection(detail: CommunityDetail) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Gray_1
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "현재 청원 진행 중",
                style = IssueTypo.Bold12.copy(color = Orange)
            )
            Spacer(modifier = Modifier.height(12.dp))
            val progress = if (detail.petitionTargetCount != null && detail.petitionTargetCount > 0) {
                (detail.petitionCount / detail.petitionTargetCount.toFloat()).coerceIn(0f, 1f)
            } else 0f
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Orange,
                trackColor = Gray_3
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "참여 ${detail.petitionCount}명",
                    style = IssueTypo.Bold12.copy(color = Title)
                )
                if (detail.petitionTargetCount != null) {
                    Text(
                        text = "목표 ${detail.petitionTargetCount}명",
                        style = IssueTypo.Regular12.copy(color = Gray_6)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityDetailIssueActionSection(
    detail: CommunityDetail,
    onGoNowClick: () -> Unit,
    onPetitionClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GoNowButton(
            state = if (detail.isProblemSolver) ActionState.MOVING else ActionState.DEFAULT,
            onClick = onGoNowClick,
            modifier = Modifier.weight(1f)
        )
        SignButton(
            isSigned = detail.isPetitionedByMe,
            count = detail.petitionCount,
            onClick = onPetitionClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CommunityCommentSection(
    comments: List<CommunityComment>,
    isLoading: Boolean,
    onCommentEditClick: (CommunityComment) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray_1)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(text = "댓글 ${comments.size}", style = IssueTypo.Bold18.copy(color = Title))
        Spacer(modifier = Modifier.height(24.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandColor, modifier = Modifier.size(28.dp))
                }
            }
            comments.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.communicate),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Gray_3
                    )
                    Text(
                        text = "첫 번째 댓글을 남겨보세요.",
                        style = IssueTypo.Regular12.copy(color = Gray_5),
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    comments.forEach { comment ->
                        CommunityCommentItem(
                            comment = comment,
                            onEditClick = { onCommentEditClick(comment) },
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun CommunityCommentItem(
    comment: CommunityComment,
    onEditClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        WriterAvatar(imageUrl = comment.profileImageUrl, size = 36.dp)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.nickname,
                    style = IssueTypo.Bold12.copy(color = Title)
                )
                Text(
                    text = formatTimestamp(comment.createdAt),
                    style = IssueTypo.Regular12.copy(color = Gray_5)
                )
                if (comment.isEdited) {
                    Text(
                        text = "수정됨",
                        style = IssueTypo.Regular12.copy(color = Gray_5)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 2.dp,
                        topEnd = 16.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 16.dp
                    ),
                    color = BrandColor,
                ) {
                    Text(
                        text = comment.content,
                        style = IssueTypo.Regular15.copy(color = White),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
                if (comment.isMine) {
                    CompactCircleIconButton(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "댓글 수정",
                        onClick = onEditClick,
                    )
                    CompactCircleIconButton(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "댓글 삭제",
                        onClick = {},
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityDetailBottomBar(
    isSubmitting: Boolean,
    editingComment: CommunityComment?,
    onEditComplete: () -> Unit,
    onCommentSubmit: (String) -> Unit,
    onCommentUpdate: (Long, String) -> Unit,
) {
    var commentText by remember { mutableStateOf("") }
    val canSubmit = commentText.isNotBlank() && !isSubmitting
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    val bottomPadding = if (isKeyboardVisible) 36.dp else 16.dp

    LaunchedEffect(editingComment?.commentId) {
        commentText = editingComment?.content.orEmpty()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 0.dp,
        color = White
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = bottomPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(White)
                        .border(1.dp, BrandColor, RoundedCornerShape(24.dp))
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        enabled = !isSubmitting,
                        singleLine = true,
                        textStyle = IssueTypo.Regular15.copy(color = Title),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (commentText.isBlank()) {
                                Text(
                                    text = if (editingComment == null) {
                                        "댓글을 입력해주세요..."
                                    } else {
                                        "댓글을 수정해주세요..."
                                    },
                                    style = IssueTypo.Regular15.copy(color = Gray_5)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrandColor)
                        .clickable(enabled = canSubmit) {
                            val targetComment = editingComment
                            if (targetComment == null) {
                                onCommentSubmit(commentText)
                            } else {
                                onCommentUpdate(targetComment.commentId, commentText)
                                onEditComplete()
                            }
                            commentText = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "전송",
                            tint = White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WriterAvatar(
    imageUrl: String?,
    size: Dp = 36.dp
) {
    val avatarModifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(Gray_3)

    if (imageUrl.isNullOrBlank()) {
        Box(modifier = avatarModifier)
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = "작성자 프로필",
            modifier = avatarModifier,
            contentScale = ContentScale.Crop
        )
    }
}

private fun getKindDisplayName(kind: CommunityItemKind): String = when (kind) {
    CommunityItemKind.ISSUE -> "이슈"
    CommunityItemKind.COMMUNICATION -> "소통"
    CommunityItemKind.STORE -> "가게홍보"
    CommunityItemKind.FESTIVAL -> "축제행사"
    CommunityItemKind.POLICY -> "정책"
    CommunityItemKind.CONTEST -> "공모전"
    CommunityItemKind.CARDNEWS -> "카드뉴스"
    CommunityItemKind.UNKNOWN -> "기타"
}

private fun formatTimestamp(raw: String?): String {
    if (raw == null) return ""
    val pattern = DateTimeFormatter.ofPattern("MM.dd HH:mm")
    return runCatching {
        OffsetDateTime.parse(raw).format(pattern)
    }.getOrElse {
        runCatching {
            Instant.parse(raw)
                .atZone(ZoneId.systemDefault())
                .format(pattern)
        }.getOrElse {
            runCatching {
                LocalDateTime.parse(raw).format(pattern)
            }.getOrDefault(raw)
        }
    }
}

@Preview(name = "ISSUE 상세 (비작성자)", showBackground = true, heightDp = 1200)
@Composable
fun PreviewCommunityDetailScreenIssue() {
    val dummyDetail = CommunityDetail(
        communityId = 1L, pinId = 10L, kind = CommunityItemKind.ISSUE,
        title = "우리 동네 새로운 공원 조성 소식",
        content = "마포구 성산동 부근에 새로운 공원이 조성될 예정입니다. 주민 여러분의 많은 관심 부탁드립니다.\n\n공원에는 산책로, 운동 기구, 그리고 아이들을 위한 놀이터가 포함될 예정입니다.",
        imageUrls = listOf(
            "https://picsum.photos/400/400?random=1",
            "https://picsum.photos/400/400?random=2",
            "https://picsum.photos/400/400?random=3"
        ),
        writerNickname = "이슈알리미", writerProfileUrl = null, address = "서울시 마포구 성산동 123-45",
        viewCount = 1234, likeCount = 56, createdAt = "2026-05-16T12:00:00.000Z", updatedAt = null,
        isReported = false, isPetitioned = false, isProblemSolver = false, isMine = false,
        reliabilityScore = 33, reliabilityReason = null,
        issueStatusText = "진행중",
        petitionCount = 450, petitionTargetCount = 1000
    )
    CommunityDetailScreenContent(uiState = CommunityDetailUiState(detail = dummyDetail), onBackClick = {})
}

@Preview(name = "ISSUE 상세 (본인)", showBackground = true, heightDp = 1200)
@Composable
fun PreviewCommunityDetailScreenIssueMine() {
    val dummyDetail = CommunityDetail(
        communityId = 2L, pinId = 10L, kind = CommunityItemKind.ISSUE,
        title = "내가 올린 우리 동네 이슈",
        content = "본인이 작성한 이슈 게시글의 관리 메뉴를 확인하기 위한 프리뷰입니다.",
        imageUrls = emptyList(),
        writerNickname = "나", writerProfileUrl = null, address = "서울시 마포구 성산동",
        viewCount = 10, likeCount = 2, createdAt = "2026-05-16T12:00:00.000Z", updatedAt = null,
        isReported = false, isPetitioned = false, isProblemSolver = false, isMine = true,
        reliabilityScore = 90, reliabilityReason = null,
        issueStatusText = "해결 완료"
    )
    CommunityDetailScreenContent(uiState = CommunityDetailUiState(detail = dummyDetail), onBackClick = {})
}

@Preview(name = "ISSUE 상세 (신고됨)", showBackground = true, heightDp = 1200)
@Composable
fun PreviewCommunityDetailScreenIssueReported() {
    val dummyDetail = CommunityDetail(
        communityId = 3L, pinId = 10L, kind = CommunityItemKind.ISSUE,
        title = "이미 신고 처리된 이슈",
        content = "신고 버튼이 비활성화된 상태를 확인하기 위한 프리뷰입니다.",
        imageUrls = emptyList(),
        writerNickname = "누군가", writerProfileUrl = null, address = "서울시 마포구",
        viewCount = 100, likeCount = 5, createdAt = "2026-05-16T12:00:00.000Z", updatedAt = null,
        isReported = true, isPetitioned = false, isProblemSolver = false, isMine = false,
        reliabilityScore = 75, reliabilityReason = null,
        issueStatusText = "진행중"
    )
    CommunityDetailScreenContent(uiState = CommunityDetailUiState(detail = dummyDetail), onBackClick = {})
}

@Preview(name = "ISSUE 상세 (신뢰도 검사중)", showBackground = true, heightDp = 1200)
@Composable
fun PreviewCommunityDetailScreenIssueNullReliability() {
    val dummyDetail = CommunityDetail(
        communityId = 4L, pinId = 10L, kind = CommunityItemKind.ISSUE,
        title = "AI 신뢰도 검사 중인 이슈",
        content = "AI가 아직 신뢰도를 분석 중인 상태의 이슈 게시글입니다.",
        imageUrls = emptyList(),
        writerNickname = "이슈알리미", writerProfileUrl = null, address = "서울시 마포구",
        viewCount = 100, likeCount = 10, createdAt = "2026-05-16T12:00:00.000Z", updatedAt = null,
        isReported = false, isPetitioned = false, isProblemSolver = false, isMine = false,
        reliabilityScore = null, reliabilityReason = null,
        issueStatusText = "진행중"
    )
    CommunityDetailScreenContent(uiState = CommunityDetailUiState(detail = dummyDetail), onBackClick = {})
}

@Preview(name = "ISSUE 상세 (신뢰도 낮음)", showBackground = true, heightDp = 1200)
@Composable
fun PreviewCommunityDetailScreenIssueLowReliability() {
    val dummyDetail = CommunityDetail(
        communityId = 5L, pinId = 10L, kind = CommunityItemKind.ISSUE,
        title = "신뢰도 낮은 이슈 게시글",
        content = "이 게시글은 AI 신뢰도가 낮게 측정되어 사유가 표시되는 예시입니다.",
        imageUrls = emptyList(),
        writerNickname = "정보제보자", writerProfileUrl = null, address = "서울시 서대문구",
        viewCount = 50, likeCount = 5, createdAt = "2026-05-16T12:00:00.000Z", updatedAt = null,
        isReported = false, isPetitioned = false, isProblemSolver = false, isMine = false,
        reliabilityScore = 20, reliabilityReason = "출처가 불분명하며 허위 정보일 가능성이 있습니다.",
        issueStatusText = "해결 전"
    )
    CommunityDetailScreenContent(uiState = CommunityDetailUiState(detail = dummyDetail), onBackClick = {})
}

@Preview(name = "COMMUNICATION 상세 (본인)", showBackground = true, heightDp = 1000)
@Composable
fun PreviewCommunityDetailScreenCommunicationMine() {
    val dummyDetail = CommunityDetail(
        communityId = 6L, pinId = 50L, kind = CommunityItemKind.COMMUNICATION,
        title = "성산동 근처 맛있는 카페 추천해주세요!",
        content = "이사 온 지 얼마 안 돼서 동네를 잘 몰라요. 조용하고 커피 맛있는 카페 있으면 추천 부탁드립니다!",
        imageUrls = emptyList(),
        writerNickname = "동네뉴비", writerProfileUrl = null, address = "서울시 마포구 성산동",
        viewCount = 120, likeCount = 15, createdAt = "2026-05-16T10:00:00.000Z", updatedAt = null,
        isReported = false, isPetitioned = false, isProblemSolver = false, isMine = true,
        reliabilityScore = null, reliabilityReason = null
    )
    CommunityDetailScreenContent(uiState = CommunityDetailUiState(detail = dummyDetail), onBackClick = {})
}

@Preview(name = "STORE 상세", showBackground = true, heightDp = 1000)
@Composable
fun PreviewCommunityDetailScreenStore() {
    val dummyDetail = CommunityDetail(
        communityId = 7L, pinId = 20L, kind = CommunityItemKind.STORE,
        title = "맛있는 빵집 오픈 1주년 이벤트!",
        content = "오픈 1주년을 맞아 전 품목 할인 행사를 진행합니다. 맛있는 빵 드시러 오세요!",
        imageUrls = listOf("https://picsum.photos/400/400?random=4"),
        writerNickname = null, writerProfileUrl = null, address = "서울시 마포구 망원동",
        viewCount = 256, likeCount = 89, createdAt = "2026-05-15T10:00:00.000Z", updatedAt = null,
        isReported = false, isPetitioned = false, isProblemSolver = false, isMine = false,
        reliabilityScore = null, reliabilityReason = null, discount = "전 품목 20% 할인"
    )
    CommunityDetailScreenContent(uiState = CommunityDetailUiState(detail = dummyDetail), onBackClick = {})
}

@Preview(name = "FESTIVAL 상세", showBackground = true, heightDp = 1000)
@Composable
fun PreviewCommunityDetailScreenFestival() {
    val dummyDetail = CommunityDetail(
        communityId = 8L, pinId = 60L, kind = CommunityItemKind.FESTIVAL,
        title = "2026 마포구 봄꽃 축제 안내",
        content = "경의선 숲길에서 펼쳐지는 봄꽃의 향연! 다양한 공연과 먹거리가 준비되어 있습니다. 가족, 친구들과 함께 오셔서 즐거운 시간 보내세요.",
        imageUrls = listOf("https://picsum.photos/400/400?random=5", "https://picsum.photos/400/400?random=6"),
        writerNickname = null, writerProfileUrl = null, address = "서울시 마포구 연남동",
        viewCount = 3500, likeCount = 120, createdAt = "2026-05-14T09:00:00.000Z", updatedAt = null,
        isReported = false, isPetitioned = false, isProblemSolver = false, isMine = false,
        reliabilityScore = null, reliabilityReason = null,
        eventStartTime = "2026-05-14T00:00:00.000Z", eventEndTime = "2026-05-20T00:00:00.000Z"
    )
    CommunityDetailScreenContent(uiState = CommunityDetailUiState(detail = dummyDetail), onBackClick = {})
}

@Preview(name = "Loading Preview", showBackground = true)
@Composable
fun PreviewCommunityDetailScreenLoading() {
    CommunityDetailScreenContent(uiState = CommunityDetailUiState(isLoading = true), onBackClick = {})
}

@Preview(name = "Error Preview", showBackground = true)
@Composable
fun PreviewCommunityDetailScreenError() {
    CommunityDetailScreenContent(
        uiState = CommunityDetailUiState(errorMessage = "게시글을 불러오지 못했습니다.\n잠시 후 다시 시도해주세요."),
        onBackClick = {}
    )
}
