package com.issueissyu.fe.ui.screens.landing

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.issueissyu.fe.R
import com.issueissyu.fe.domain.model.pin.CommunicationPinDetail
import com.issueissyu.fe.domain.model.pin.FestivalPinDetail
import com.issueissyu.fe.domain.model.pin.IssuePinDetail
import com.issueissyu.fe.domain.model.pin.Pin
import com.issueissyu.fe.domain.model.pin.PinCoordinate
import com.issueissyu.fe.domain.model.pin.PinEmojiReaction
import com.issueissyu.fe.domain.model.pin.PinUser
import com.issueissyu.fe.domain.model.pin.ResolutionStatus
import com.issueissyu.fe.domain.model.pin.ShopPinDetail
import com.issueissyu.fe.ui.screens.map.PinSummaryCard
import com.issueissyu.fe.ui.screens.map.PinSummaryCardHighlight
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.FestivalContainer
import com.issueissyu.fe.ui.theme.Gray_1
import com.issueissyu.fe.ui.theme.Gray_2
import com.issueissyu.fe.ui.theme.Gray_3
import com.issueissyu.fe.ui.theme.Gray_4
import com.issueissyu.fe.ui.theme.Gray_5
import com.issueissyu.fe.ui.theme.Gray_7
import com.issueissyu.fe.ui.theme.Issue
import com.issueissyu.fe.ui.theme.IssueContainer
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.IssueissyuTheme
import com.issueissyu.fe.ui.theme.Shop
import com.issueissyu.fe.ui.theme.ShopContainer
import com.issueissyu.fe.ui.theme.Title
import com.issueissyu.fe.ui.theme.White
import com.issueissyu.fe.ui.theme.chabFontFamily
import com.issueissyu.fe.ui.theme.suiteFontFamily
import kotlinx.coroutines.launch

private const val GUIDE_PAGE_COUNT = 6
private const val LANDING_STAGE_COUNT = 14

@Composable
fun LandingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { LANDING_STAGE_COUNT })
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = false,
        modifier = modifier.fillMaxSize(),
    ) { page ->
        LandingPage(
            page = page,
            onNext = {
                if (page < LANDING_STAGE_COUNT - 1) {
                    coroutineScope.launch {
                        val nextPage = page + 1
                        if (page.toProgressIndex() == nextPage.toProgressIndex()) {
                            pagerState.scrollToPage(nextPage)
                        } else {
                            pagerState.animateScrollToPage(
                                page = nextPage,
                                animationSpec = tween(
                                    durationMillis = 250,
                                    easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f),
                                ),
                            )
                        }
                    }
                }
            },
            onComplete = onComplete,
        )
    }
}

@Composable
private fun LandingPage(
    page: Int,
    onNext: () -> Unit,
    onComplete: () -> Unit,
) {
    val isCompletePage = page == LANDING_STAGE_COUNT - 1
    val isIssueDetailGuidePage = page in 11..12

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(if (isIssueDetailGuidePage) White else Color(0xFFF3F8FF))
            .pointerInput(page, isCompletePage) {
                if (!isCompletePage) {
                    detectTapGestures(onTap = { onNext() })
                }
            },
    ) {
        val deviceViewportWidth =
            LocalConfiguration.current.screenWidthDp.dp.coerceAtMost(maxWidth)

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(deviceViewportWidth)
                .fillMaxHeight()
        ) {
            if (isIssueDetailGuidePage) {
                LandingIssueDetailGuidePage(
                    showActionGuide = page == 12,
                    modifier = Modifier.fillMaxSize(),
                )
                return@Box
            }

            Image(
                painter = painterResource(
                    if (isCompletePage) {
                        R.drawable.img_landing_complete_background
                    } else {
                        R.drawable.img_landing_guide_background
                    }
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(
                        start = 27.dp,
                        top = 58.dp,
                        end = 27.dp,
                        bottom = 20.dp,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LandingProgress(
                    currentPage = page.toProgressIndex(),
                    modifier = Modifier.width(320.dp),
                )

                Spacer(modifier = Modifier.height(95.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    if (isCompletePage) {
                        LandingCompleteContent(
                            onComplete = onComplete,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        LandingGuideContent(
                            page = page,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LandingProgress(
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(GUIDE_PAGE_COUNT) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        color = if (index <= currentPage) BrandColor else Gray_4,
                    )
            )
        }
    }
}

@Composable
private fun LandingGuideContent(
    page: Int,
    modifier: Modifier = Modifier,
) {
    when (page) {
        0 -> LandingIntroPage(modifier)
        1 -> LandingPinTypesPage(selectedType = null, modifier = modifier)
        2 -> LandingPinTypesPage(
            selectedType = LandingPinShowcaseType.ISSUE,
            modifier = modifier,
        )
        3 -> LandingPinTypesPage(
            selectedType = LandingPinShowcaseType.ISSUE,
            guideText = "공감 10개 이상을 받으면\n커뮤니티에 올라가요!",
            guideFocus = LandingPinGuideFocus.REACTIONS,
            modifier = modifier,
        )
        4 -> LandingPinTypesPage(
            selectedType = LandingPinShowcaseType.ISSUE,
            guideText = "핀 내용을 더 자세히 보고 싶다면,\n커뮤니티에서 다양한 의견을 확인해 보세요.",
            guideFocus = LandingPinGuideFocus.COMMUNITY_BUTTON,
            modifier = modifier,
        )
        5 -> LandingPinTypesPage(
            selectedType = LandingPinShowcaseType.FESTIVAL,
            modifier = modifier,
        )
        6 -> LandingPinTypesPage(
            selectedType = LandingPinShowcaseType.SHOP,
            modifier = modifier,
        )
        7 -> LandingPinTypesPage(
            selectedType = LandingPinShowcaseType.COMMUNICATION,
            modifier = modifier,
        )
        8 -> LandingPinCreatePage(modifier)
        9 -> LandingHistoryPage(modifier)
        10 -> LandingCommunityPage(modifier)
        else -> Unit
    }
}

private fun Int.toProgressIndex(): Int {
    return when (this) {
        0 -> 0
        in 1..7 -> 1
        8 -> 2
        9 -> 3
        in 10..12 -> 4
        else -> 5
    }
}

private enum class LandingPinShowcaseType {
    ISSUE,
    FESTIVAL,
    SHOP,
    COMMUNICATION,
}

private enum class LandingPinGuideFocus {
    NONE,
    REACTIONS,
    COMMUNITY_BUTTON,
}

@Composable
private fun LandingIntroPage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(R.drawable.img_logo),
            contentDescription = "이슈있슈",
            modifier = Modifier
                .offset(x = 69.dp, y = (-55).dp)
                .width(220.dp)
                .height(150.dp),
            contentScale = ContentScale.Fit,
        )

        Text(
            text = "우리 동네의\n모든 이슈를 한눈에!\n이슈를 발견하고\n핀을 찍어 공유하고\n해결해 보세요!",
            fontFamily = suiteFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 40.sp,
            color = Gray_7,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .offset(x = 2.dp, y = 101.dp)
                .width(344.dp)
                .height(185.dp),
        )

        Image(
            painter = painterResource(R.drawable.ic_character_default),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 133.dp, y = 311.dp)
                .width(90.dp)
                .height(139.dp),
            contentScale = ContentScale.Fit,
        )

        LandingIntroMapIcon(
            iconRes = R.drawable.shop_landing_outline,
            width = 32.dp,
            height = 29.dp,
            x = 158.dp,
            y = 630.dp,
        )
        LandingIntroMapIcon(
            iconRes = R.drawable.shop_landing_outline,
            width = 32.dp,
            height = 29.dp,
            x = 268.dp,
            y = 514.dp,
        )
        LandingIntroMapIcon(
            iconRes = R.drawable.issue_landing_outline,
            width = 27.dp,
            height = 32.dp,
            x = 255.dp,
            y = 598.dp,
        )
        LandingIntroMapIcon(
            iconRes = R.drawable.issue_landing_outline,
            width = 27.dp,
            height = 32.dp,
            x = 177.dp,
            y = 543.dp,
        )
        LandingIntroMapIcon(
            iconRes = R.drawable.festival_landing_outline,
            width = 30.dp,
            height = 32.dp,
            x = 49.dp,
            y = 614.dp,
        )
        LandingIntroMapIcon(
            iconRes = R.drawable.communicate_landing_outline,
            width = 24.dp,
            height = 32.dp,
            x = 100.dp,
            y = 543.dp,
        )
    }
}

@Composable
private fun LandingIntroMapIcon(
    iconRes: Int,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    x: androidx.compose.ui.unit.Dp,
    y: androidx.compose.ui.unit.Dp,
) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier
            .offset(x = x, y = y)
            .width(width)
            .height(height),
    )
}

@Composable
private fun LandingPinTypesPage(
    selectedType: LandingPinShowcaseType?,
    guideText: String? = null,
    guideFocus: LandingPinGuideFocus = LandingPinGuideFocus.NONE,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LandingTitle(
                title = "핀의 종류",
                subtitle = "다양한 핀으로 정보를 한눈에 구분할 수 있어요!",
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 97.dp)
                .height(423.dp),
            contentAlignment = Alignment.Center,
        ) {
            LandingPinTypeGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .alpha(if (selectedType == null) 1f else 0.38f),
            )

            selectedType?.let { type ->
                val pinGuideText = guideText ?: when (type) {
                    LandingPinShowcaseType.ISSUE ->
                        "이슈 핀에서는 동네에서 발생한 문제를 확인하고\n해결 과정에 함께 참여할 수 있어요."
                    LandingPinShowcaseType.FESTIVAL ->
                        "이벤트 핀에서는 동네에서 열리는\n다양한 축제와 행사를 확인할 수 있어요."
                    LandingPinShowcaseType.SHOP ->
                        "홍보 핀에서는 우리 동네 가게의 행사, 할인,\n신규 소식을 확인할 수 있어요."
                    LandingPinShowcaseType.COMMUNICATION ->
                        "소통 핀에서는 우리 동네 사람들과\n다양한 이야기를 나눌 수 있어요!"
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LandingSpeechBubble(text = pinGuideText)
                    Spacer(modifier = Modifier.height(14.dp))
                    LandingPinShowcaseCard(
                        type = type,
                        guideFocus = guideFocus,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun LandingPinTypeGrid(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LandingPinTypeCard(
                label = "이슈",
                title = "생활 이슈",
                description = "주민들이 제보한\n생활 이슈 확인",
                color = IssueContainer,
                accentColor = Issue,
                iconRes = R.drawable.issue_landing_outline,
                iconSize = 52,
                iconOffsetY = 42,
                titleOffsetY = 109,
                descriptionOffsetY = 138,
                modifier = Modifier.weight(1f),
            )
            LandingPinTypeCard(
                label = "행사",
                title = "축제·행사",
                description = "우리 동네의\n축제 및 행사를 한번에!",
                color = FestivalContainer,
                accentColor = Festival,
                iconRes = R.drawable.festival_landing_outline,
                iconSize = 52,
                iconOffsetY = 39,
                titleOffsetY = 109,
                descriptionOffsetY = 138,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LandingPinTypeCard(
                label = "홍보",
                title = "동네 가게",
                description = "우리 동네 가게의 정보와\n추천을 간편하게!",
                color = ShopContainer,
                accentColor = Shop,
                iconRes = R.drawable.shop_landing_outline,
                iconSize = 60,
                iconOffsetY = 47,
                titleOffsetY = 114,
                descriptionOffsetY = 143,
                modifier = Modifier.weight(1f),
            )
            LandingPinTypeCard(
                label = "소통",
                title = "동네 소통",
                description = "이웃과 의견을\n자유롭게 나누어요!",
                color = Communication,
                accentColor = BrandColor,
                iconRes = R.drawable.communicate_landing_outline,
                labelWidth = 74,
                iconSize = 60,
                iconOffsetY = 47,
                titleOffsetY = 117,
                descriptionOffsetY = 143,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LandingPinTypeCard(
    label: String,
    title: String,
    description: String,
    color: Color,
    accentColor: Color,
    iconRes: Int,
    labelWidth: Int = 47,
    iconSize: Int,
    iconOffsetY: Int,
    titleOffsetY: Int,
    descriptionOffsetY: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(204.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.7f),
                        White.copy(alpha = 0.7f),
                    ),
                ),
            )
            .border(6.dp, White, RoundedCornerShape(15.dp)),
    ) {
        Text(
            text = label,
            fontFamily = suiteFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 21.sp,
            color = White,
            modifier = Modifier
                .offset(x = 15.dp, y = 14.dp)
                .width(labelWidth.dp)
                .height(23.dp)
                .background(accentColor, RoundedCornerShape(14.dp))
                .padding(top = 1.dp),
            textAlign = TextAlign.Center,
        )

        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = iconOffsetY.dp)
                .size(iconSize.dp),
        )

        Text(
            text = title,
            fontFamily = suiteFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 21.sp,
            color = Title,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = titleOffsetY.dp),
        )

        Text(
            text = description,
            fontFamily = suiteFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 21.sp,
            color = Title,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = descriptionOffsetY.dp),
        )
    }
}

@Composable
private fun LandingPinShowcaseCard(
    type: LandingPinShowcaseType,
    guideFocus: LandingPinGuideFocus = LandingPinGuideFocus.NONE,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.height(260.dp),
    ) {
        PinSummaryCard(
            pin = type.toSamplePin(),
            currentUserId = SAMPLE_WRITER_ID,
            onDetailClick = {},
            onCommunityClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onSympathyClick = {},
            onEmojiClick = {},
            modifier = Modifier.fillMaxSize(),
            interactionsEnabled = false,
            highlight = when (guideFocus) {
                LandingPinGuideFocus.REACTIONS -> PinSummaryCardHighlight.SYMPATHY
                LandingPinGuideFocus.COMMUNITY_BUTTON -> PinSummaryCardHighlight.COMMUNITY
                LandingPinGuideFocus.NONE -> PinSummaryCardHighlight.NONE
            },
        )
    }
}

private const val SAMPLE_WRITER_ID = "landing-writer"

private fun LandingPinShowcaseType.toSamplePin(): Pin {
    val writer = PinUser(
        id = SAMPLE_WRITER_ID,
        name = "이웃 A",
        imageUrl = null,
    )
    val commonReactions = listOf(
        PinEmojiReaction(emojiId = "sad", count = 3),
        PinEmojiReaction(emojiId = "angry", count = 1),
        PinEmojiReaction(emojiId = "surprised", count = 1),
    )

    return when (this) {
        LandingPinShowcaseType.ISSUE -> Pin(
            id = "landing-issue",
            title = "공원 조명 고장",
            description = "동네 공원의 조명이 꺼져 있어 저녁 시간에 이용하기 불편하다는 제보가 있어요.",
            coordinate = PinCoordinate(0.0, 0.0),
            address = "예시시 예시구 가상로 12",
            locationName = "가상동 중앙공원",
            sympathyCount = 21,
            isSympathizedByMe = true,
            emojiReactions = commonReactions,
            communityPostId = "landing-community",
            createdAt = "2025-01-01T00:00:00Z",
            detail = IssuePinDetail(
                writer = writer,
                resolutionStatus = ResolutionStatus.RESOLVED,
            ),
        )
        LandingPinShowcaseType.FESTIVAL -> Pin(
            id = "landing-festival",
            title = "우리동네 나눔 행사",
            description = "이웃과 함께 물건을 나누고 새로운 가치를 만드는 가상의 동네 행사예요.",
            coordinate = PinCoordinate(0.0, 0.0),
            address = "예시시 예시구 가상로 20",
            locationName = "가상동 주민광장",
            sympathyCount = 34,
            communityPostId = "landing-festival-community",
            createdAt = "2025-01-01T00:00:00Z",
            detail = FestivalPinDetail(
                keywords = listOf("소소한 행복"),
                startDate = "2025-01-01",
                endDate = "2025-01-01",
            ),
        )
        LandingPinShowcaseType.SHOP -> Pin(
            id = "landing-shop",
            title = "동네카페 새소식",
            description = "가상의 동네카페에서 준비한 신메뉴와 할인 소식을 소개합니다.",
            coordinate = PinCoordinate(0.0, 0.0),
            address = "예시시 예시구 가상로 30",
            locationName = "가상동 동네카페",
            sympathyCount = 6,
            communityPostId = "landing-shop-community",
            createdAt = "2025-01-01T00:00:00Z",
            detail = ShopPinDetail(
                keywords = listOf("분위기", "커피맛집"),
                currentNews = "전 품목 50% 할인",
            ),
        )
        LandingPinShowcaseType.COMMUNICATION -> Pin(
            id = "landing-communication",
            title = "저녁 산책 모임",
            description = "오늘 저녁 가상동 공원에서 함께 산책하며 이야기 나눌 이웃을 찾아요.",
            coordinate = PinCoordinate(0.0, 0.0),
            address = "예시시 예시구 가상로 12",
            locationName = "가상동 중앙공원",
            sympathyCount = 18,
            emojiReactions = commonReactions,
            communityPostId = "landing-communication-community",
            createdAt = "2025-01-01T00:00:00Z",
            detail = CommunicationPinDetail(writer = writer),
        )
    }
}

@Composable
private fun LandingPinCreatePage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LandingTitle(
            title = "핀 생성",
            subtitle = "사진과 간단한 설명만으로도\n누구나 쉽게 등록!",
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "이런 기능도 있어요! 👇",
            style = IssueTypo.Regular16.copy(color = Gray_7),
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(470.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(250.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.Black)
                    .padding(6.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(30.dp))
                        .background(White)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = "‹             이슈 작성",
                        style = IssueTypo.Bold12.copy(color = Title),
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("사진", style = IssueTypo.Bold12.copy(color = Title))
                    Spacer(modifier = Modifier.height(5.dp))
                    Column(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF7F7F7))
                            .border(1.dp, Color(0xFFE2E2E2), RoundedCornerShape(10.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Gray_4,
                            modifier = Modifier.size(21.dp),
                        )
                        Text("사진 추가", style = IssueTypo.Regular12.copy(color = Gray_4))
                    }
                    Spacer(modifier = Modifier.height(9.dp))
                    LandingFormField(
                        label = "등록 장소",
                        placeholder = "예시시 예시구 가상동",
                        height = 40.dp,
                        filled = true,
                    )
                    LandingFormField("제목", "제목을 입력하세요.", height = 40.dp)
                    LandingFormField(
                        "상세 설명",
                        "상세 설명을 작성해 주세요.",
                        height = 62.dp,
                    )
                    Text("말투 설정", style = IssueTypo.Bold12.copy(color = Title))
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf("#기본체", "#친근하게", "#부드럽게").forEach { tone ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(White)
                                    .border(1.dp, Gray_4, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    text = tone,
                                    style = IssueTypo.Regular12.copy(color = Title),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .background(Color(0xFFE8E8E8), RoundedCornerShape(15.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("작성 완료", style = IssueTypo.Bold12.copy(color = Gray_4))
                    }
                }
            }

            LandingStepChip(
                text = "1. 사진 찍고",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 54.dp),
            )
            LandingStepChip(
                text = "2. 한 줄 작성 하고",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(bottom = 28.dp),
            )
            LandingStepChip(
                text = "3. 원하는 말투 선택",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 72.dp),
            )
        }
    }
}

@Composable
private fun LandingFormField(
    label: String,
    placeholder: String,
    height: androidx.compose.ui.unit.Dp = 56.dp,
    filled: Boolean = false,
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(label, style = IssueTypo.Bold12.copy(color = Title))
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(
                    if (filled) Color(0xFFF7F7F7) else White,
                    RoundedCornerShape(10.dp),
                )
                .border(1.dp, Color(0xFFE2E2E2), RoundedCornerShape(10.dp))
                .padding(10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(placeholder, style = IssueTypo.Regular12.copy(color = Gray_4))
        }
    }
}

@Composable
private fun LandingStepChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(White, RoundedCornerShape(14.dp))
            .border(1.dp, Gray_4, RoundedCornerShape(14.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Text(
            text = text,
            style = IssueTypo.Regular16.copy(color = Title),
        )
    }
}

@Composable
private fun LandingHistoryPage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LandingTitle(
            title = "히스토리",
            subtitle = "우리 동네가 변화하고 있는 모습과\n변화 과정을 담은 패치노트를 확인할 수 있어요.",
        )

        Spacer(modifier = Modifier.height(42.dp))

        Column(
            modifier = Modifier
                .width(280.dp)
                .height(410.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.Black)
                .padding(start = 6.dp, top = 6.dp, end = 6.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(White)
                    .padding(18.dp),
            ) {
                Text(
                    text = "‹             동네 패치노트",
                    style = IssueTypo.Bold12.copy(color = Title),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "지난 동네의 히스토리를 볼 수 있어요",
                    style = IssueTypo.Regular12.copy(color = Gray_7),
                )
                Spacer(modifier = Modifier.height(16.dp))
                HistoryItem("공원 조명 점검", 51, "해결 전", White, Color.Black)
                HistoryItem("산책로 보수", 100, "진행 중", IssueContainer, Issue)
                HistoryItem("안내판 교체", 107, "해결 완료", Communication, BrandColor)
            }
        }
    }
}

@Composable
private fun HistoryItem(
    title: String,
    viewCount: Int,
    status: String,
    color: Color,
    statusColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(color, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = IssueTypo.Bold12.copy(color = Title),
                modifier = Modifier.weight(1f),
            )
            Text("조회 $viewCount", style = IssueTypo.Regular12.copy(color = Gray_4))
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Gray_4,
                modifier = Modifier
                    .size(18.dp)
                    .background(Color(0xFFF0F0F0), CircleShape),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("이웃 A", style = IssueTypo.Bold12.copy(color = Gray_7))
        }
        Spacer(modifier = Modifier.height(7.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Gray_7,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "예시시 예시구 가상동",
                    style = IssueTypo.Regular12.copy(color = Gray_7),
                )
            }
            Text(
                text = status,
                style = IssueTypo.Bold12.copy(color = White),
                modifier = Modifier
                    .background(statusColor, RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun LandingCommunityPage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LandingTitle(
            title = "커뮤니티",
            subtitle = "핫이슈, 가게 홍보, 축제·행사부터\n정책, 지원사업, 공모전 정보까지 한눈에 확인해 보세요.",
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(300.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(34.dp))
                    .background(Color.Black)
                    .padding(6.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFFF7F7F7)),
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(White)
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        items(landingCommunityCategories) { category ->
                            Row(
                                modifier = Modifier
                                    .height(32.dp)
                                    .background(
                                        if (category.selected) Color(0xFF333333) else White,
                                        RoundedCornerShape(18.dp),
                                    )
                                    .border(
                                        1.dp,
                                        if (category.selected) Color(0xFF333333) else Color(0xFFE1E1E1),
                                        RoundedCornerShape(18.dp),
                                    )
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(category.iconRes),
                                    contentDescription = null,
                                    tint = if (category.selected) White else category.iconColor,
                                    modifier = Modifier.size(15.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = category.label,
                                    style = IssueTypo.Bold12.copy(
                                        color = if (category.selected) White else Title,
                                    ),
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(White)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            text = "예시시 예시구⌄",
                            style = IssueTypo.Bold12.copy(color = BrandColor),
                            modifier = Modifier
                                .border(1.dp, BrandColor, RoundedCornerShape(14.dp))
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                        )
                    }

                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        CommunityFeedItem(
                            title = "동네식당 새소식",
                            subtitle = "가상 할인 행사 · 기간: 예시일",
                            footer = "조회 100·공감 43",
                            color = ShopContainer,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "우리 동네 인기 소식 🔥",
                            style = IssueTypo.ExtraBold18.copy(color = Title),
                        )
                        Spacer(modifier = Modifier.height(7.dp))
                        CommunityPostPreview()
                    }
                }
            }

            LandingStepChip(
                text = "카테고리 선택으로 원하는 정보만 쏙쏙!",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 36.dp),
            )
            LandingStepChip(
                text = "이웃이 올린 이슈에\n공감하고",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(top = 90.dp),
            )
            LandingStepChip(
                text = "댓글로 의견 남기기!",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 42.dp),
            )
        }
    }
}

private data class LandingCommunityCategory(
    val label: String,
    val iconRes: Int,
    val iconColor: Color,
    val selected: Boolean = false,
)

private val landingCommunityCategories = listOf(
    LandingCommunityCategory("HOT", R.drawable.ic_fire, Issue, selected = true),
    LandingCommunityCategory("이슈", R.drawable.issue, Issue),
    LandingCommunityCategory("소통", R.drawable.communicate, Communication),
    LandingCommunityCategory("가게 홍보", R.drawable.shop, Shop),
    LandingCommunityCategory("축제·행사", R.drawable.festival, Festival),
    LandingCommunityCategory("정책", R.drawable.ic_policy, Gray_4),
    LandingCommunityCategory("공모전", R.drawable.ic_award, Shop),
    LandingCommunityCategory("카드뉴스", R.drawable.ic_cardnews, BrandColor),
    LandingCommunityCategory("전체", R.drawable.ic_all, Title),
)

@Composable
private fun CommunityPostPreview() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .background(White, RoundedCornerShape(12.dp))
            .padding(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .background(IssueContainer, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.issue),
                contentDescription = null,
                tint = Issue,
                modifier = Modifier.size(42.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.fillMaxHeight()) {
            Text("공원 조명 고장", style = IssueTypo.Bold18.copy(color = Title))
            Spacer(modifier = Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_character_default),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("이웃 A", style = IssueTypo.Regular12.copy(color = Gray_7))
            }
            Text(
                "예시시 예시구 가상동",
                style = IssueTypo.Regular12.copy(color = Gray_7),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text("조회 51·공감 38", style = IssueTypo.Regular12.copy(color = Gray_4))
        }
    }
}

@Composable
private fun CommunityFeedItem(
    title: String,
    subtitle: String,
    footer: String,
    color: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .background(White, RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .background(color, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                tint = Shop,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.fillMaxHeight()) {
            Text(title, style = IssueTypo.Bold18.copy(color = Title))
            Spacer(modifier = Modifier.height(6.dp))
            Text(subtitle, style = IssueTypo.Regular12.copy(color = Gray_7))
            Spacer(modifier = Modifier.weight(1f))
            Text(footer, style = IssueTypo.Regular12.copy(color = Gray_4))
        }
    }
}

@Composable
private fun LandingTitle(
    title: String,
    subtitle: String,
) {
    Text(
        text = title,
        fontFamily = suiteFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        color = Title,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(3.dp))
    Text(
        text = subtitle,
        fontFamily = suiteFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 21.sp,
        color = Gray_7,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun LandingSpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(16.dp))
            .border(1.dp, Gray_4, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = IssueTypo.Regular16.copy(
                color = Title,
                lineHeight = 24.sp,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LandingIssueDetailGuidePage(
    showActionGuide: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(White)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "#이슈",
                    style = IssueTypo.Bold18.copy(color = Title),
                    modifier = Modifier
                        .background(Color(0xFFD8E8F8), RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .border(1.dp, Gray_4, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_report),
                        contentDescription = null,
                        tint = Issue,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .border(1.dp, Gray_4, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = BrandColor,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_character_default),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("밤티", style = IssueTypo.Bold18.copy(color = Title))
                    Text(
                        "04.07 18:24 · 조회 51 · 공감 38",
                        style = IssueTypo.Regular12.copy(color = Gray_7),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("쓰레기 무단투기", style = IssueTypo.Bold18.copy(color = Title, fontSize = 22.sp))
            Spacer(modifier = Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("서울 마포구 홍익로 6길 34", style = IssueTypo.Regular15.copy(color = Title))
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .background(Color(0xFFD7D0C7), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Gray_7,
                            modifier = Modifier.size(34.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .background(Gray_3, RoundedCornerShape(4.dp)),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "최근 홍대입구역 근처 골목에서 쓰레기 무단투기가 지속적으로 발생하고 있습니다.\n" +
                    "주변 환경이 훼손되고 악취 및 위생 문제로 인해 주민 불편이 커지고 있는 상황입니다.\n" +
                    "해당 지역에 대한 확인 및 적절한 조치 부탁드립니다.",
                style = IssueTypo.Regular15.copy(
                    color = Title,
                    lineHeight = 22.sp,
                ),
            )

            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(15.dp))
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LandingReactionRow(listOf("❤️ 30", "🥳 10", "😡 3", "👊 1", "👎 1"))
                LandingReactionRow(listOf("😤 1", "😮 1", "🫨 1", "🤫 1", "☺ 1"))
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(White, RoundedCornerShape(15.dp))
                    .padding(7.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LandingIssueActionButton(
                    text = "🔥 지금 가요",
                    backgroundColor = Gray_1,
                    textColor = Issue,
                    modifier = Modifier.weight(1f),
                )
                LandingIssueActionButton(
                    text = "📣 청원 (0)",
                    backgroundColor = Color(0xFFFF6F35),
                    textColor = White,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Gray_1, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(14.dp),
            ) {
                LandingGuideComment("밤티", "저건 좀 아니다,,")
                Spacer(modifier = Modifier.height(10.dp))
                LandingGuideComment("밤티", "저건 좀 아니다,,")
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(Gray_2, RoundedCornerShape(22.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(36.dp)
                            .background(BrandColor, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        "댓글 달기",
                        style = IssueTypo.Regular15.copy(color = Gray_5),
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f)),
        )

        if (showActionGuide) {
            LandingGuideCallout(
                text = "이슈 핀에서는\n‘지금 가요’ 버튼으로\n직접 해결에 참여할 수도 있고,",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 13.dp, end = 74.dp)
                    .padding(bottom = 60.dp),
            )
            LandingGuideCallout(
                text = "‘청원’ 버튼을 눌러\n청원에 동참해 지자체에 목소리를 전달할 수도 있어요.",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(start = 37.dp, end = 16.dp, bottom = 145.dp),
            )
        } else {
            LandingGuideCallout(
                text = "커뮤니티에서는\n이모지와 댓글을 통해\n의견을 공유할 수 있어요.",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp, end = 71.dp, bottom = 110.dp),
            )
        }

        if (!showActionGuide) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 54.dp)
                    .size(42.dp)
                    .background(BrandColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = White,
                )
            }
        }
    }
}

@Composable
private fun LandingReactionRow(reactions: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        reactions.forEach { reaction ->
            Text(
                text = reaction,
                style = IssueTypo.Regular15.copy(color = Title),
            )
        }
    }
}

@Composable
private fun LandingIssueActionButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(backgroundColor, RoundedCornerShape(13.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = IssueTypo.Bold18.copy(color = textColor),
        )
    }
}

@Composable
private fun LandingGuideComment(
    nickname: String,
    content: String,
) {
    Row(verticalAlignment = Alignment.Top) {
        Image(
            painter = painterResource(R.drawable.ic_character_default),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(nickname, style = IssueTypo.Regular12.copy(color = Gray_7))
            Text(
                content,
                style = IssueTypo.Regular15.copy(color = White),
                modifier = Modifier
                    .background(BrandColor, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun LandingGuideCallout(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(15.dp),
                clip = false,
            )
            .background(White, RoundedCornerShape(15.dp))
            .border(1.dp, Gray_5.copy(alpha = 0.7f), RoundedCornerShape(15.dp))
            .padding(horizontal = 12.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontFamily = suiteFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 21.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LandingCompleteContent(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        Image(
            painter = painterResource(R.drawable.img_logo),
            contentDescription = "이슈있슈",
            modifier = Modifier
                .width(250.dp)
                .height(190.dp),
            contentScale = ContentScale.Fit,
        )

        Text(
            text = "함께 만들어가는\n우리 동네",
            fontFamily = chabFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 34.sp,
            lineHeight = 44.sp,
            color = Title,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "여러분의 작은 제보와 이야기가\n더 나은 변화를 만들어갑니다",
            fontFamily = suiteFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 32.sp,
            color = Gray_7,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandColor),
        ) {
            Text(
                text = "시작하기",
                style = IssueTypo.Bold18.copy(color = White),
            )
        }
    }
}

@Preview(
    name = "Landing - Intro",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingIntroPreview() {
    IssueissyuTheme {
        LandingPage(
            page = 0,
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview(
    name = "Landing - Pin Types",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingPinTypesPreview() {
    IssueissyuTheme {
        LandingPage(
            page = 1,
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview(
    name = "Landing - Issue Pin Overlay",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingIssuePinOverlayPreview() {
    IssueissyuTheme {
        LandingPage(
            page = 2,
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview(
    name = "Landing - Issue Reactions Guide",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingIssueReactionsGuidePreview() {
    IssueissyuTheme {
        LandingPage(
            page = 3,
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview(
    name = "Landing - Issue Community Button Guide",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingIssueCommunityButtonGuidePreview() {
    IssueissyuTheme {
        LandingPage(
            page = 4,
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview(
    name = "Landing - Festival Pin Overlay",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingFestivalPinOverlayPreview() {
    IssueissyuTheme {
        LandingPage(
            page = 5,
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview(
    name = "Landing - Shop Pin Overlay",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingShopPinOverlayPreview() {
    IssueissyuTheme {
        LandingPage(
            page = 6,
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview(
    name = "Landing - Communication Pin Overlay",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingCommunicationPinOverlayPreview() {
    IssueissyuTheme {
        LandingPage(
            page = 7,
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview(
    name = "Landing - Pin Create",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingPinCreatePreview() {
    IssueissyuTheme {
        LandingPage(
            page = 8,
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview(
    name = "Landing - History",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingHistoryPreview() {
    IssueissyuTheme {
        LandingPage(
            page = 9,
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview(
    name = "Landing - Community",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingCommunityPreview() {
    IssueissyuTheme {
        LandingPage(
            page = 10,
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview(
    name = "Landing - Issue Emoji And Comments Guide",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingIssueEmojiAndCommentsGuidePreview() {
    IssueissyuTheme {
        LandingPage(
            page = 11,
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview(
    name = "Landing - Issue Actions Guide",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingIssueActionsGuidePreview() {
    IssueissyuTheme {
        LandingPage(
            page = 12,
            onNext = {},
            onComplete = {},
        )
    }
}

@Preview(
    name = "Landing - Complete",
    showSystemUi = true,
    widthDp = 412,
    heightDp = 917,
)
@Composable
private fun LandingCompletePreview() {
    IssueissyuTheme {
        LandingPage(
            page = LANDING_STAGE_COUNT - 1,
            onNext = {},
            onComplete = {},
        )
    }
}
