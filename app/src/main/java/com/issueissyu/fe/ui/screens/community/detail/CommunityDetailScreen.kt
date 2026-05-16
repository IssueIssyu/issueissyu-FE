package com.issueissyu.fe.ui.screens.community.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.issueissyu.fe.domain.model.community.CommunityDetail
import com.issueissyu.fe.domain.model.community.CommunityItemKind
import com.issueissyu.fe.ui.theme.IssueTypo

@Composable
fun CommunityDetailScreen(
    viewModel: CommunityDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    CommunityDetailScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = viewModel::loadDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailScreenContent(
    uiState: CommunityDetailUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "상세 보기", style = IssueTypo.Bold18) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: 더보기 메뉴 */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "더보기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            // TODO: 댓글 입력창 또는 하단 액션바
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Placeholder for comment input
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "댓글을 입력해주세요...",
                            style = IssueTypo.Regular15,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "등록",
                        style = IssueTypo.Bold12,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = uiState.errorMessage, style = IssueTypo.Regular15)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Text(text = "다시 시도")
                        }
                    }
                }
                uiState.detail != null -> {
                    val detail = uiState.detail
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. 이미지 목록
                        if (detail.imageUrls.isNotEmpty()) {
                            item {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(detail.imageUrls) { imageUrl ->
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .aspectRatio(4f / 3f)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // 2. 작성자 정보 및 메타데이터
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // 작성자 프로필
                                    if (detail.writerNickname != null) {
                                        AsyncImage(
                                            model = detail.writerProfileUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = detail.writerNickname, style = IssueTypo.Bold12)
                                            Text(
                                                text = "${detail.createdAt ?: ""} · 조회 ${detail.viewCount}",
                                                style = IssueTypo.Regular12,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    } else {
                                        // 작성자 정보가 없는 경우 (가게 홍보 등)
                                        Text(
                                            text = "${detail.kind.name} · ${detail.createdAt ?: ""} · 조회 ${detail.viewCount}",
                                            style = IssueTypo.Regular12,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    // 신고 버튼 placeholder
                                    Text(
                                        text = "신고",
                                        style = IssueTypo.Regular12,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // 제목
                                Text(text = detail.title, style = IssueTypo.Bold18)
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // 주소
                                if (detail.address != null) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = "📍 ${detail.address}",
                                            style = IssueTypo.Regular12,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // 본문
                                Text(
                                    text = detail.content,
                                    style = IssueTypo.Regular16,
                                    lineHeight = 24.sp
                                )
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                // 이모지/공감 영역 placeholder
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        color = Color.White,
                                        onClick = { /* TODO: 공감 클릭 */ }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "👍", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "공감 ${detail.likeCount}", style = IssueTypo.Bold12)
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(40.dp))
                                
                                // 댓글 영역 placeholder
                                Text(text = "댓글 0", style = IssueTypo.Bold18)
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "첫 번째 댓글을 남겨보세요.",
                                        style = IssueTypo.Regular15,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(80.dp))
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
fun PreviewCommunityDetailScreenIssue() {
    val dummyDetail = CommunityDetail(
        communityId = 1L,
        pinId = 10L,
        kind = CommunityItemKind.ISSUE,
        title = "우리 동네 새로운 공원 조성 소식",
        content = "마포구 성산동 부근에 새로운 공원이 조성될 예정입니다. 주민 여러분의 많은 관심 부탁드립니다.\n\n공원에는 산책로, 운동 기구, 그리고 아이들을 위한 놀이터가 포함될 예정입니다.",
        imageUrls = listOf(
            "https://picsum.photos/800/600?random=1",
            "https://picsum.photos/800/600?random=2"
        ),
        writerNickname = "이슈알리미",
        writerProfileUrl = "https://picsum.photos/200/200?random=1",
        address = "서울시 마포구 성산동",
        viewCount = 123,
        likeCount = 45,
        createdAt = "2026-05-16T12:00:00.000Z",
        updatedAt = null,
        isReported = false,
        isPetitioned = false,
        isProblemSolver = false,
        isMine = false
    )
    CommunityDetailScreenContent(
        uiState = CommunityDetailUiState(detail = dummyDetail),
        onBackClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityDetailScreenStore() {
    val dummyDetail = CommunityDetail(
        communityId = 2L,
        pinId = 20L,
        kind = CommunityItemKind.STORE,
        title = "[할인] 맛있는 빵집 오픈 1주년 이벤트!",
        content = "오픈 1주년을 맞아 전 품목 할인 행사를 진행합니다. 맛있는 빵 드시러 오세요!\n\n인기 메뉴인 소금빵과 크루아상은 조기 품절될 수 있으니 서둘러 방문해주세요.",
        imageUrls = listOf("https://picsum.photos/800/600?random=3"),
        writerNickname = null,
        writerProfileUrl = null,
        address = "서울시 마포구 망원동",
        viewCount = 256,
        likeCount = 89,
        createdAt = "2026-05-15T10:00:00.000Z",
        updatedAt = null,
        isReported = false,
        isPetitioned = false,
        isProblemSolver = false,
        isMine = true
    )
    CommunityDetailScreenContent(
        uiState = CommunityDetailUiState(detail = dummyDetail),
        onBackClick = {}
    )
}
