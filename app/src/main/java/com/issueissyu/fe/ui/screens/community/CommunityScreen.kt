package com.issueissyu.fe.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.Shop

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

    val categories = remember {
        CommunityTab.visibleTabs.map { tab ->
            when (tab) {
                CommunityTab.HOT -> CategoryItem(tab.displayName, R.drawable.issue, Issue, Color(0xFFFFEBEE))
                CommunityTab.ISSUE -> CategoryItem(tab.displayName, R.drawable.issue, Issue, Color(0xFFFFEBEE))
                CommunityTab.STORE -> CategoryItem(tab.displayName, R.drawable.shop, Shop, Color(0xFFE8F5E9))
                CommunityTab.FESTIVAL -> CategoryItem(tab.displayName, R.drawable.festival, Festival, Color(0xFFFFF3E0))
                CommunityTab.POLICY -> CategoryItem(tab.displayName, R.drawable.communicate, Communication, Color(0xFFE3F2FD)) // TODO: 전용 아이콘 교체
                CommunityTab.CONTEST -> CategoryItem(tab.displayName, R.drawable.communicate, Communication, Color(0xFFF3E5F5)) // TODO: 전용 아이콘 교체
                CommunityTab.CARDNEWS -> CategoryItem(tab.displayName, R.drawable.communicate, Communication, Color(0xFFE0F2F1)) // TODO: 전용 아이콘 교체
                CommunityTab.ALL -> CategoryItem(tab.displayName, R.drawable.communicate, Communication, Color(0xFFF5F5F5)) // TODO: 전용 아이콘 교체
                else -> CategoryItem(tab.displayName, R.drawable.communicate, Communication, Color.LightGray)
            }
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
                .background(Color(0xFFF8F8F8))
        ) {
            when {
                uiState.isLoading && !uiState.isRefreshing -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRefresh) {
                            Text("다시 시도")
                        }
                    }
                }
                uiState.feedItems.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "표시할 소식이 없습니다.",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
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
                                                    color = Color(0xFFF5F5F5)
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
                                                color = Color(0xFFF5F5F5)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // 일반 카테고리 탭: 리스트만 표시
                            items(uiState.feedItems) { item ->
                                Card(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    CommunityFeedCard(
                                        item = item,
                                        onClick = { /* TODO: 상세 이동 */ }
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
        color = Color(0xFFF5F5F5),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = region,
                style = MaterialTheme.typography.titleMedium.copy(
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
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF212121)
        ),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityScreen() {
    val dummyItems = listOf(
        CommunityFeedItem(
            communityId = 1L,
            pinId = null,
            kind = CommunityItemKind.ISSUE,
            title = "우리 동네 새로운 이슈",
            content = "내용입니다.",
            thumbnailUrl = "https://picsum.photos/200/200",
            writerNickname = "작성자",
            writerProfileUrl = null,
            address = "마포구",
            viewCount = 10,
            likeCount = 5,
            eventStartTime = null,
            eventEndTime = null,
            discount = null,
            isHot = true
        ),
        CommunityFeedItem(
            communityId = 2L,
            pinId = null,
            kind = CommunityItemKind.STORE,
            title = "가게 홍보",
            content = "할인 중!",
            thumbnailUrl = null,
            writerNickname = null,
            writerProfileUrl = null,
            address = "서대문구",
            viewCount = 20,
            likeCount = 10,
            eventStartTime = null,
            eventEndTime = null,
            discount = "10%",
            isHot = false
        )
    )
    
    CommunityScreenContent(
        uiState = CommunityUiState(
            feedItems = dummyItems,
            selectedTab = CommunityTab.HOT
        ),
        onBackClick = {}
    )
}
