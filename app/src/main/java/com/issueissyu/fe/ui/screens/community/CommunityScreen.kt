package com.issueissyu.fe.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.hilt.navigation.compose.hiltViewModel
import com.issueissyu.fe.domain.model.community.CommunityTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRegionDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showRegionDropdown = true }
                    ) {
                        Text(
                            text = uiState.region,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        
                        DropdownMenu(
                            expanded = showRegionDropdown,
                            onDismissRequest = { showRegionDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("마포구") },
                                onClick = { 
                                    // TODO: 지역 변경 로직
                                    showRegionDropdown = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("서대문구") },
                                onClick = { showRegionDropdown = false }
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: 검색 */ }) {
                        Icon(Icons.Default.Search, contentDescription = "검색")
                    }
                    IconButton(onClick = { /* TODO: 알림 */ }) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = "알림")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
        ) {
            CommunityTabs(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::onTabSelected
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // 대표 카드 영역
                if (uiState.feedItems.isNotEmpty()) {
                    item {
                        RepresentativeFeedCard(
                            item = uiState.feedItems.first(),
                            onClick = { /* TODO: 상세 이동 */ },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // 우리 동네 인기 소식 섹션
                val hotItems = uiState.feedItems.filter { it.isHot }
                if (hotItems.isNotEmpty()) {
                    item {
                        SectionHeader(title = "우리 동네 인기 소식 🔥")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(hotItems) { item ->
                                CommunityFeedCard(
                                    item = item,
                                    onClick = { /* TODO: 상세 이동 */ },
                                    modifier = Modifier.width(280.dp)
                                )
                            }
                        }
                    }
                }

                // 우리 동네 최근 소식 섹션
                item {
                    SectionHeader(title = "우리 동네 최근 소식 🆕")
                }

                items(uiState.feedItems) { item ->
                    CommunityFeedCard(
                        item = item,
                        onClick = { /* TODO: 상세 이동 */ },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CommunityTabs(
    selectedTab: CommunityTab,
    onTabSelected: (CommunityTab) -> Unit
) {
    val visibleTabs = CommunityTab.visibleTabs
    val selectedIndex = visibleTabs.indexOf(selectedTab).coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 16.dp,
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {},
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        visibleTabs.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}
