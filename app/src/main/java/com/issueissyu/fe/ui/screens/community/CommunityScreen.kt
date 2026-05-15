package com.issueissyu.fe.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle // Added import
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.issueissyu.fe.R
import com.issueissyu.fe.domain.model.community.CommunityFeedItem
import com.issueissyu.fe.domain.model.community.CommunityItemKind
import com.issueissyu.fe.domain.model.community.CommunityTab
import com.issueissyu.fe.ui.components.CategoryButtons
import com.issueissyu.fe.ui.components.CategoryItem
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.Lime
import com.issueissyu.fe.ui.theme.Shop
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.suiteFontFamily // Added import

@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel = hiltViewModel(),
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    
    CommunityScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onTabSelected = viewModel::onTabSelected,
        onRefresh = viewModel::onRefresh
    )
}

@Composable
fun CommunityScreenContent(
    uiState: CommunityUiState,
    onBackClick: (() -> Unit)? = null,
    onTabSelected: (CommunityTab) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    var showRegionDropdown by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    val categories = CommunityTab.visibleTabs.map { tab ->
            when (tab) {
                CommunityTab.HOT -> CategoryItem(tab.displayName, R.drawable.ic_fire, Issue, colorScheme.errorContainer)
                CommunityTab.ISSUE -> CategoryItem(tab.displayName, R.drawable.issue, Issue, colorScheme.errorContainer)
                CommunityTab.STORE -> CategoryItem(tab.displayName, R.drawable.shop, Shop, colorScheme.secondaryContainer)
                CommunityTab.FESTIVAL -> CategoryItem(tab.displayName, R.drawable.festival, Festival, colorScheme.tertiaryContainer)
                CommunityTab.POLICY -> CategoryItem(tab.displayName, R.drawable.ic_policy, Gray_5, colorScheme.primaryContainer)
                CommunityTab.CONTEST -> CategoryItem(tab.displayName, R.drawable.ic_award, Shop, colorScheme.primaryContainer)
                CommunityTab.CARDNEWS -> CategoryItem(tab.displayName, R.drawable.ic_cardnews, Lime, colorScheme.secondaryContainer)
                CommunityTab.ALL -> CategoryItem(tab.displayName, R.drawable.ic_all, Title, colorScheme.surfaceVariant)
                else -> CategoryItem(tab.displayName, R.drawable.communicate, Communication, colorScheme.outline)
            }
        }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                // 1. 카테고리 버튼 (제일 위)
                CategoryButtons(
                    categories = categories,
                    selectedCategory = uiState.selectedTab.displayName,
                    onCategorySelected = { name ->
                        onTabSelected(name.toCommunityTab())
                    }
                )

                // 2. 상단바 Row (뒤로가기 + 지역 선택)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로가기",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    RegionDropdownPill(
                        region = uiState.region,
                        expanded = showRegionDropdown,
                        onClick = { showRegionDropdown = true },
                        onDismissRequest = { showRegionDropdown = false }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Color(0xFFF8F8F8) -> MaterialTheme.colorScheme.background
        ) {
            when {
                uiState.isLoading && !uiState.isRefreshing -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.error,
                            style = TextStyle(
                                fontFamily = suiteFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant // Color(0xFF757575) -> MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onRefresh,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("다시 시도")
                        }
                    }
                }
                uiState.feedItems.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "현재 표시할 소식이 없습니다.\n다른 지역을 선택하거나 나중에 다시 확인해주세요.",
                            style = TextStyle(
                                fontFamily = suiteFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.outline, // Color(0xFF9E9E9E) -> MaterialTheme.colorScheme.outline
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        )
                    }
                }
                else -> {
                    val isSectionedTab = uiState.selectedTab == CommunityTab.HOT || uiState.selectedTab == CommunityTab.ALL
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        if (isSectionedTab) {
                            // 대표 카드 영역
                            item {
                                RepresentativeFeedCard(
                                    item = uiState.feedItems.first(),
                                    onClick = { /* TODO: 상세 이동 */ },
                                    modifier = Modifier.padding(16.dp)
                                )
                            }

                            // 우리 동네 인기 소식 섹션
                            val hotItems = uiState.feedItems.filter { it.isHot }
                            if (hotItems.isNotEmpty()) {
                                item {
                                    SectionHeader(title = "우리 동네 인기 소식 🔥")
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                    ) {
                                        hotItems.take(3).forEachIndexed { index, item ->
                                            CommunityFeedCard(
                                                item = item,
                                                onClick = { /* TODO: 상세 이동 */ }
                                            )
                                            if (index < hotItems.take(3).size - 1) {
                                                HorizontalDivider(
                                                    modifier = Modifier.padding(horizontal = 16.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant, // Color(0xFFF5F5F5) -> MaterialTheme.colorScheme.surfaceVariant
                                                    thickness = 1.dp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 우리 동네 최근 소식 섹션
                            item {
                                SectionHeader(title = "우리 동네 최근 소식 🆕")
                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White)
                                ) {
                                    uiState.feedItems.forEachIndexed { index, item ->
                                        CommunityFeedCard(
                                            item = item,
                                            onClick = { /* TODO: 상세 이동 */ }
                                        )
                                        if (index < uiState.feedItems.size - 1) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant, // Color(0xFFF5F5F5) -> MaterialTheme.colorScheme.surfaceVariant
                                                thickness = 1.dp
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // 일반 카테고리 탭: 리스트만 표시
                            itemsIndexed(uiState.feedItems) { index, item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White) // 카드 느낌을 주기 위해 흰색 배경
                                ) {
                                    CommunityFeedCard(
                                        item = item,
                                        onClick = { /* TODO: 상세 이동 */ },
                                        // 카드 내부 padding은 CommunityFeedCard 내부에서 처리되므로 여기서는 제거
                                    )
                                    if (index < uiState.feedItems.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant, // Color(0xFFF5F5F5) -> MaterialTheme.colorScheme.surfaceVariant
                                            thickness = 1.dp
                                        )
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

private fun String?.toCommunityTab(): CommunityTab {
    return when (this) {
        "HOT" -> CommunityTab.HOT
        "이슈" -> CommunityTab.ISSUE
        "가게 홍보" -> CommunityTab.STORE
        "축제·행사" -> CommunityTab.FESTIVAL
        "정책" -> CommunityTab.POLICY
        "공모전" -> CommunityTab.CONTEST
        "카드뉴스" -> CommunityTab.CARDNEWS
        "전체", null -> CommunityTab.ALL
        else -> CommunityTab.ALL
    }
}

@Composable
fun RegionDropdownPill(
    region: String,
    expanded: Boolean,
    onClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant, // Color(0xFFF5F5F5) -> MaterialTheme.colorScheme.surfaceVariant
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = region,
                style = TextStyle(
                    fontFamily = suiteFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                DropdownMenuItem(
                    text = { Text("마포구") },
                    onClick = onDismissRequest
                )
                DropdownMenuItem(
                    text = { Text("서대문구") },
                    onClick = onDismissRequest
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontFamily = suiteFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground // Color(0xFF212121) -> MaterialTheme.colorScheme.onBackground
        ),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewRegionDropdownPill() {
    Box(modifier = Modifier.padding(16.dp)) {
        RegionDropdownPill(
            region = "마포구",
            expanded = false,
            onClick = {},
            onDismissRequest = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityScreen() {
    val dummyItems = listOf(
        CommunityFeedItem(
            communityId = 1L,
            pinId = null,
            kind = CommunityItemKind.ISSUE,
            title = "[이슈] 우리 동네 새로운 공원 조성 소식",
            content = "마포구 성산동 부근에 새로운 공원이 조성될 예정입니다. 주민 여러분의 많은 관심 부탁드립니다.",
            thumbnailUrl = "https://picsum.photos/400/300?random=1",
            writerNickname = "이슈알리미",
            writerProfileUrl = null,
            address = "서울시 마포구 성산동",
            viewCount = 123,
            likeCount = 45,
            eventStartTime = null,
            eventEndTime = null,
            discount = null,
            isHot = true
        ),
        CommunityFeedItem(
            communityId = 2L,
            pinId = null,
            kind = CommunityItemKind.STORE,
            title = "[할인] 맛있는 빵집 오픈 1주년 이벤트!",
            content = "오픈 1주년을 맞아 전 품목 할인 행사를 진행합니다. 맛있는 빵 드시러 오세요!",
            thumbnailUrl = "https://picsum.photos/400/300?random=2",
            writerNickname = null,
            writerProfileUrl = null,
            address = "서울시 마포구 망원동",
            viewCount = 256,
            likeCount = 89,
            eventStartTime = "2026-05-14T00:00:00.000Z",
            eventEndTime = "2026-05-20T00:00:00.000Z",
            discount = "30% 할인",
            isHot = false
        ),
        CommunityFeedItem(
            communityId = 3L,
            pinId = null,
            kind = CommunityItemKind.FESTIVAL,
            title = "[축제] 2026 마포구 봄꽃 축제",
            content = "경의선 숲길에서 펼쳐지는 봄꽃의 향연! 다양한 공연과 먹거리가 준비되어 있습니다.",
            thumbnailUrl = "https://picsum.photos/400/300?random=3",
            writerNickname = null,
            writerProfileUrl = null,
            address = "서울시 마포구 연남동",
            viewCount = 512,
            likeCount = 128,
            eventStartTime = "2026-05-15T10:00:00.000Z",
            eventEndTime = "2026-05-17T20:00:00.000Z",
            discount = null,
            isHot = true
        )
    )
    
    CommunityScreenContent(
        uiState = CommunityUiState(
            feedItems = dummyItems,
            selectedTab = CommunityTab.HOT,
            region = "마포구"
        ),
        onBackClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityScreenEmpty() {
    CommunityScreenContent(
        uiState = CommunityUiState(
            feedItems = emptyList(),
            selectedTab = CommunityTab.ALL,
            region = "서대문구"
        ),
        onBackClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityScreenLoading() {
    CommunityScreenContent(
        uiState = CommunityUiState(
            isLoading = true,
            feedItems = emptyList(),
            selectedTab = CommunityTab.ALL,
            region = "마포구"
        ),
        onBackClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityScreenError() {
    CommunityScreenContent(
        uiState = CommunityUiState(
            error = "서버 연결에 실패했습니다. 네트워크 상태를 확인해주세요.",
            feedItems = emptyList(),
            selectedTab = CommunityTab.ALL,
            region = "마포구"
        ),
        onBackClick = {}
    )
}
