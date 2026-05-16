package com.issueissyu.fe.ui.screens.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
import com.issueissyu.fe.ui.theme.*

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
        onRefresh = viewModel::onRefresh,
        onRegionSelected = viewModel::onRegionSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreenContent(
    uiState: CommunityUiState,
    onBackClick: (() -> Unit)? = null,
    onTabSelected: (CommunityTab) -> Unit = {},
    onRefresh: () -> Unit = {},
    onRegionSelected: (String) -> Unit = {}
) {
    var showRegionSelector by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    val categories = remember(colorScheme) {
        CommunityTab.visibleTabs.map { tab ->
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
                        onClick = { showRegionSelector = true }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                color = MaterialTheme.colorScheme.outline,
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
                            // 대표 카드 영역 (자체적으로 둥근 모서리를 가짐)
                            item {
                                RepresentativeFeedCard(
                                    item = uiState.feedItems.first(),
                                    onClick = { /* TODO: 상세 이동 */ },
                                    modifier = Modifier.padding(16.dp)
                                )
                            }

                            // 우리 동네 인기 소식 섹션 (컨테이너를 둥글게 처리)
                            val hotItems = uiState.feedItems.filter { it.isHot }
                            if (hotItems.isNotEmpty()) {
                                item {
                                    SectionHeader(title = "우리 동네 인기 소식 🔥")
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .clip(RoundedCornerShape(16.dp)) // 컨테이너 둥근 모서리
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
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    thickness = 1.dp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 우리 동네 최근 소식 섹션 (컨테이너를 둥글게 처리)
                            item {
                                SectionHeader(title = "우리 동네 최근 소식 🆕")
                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .clip(RoundedCornerShape(16.dp)) // 컨테이너 둥근 모서리
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
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                thickness = 1.dp
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // 일반 카테고리 탭: 리스트만 표시 (피그마 기준 리스트형)
                            itemsIndexed(uiState.feedItems) { index, item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                ) {
                                    CommunityFeedCard(
                                        item = item,
                                        onClick = { /* TODO: 상세 이동 */ }
                                    )
                                    if (index < uiState.feedItems.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
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

    if (showRegionSelector) {
        RegionSelectorSheet(
            currentRegion = uiState.region,
            onDismissRequest = { showRegionSelector = false },
            onRegionSelected = { region ->
                onRegionSelected(region)
                showRegionSelector = false
            }
        )
    }
}

private fun String?.toCommunityTab(): CommunityTab {
    return CommunityTab.entries.find { it.displayName == this } ?: CommunityTab.ALL
}

@Composable
fun RegionDropdownPill(
    region: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectorSheet(
    currentRegion: String,
    onDismissRequest: () -> Unit,
    onRegionSelected: (String) -> Unit
) {
    var selectedProvince by remember { mutableStateOf("서울") }
    var draftRegion by remember { mutableStateOf(currentRegion) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "지역 선택",
                    style = TextStyle(
                        fontFamily = suiteFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Title
                    )
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "닫기")
                }
            }

            // Body
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Left: Provinces
                RegionProvinceList(
                    provinces = dummyProvinces,
                    selectedProvince = selectedProvince,
                    onProvinceSelected = { selectedProvince = it },
                    modifier = Modifier.width(120.dp)
                )

                // Right: Districts
                RegionDistrictList(
                    districts = dummyDistrictsMap[selectedProvince] ?: emptyList(),
                    selectedDistrict = draftRegion,
                    onDistrictSelected = { draftRegion = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // Footer
            SelectedRegionFooter(
                selectedRegion = draftRegion,
                onRemove = { draftRegion = "" },
                onApply = { 
                    if (draftRegion.isNotEmpty()) {
                        onRegionSelected(draftRegion)
                    }
                }
            )
        }
    }
}

@Composable
fun RegionProvinceList(
    provinces: List<String>,
    selectedProvince: String,
    onProvinceSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .background(Gray_1)
    ) {
        items(provinces) { province ->
            val isSelected = province == selectedProvince
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProvinceSelected(province) }
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = province,
                    style = TextStyle(
                        fontFamily = suiteFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp,
                        color = if (isSelected) BrandColor else Gray_6
                    )
                )
            }
        }
    }
}

@Composable
fun RegionDistrictList(
    districts: List<String>,
    selectedDistrict: String,
    onDistrictSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.White)
    ) {
        items(districts) { district ->
            val isSelected = district == selectedDistrict
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDistrictSelected(district) }
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = district,
                    style = TextStyle(
                        fontFamily = suiteFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp,
                        color = if (isSelected) BrandColor else Gray_8
                    )
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "선택됨",
                        tint = BrandColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedRegionFooter(
    selectedRegion: String,
    onRemove: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "선택한 곳 ${if (selectedRegion.isEmpty()) 0 else 1}/10",
            style = TextStyle(
                fontFamily = suiteFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = Gray_6
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (selectedRegion.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BrandColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, BrandColor),
                modifier = Modifier.height(36.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = selectedRegion,
                        style = TextStyle(
                            fontFamily = suiteFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = BrandColor
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "삭제",
                        tint = BrandColor,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onRemove() }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandColor,
                contentColor = Color.White
            ),
            enabled = selectedRegion.isNotEmpty()
        ) {
            Text(
                text = "적용하기",
                style = TextStyle(
                    fontFamily = suiteFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }
    }
}

private val dummyProvinces = listOf("서울", "경기", "인천", "강원", "대전", "세종", "충남", "충북", "부산", "울산", "경남", "경북")
private val dummyDistrictsMap = mapOf(
    "서울" to listOf("전체", "강남구", "강동구", "강북구", "강서구", "관악구", "광진구", "구로구", "금천구", "노원구", "도봉구", "동대문구", "동작구", "마포구", "서대문구", "서초구", "성동구", "성북구", "송파구", "양천구", "영등포구", "용산구", "은평구", "종로구", "중구", "중랑구"),
    "경기" to listOf("전체", "수원시", "고양시", "용인시", "성남시", "부천시", "화성시", "안산시", "남양주시", "안양시", "평택시"),
    "인천" to listOf("전체", "중구", "동구", "미추홀구", "연수구", "남동구", "부평구", "계양구", "서구"),
    "강원" to listOf("전체", "춘천시", "원주시", "강릉시", "동해시", "태백시", "속초시", "삼척시")
)

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontFamily = suiteFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
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
            onClick = {}
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
        onBackClick = {},
        onRegionSelected = {}
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
        onBackClick = {},
        onRegionSelected = {}
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
        onBackClick = {},
        onRegionSelected = {}
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
        onBackClick = {},
        onRegionSelected = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PreviewRegionSelectorSheet() {
    // 바텀시트 내부 UI만 확인하기 위해 시트의 컨텐츠 구조를 직접 렌더링
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.32f) // 배경 딤 처리 시뮬레이션
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color.White)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "지역 선택",
                        style = TextStyle(
                            fontFamily = suiteFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Title
                        )
                    )
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                }

                // Body
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    RegionProvinceList(
                        provinces = dummyProvinces,
                        selectedProvince = "서울",
                        onProvinceSelected = {},
                        modifier = Modifier.width(120.dp)
                    )

                    RegionDistrictList(
                        districts = dummyDistrictsMap["서울"] ?: emptyList(),
                        selectedDistrict = "마포구",
                        onDistrictSelected = {},
                        modifier = Modifier.weight(1f)
                    )
                }

                // Footer
                SelectedRegionFooter(
                    selectedRegion = "마포구",
                    onRemove = {},
                    onApply = {}
                )
            }
        }
    }
}
