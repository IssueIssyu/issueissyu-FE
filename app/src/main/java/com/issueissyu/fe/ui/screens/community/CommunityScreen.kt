package com.issueissyu.fe.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.issueissyu.fe.domain.model.LocationRegionGroup
import com.issueissyu.fe.domain.model.LocationRegionItem
import com.issueissyu.fe.domain.model.community.CommunityFeedItem
import com.issueissyu.fe.domain.model.community.CommunityItemKind
import com.issueissyu.fe.domain.model.community.CommunityTab
import com.issueissyu.fe.ui.components.CategoryButtons
import com.issueissyu.fe.ui.components.CategoryItem
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.Gray_1
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_6
import com.issueissyu.fe.ui.theme.Gray_8
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.Lime
import com.issueissyu.fe.ui.theme.Shop
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.suiteFontFamily
import androidx.compose.material3.TextButton

@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel = hiltViewModel(),
    onBackClick: (() -> Unit)? = null,
    onCommunityClick: (communityId: Long, detailKind: String?) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()

    CommunityScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onCommunityClick = onCommunityClick,
        onCategorySelected = viewModel::onCategorySelected,
        onRefresh = viewModel::onRefresh,
        onRegionSelected = viewModel::onRegionSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreenContent(
    uiState: CommunityUiState,
    onBackClick: (() -> Unit)? = null,
    onCommunityClick: (communityId: Long, detailKind: String?) -> Unit = { _, _ -> },
    onCategorySelected: (CommunityTab) -> Unit = {},
    onRefresh: () -> Unit = {},
    onRegionSelected: (LocationRegionItem) -> Unit = {}
) {
    var showRegionSelector by remember { mutableStateOf(false) }
    val dismissRegionSelector = { showRegionSelector = false }

    val colorScheme = MaterialTheme.colorScheme

    val categories = remember(colorScheme) {
        CommunityTab.visibleTabs.map { tab ->
            when (tab) {
                CommunityTab.HOT -> CategoryItem(
                    tab.displayName,
                    R.drawable.ic_fire,
                    Issue,
                    colorScheme.errorContainer
                )

                CommunityTab.ISSUE -> CategoryItem(
                    tab.displayName,
                    R.drawable.issue,
                    Issue,
                    colorScheme.errorContainer
                )

                CommunityTab.STORE -> CategoryItem(
                    tab.displayName,
                    R.drawable.shop,
                    Shop,
                    colorScheme.secondaryContainer
                )

                CommunityTab.FESTIVAL -> CategoryItem(
                    tab.displayName,
                    R.drawable.festival,
                    Festival,
                    colorScheme.tertiaryContainer
                )

                CommunityTab.POLICY -> CategoryItem(
                    tab.displayName,
                    R.drawable.ic_policy,
                    Gray_5,
                    colorScheme.primaryContainer
                )

                CommunityTab.CONTEST -> CategoryItem(
                    tab.displayName,
                    R.drawable.ic_award,
                    Shop,
                    colorScheme.primaryContainer
                )

                CommunityTab.CARDNEWS -> CategoryItem(
                    tab.displayName,
                    R.drawable.ic_cardnews,
                    Lime,
                    colorScheme.secondaryContainer
                )

                CommunityTab.ALL -> CategoryItem(
                    tab.displayName,
                    R.drawable.ic_all,
                    Title,
                    colorScheme.surfaceVariant,
                    selectedIconColor = Color.White
                )

                else -> CategoryItem(
                    tab.displayName,
                    R.drawable.communicate,
                    Communication,
                    colorScheme.outline
                )
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                CategoryButtons(
                    categories = categories,
                    selectedCategory = uiState.selectedCategory.displayName,
                    onCategorySelected = { name ->
                        onCategorySelected(name.toCommunityTab())
                    }
                )

                val showsRegionSelector = uiState.selectedCategory.showsRegionSelector()

                if (onBackClick != null || showsRegionSelector) {
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

                    if (showsRegionSelector) {
                        RegionDropdownPill(
                            region = uiState.region.toRegionDisplayName(),
                            onClick = { showRegionSelector = true }
                        )
                    }
                }
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
                            Text(text = "다시 시도")
                        }
                    }
                }

                uiState.isCurrentCategoryEmpty() -> {
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
                    if (uiState.selectedCategory == CommunityTab.CARDNEWS) {
                        CardNewsFeedGrid(
                            items = uiState.feedItems,
                            onItemClick = { onCommunityClick(it.communityId, "CARDNEWS") },
                        )
                    } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (uiState.selectedCategory == CommunityTab.ALL) {
                            // 대표 카드 영역 (자체적으로 둥근 모서리를 가짐)
                            uiState.storePromotions.firstOrNull()?.let { storePromotion ->
                                item {
                                    RepresentativeFeedCard(
                                        item = storePromotion,
                                        onClick = { onCommunityClick(storePromotion.communityId, null) },
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }

                            // 우리 동네 인기 소식 섹션 (컨테이너를 둥글게 처리)
                            if (uiState.hotPreviews.isNotEmpty()) {
                                item {
                                    SectionHeaderWithAction(
                                        title = "우리 동네 인기 소식 🔥",
                                        actionText = "더보기",
                                        onActionClick = { onCategorySelected(CommunityTab.HOT) }
                                    )
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                    ) {
                                        uiState.hotPreviews.take(3).forEachIndexed { index, item ->
                                            CommunityFeedCard(
                                                item = item,
                                                onClick = {
                                                    onCommunityClick(item.communityId, null)
                                                }
                                            )

                                            if (index < uiState.hotPreviews.take(3).lastIndex) {
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

                            item {
                                SectionHeader(title = "우리 동네 최근 소식 🆕")

                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White)
                                ) {
                                    uiState.recentNews.forEachIndexed { index, item ->
                                        CommunityFeedCard(
                                            item = item,
                                            onClick = {
                                                onCommunityClick(item.communityId, null)
                                            }
                                        )

                                        if (index < uiState.recentNews.lastIndex) {
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
                            itemsIndexed(uiState.feedItems) { index, item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                ) {
                                    CommunityFeedCard(
                                        item = item,
                                        onClick = { onCommunityClick(item.communityId, null) }
                                    )
                                    if (index < uiState.feedItems.lastIndex) {
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
    }

    if (showRegionSelector) {
        RegionSelectorSheet(
            currentRegion = uiState.region,
            currentLocationId = uiState.locationId,
            regionGroups = uiState.regionGroups,
            isRegionLoading = uiState.isRegionLoading,
            regionError = uiState.regionError,
            onDismissRequest = dismissRegionSelector,
            onRegionSelected = { region ->
                onRegionSelected(region)
                dismissRegionSelector()
            }
        )
    }
}

private fun CommunityUiState.isCurrentCategoryEmpty(): Boolean {
    return if (selectedCategory == CommunityTab.ALL) {
        storePromotions.isEmpty() && hotPreviews.isEmpty() && recentNews.isEmpty()
    } else {
        feedItems.isEmpty()
    }
}

private fun String?.toCommunityTab(): CommunityTab {
    return CommunityTab.entries.find { it.displayName == this } ?: CommunityTab.ALL
}

private fun CommunityTab.showsRegionSelector(): Boolean {
    return this !in setOf(
        CommunityTab.POLICY,
        CommunityTab.CONTEST,
        CommunityTab.CARDNEWS,
    )
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
                text = region.ifBlank { "지역 선택" },
                style = TextStyle(
                    fontFamily = suiteFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
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
    currentLocationId: Long?,
    regionGroups: List<LocationRegionGroup>,
    isRegionLoading: Boolean,
    regionError: String?,
    onDismissRequest: () -> Unit,
    onRegionSelected: (LocationRegionItem) -> Unit
) {
    val initialProvince = regionGroups.find { group ->
        group.subLocations.any { it.locationId == currentLocationId || it.location == currentRegion }
    }?.superLocation ?: regionGroups.firstOrNull()?.superLocation.orEmpty()

    var selectedProvince by remember(currentRegion, regionGroups) { mutableStateOf(initialProvince) }
    val selectedGroup = regionGroups.find { it.superLocation == selectedProvince }
    var draftRegion by remember(currentLocationId, currentRegion, regionGroups) {
        mutableStateOf(
            selectedGroup?.subLocations?.firstOrNull {
                it.locationId == currentLocationId || it.location == currentRegion
            }
        )
    }

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
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기"
                    )
                }
            }

            when {
                isRegionLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }

                regionGroups.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = regionError ?: "선택 가능한 지역이 없습니다.",
                            style = TextStyle(
                                fontFamily = suiteFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        RegionProvinceList(
                            provinces = regionGroups.map { it.superLocation },
                            selectedProvince = selectedProvince,
                            onProvinceSelected = { province ->
                                selectedProvince = province
                                draftRegion = regionGroups
                                    .find { it.superLocation == province }
                                    ?.subLocations
                                    ?.firstOrNull()
                            },
                            modifier = Modifier.width(120.dp)
                        )

                        RegionDistrictList(
                            districts = selectedGroup?.subLocations.orEmpty(),
                            selectedLocationId = draftRegion?.locationId,
                            onDistrictSelected = { draftRegion = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            SelectedRegionFooter(
                selectedRegion = draftRegion?.location.orEmpty(),
                onApply = {
                    draftRegion?.let {
                        onRegionSelected(it)
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
    districts: List<LocationRegionItem>,
    selectedLocationId: Long?,
    onDistrictSelected: (LocationRegionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.White)
            .padding(horizontal = 12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        items(districts) { district ->
            val isSelected = district.locationId == selectedLocationId

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) {
                            BrandColor.copy(alpha = 0.08f)
                        } else {
                            Color.Transparent
                        }
                    )
                    .clickable { onDistrictSelected(district) }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = district.location.toRegionDisplayName(),
                    style = TextStyle(
                        fontFamily = suiteFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp,
                        color = if (isSelected) BrandColor else Gray_8
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "선택됨",
                        tint = BrandColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
fun SelectedRegionFooter(
    selectedRegion: String,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
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
            enabled = selectedRegion.isNotBlank()
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

private val previewRegionGroups = listOf(
    LocationRegionGroup(
        superLocation = "서울특별시",
        subLocations = listOf(
            LocationRegionItem(locationId = 1L, location = "서울특별시 강남구"),
            LocationRegionItem(locationId = 2L, location = "서울특별시 마포구"),
            LocationRegionItem(locationId = 3L, location = "서울특별시 서대문구"),
            LocationRegionItem(locationId = 4L, location = "서울특별시 종로구")
        )
    ),
    LocationRegionGroup(
        superLocation = "경기도",
        subLocations = listOf(
            LocationRegionItem(locationId = 5L, location = "경기도 고양시"),
            LocationRegionItem(locationId = 6L, location = "경기도 성남시")
        )
    )
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

@Composable
fun SectionHeaderWithAction(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 24.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = suiteFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onActionClick) {
            Text(
                text = actionText,
                style = TextStyle(
                    fontFamily = suiteFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = BrandColor
                )
            )
        }
    }
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

private fun previewCommunityItems(): List<CommunityFeedItem> {
    return listOf(
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
}

@Preview(name = "커뮤니티 홈", showBackground = true)
@Composable
fun PreviewCommunityScreenHome() {
    CommunityScreenContent(
        uiState = CommunityUiState(
            storePromotions = previewCommunityItems().filter { it.kind == CommunityItemKind.STORE },
            hotPreviews = previewCommunityItems().filter { it.isHot },
            recentNews = previewCommunityItems(),
            selectedCategory = CommunityTab.ALL,
            region = "마포구"
        ),
        onBackClick = {},
        onRegionSelected = {}
    )
}

@Preview(name = "커뮤니티 HOT 탭", showBackground = true)
@Composable
fun PreviewCommunityScreenHot() {
    CommunityScreenContent(
        uiState = CommunityUiState(
            feedItems = previewCommunityItems(),
            selectedCategory = CommunityTab.HOT,
            region = "마포구"
        ),
        onBackClick = {},
        onRegionSelected = {}
    )
}

@Preview(name = "커뮤니티 ISSUE 탭", showBackground = true)
@Composable
fun PreviewCommunityScreenIssue() {
    CommunityScreenContent(
        uiState = CommunityUiState(
            feedItems = previewCommunityItems().filter { it.kind == CommunityItemKind.ISSUE },
            selectedCategory = CommunityTab.ISSUE,
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
            selectedCategory = CommunityTab.ALL,
            region = "서대문구"
        ),
        onBackClick = {},
        onCommunityClick = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityScreenLoading() {
    CommunityScreenContent(
        uiState = CommunityUiState(
            isLoading = true,
            feedItems = emptyList(),
            selectedCategory = CommunityTab.ALL,
            region = "마포구"
        ),
        onBackClick = {},
        onCommunityClick = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityScreenError() {
    CommunityScreenContent(
        uiState = CommunityUiState(
            error = "서버 연결에 실패했습니다. 네트워크 상태를 확인해주세요.",
            feedItems = emptyList(),
            selectedCategory = CommunityTab.ALL,
            region = "마포구"
        ),
        onBackClick = {},
        onCommunityClick = { _, _ -> },
        onRegionSelected = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewRegionSelectorSheet() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.32f)
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
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기"
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    RegionProvinceList(
                        provinces = previewRegionGroups.map { it.superLocation },
                        selectedProvince = "서울특별시",
                        onProvinceSelected = {},
                        modifier = Modifier.width(120.dp)
                    )

                    RegionDistrictList(
                        districts = previewRegionGroups.first().subLocations,
                        selectedLocationId = 2L,
                        onDistrictSelected = {},
                        modifier = Modifier.weight(1f)
                    )
                }

                SelectedRegionFooter(
                    selectedRegion = "서울특별시 마포구",
                    onApply = {}
                )
            }
        }
    }
}
