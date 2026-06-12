package com.issueissyu.fe.ui.screens.landing

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocationOn
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
import com.issueissyu.fe.ui.theme.BrandColor
import com.issueissyu.fe.ui.theme.Communication
import com.issueissyu.fe.ui.theme.Festival
import com.issueissyu.fe.ui.theme.FestivalContainer
import com.issueissyu.fe.ui.theme.Gray_4
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
private const val LANDING_STAGE_COUNT = 12

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
                        pagerState.animateScrollToPage(page + 1)
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color(0xFFF3F8FF))
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
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LandingProgress(
                    currentPage = page.toProgressIndex(),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(44.dp))

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
        else -> LandingCommunityPage(modifier)
    }
}

private fun Int.toProgressIndex(): Int {
    return when (this) {
        0 -> 0
        in 1..7 -> 1
        8 -> 2
        9 -> 3
        10 -> 4
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
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.img_logo),
            contentDescription = "이슈있슈",
            modifier = Modifier
                .width(220.dp)
                .height(150.dp),
            contentScale = ContentScale.Fit,
        )

        Text(
            text = "우리 동네의\n모든 이슈를 한눈에!",
            style = IssueTypo.ExtraBold30.copy(
                color = Gray_7,
                lineHeight = 44.sp,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "이슈를 발견하고\n핀을 찍어 공유하고\n해결해 보세요!",
            style = IssueTypo.Regular18.copy(
                color = Gray_7,
                lineHeight = 36.sp,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(18.dp))

        Image(
            painter = painterResource(R.drawable.img_logo_circle),
            contentDescription = null,
            modifier = Modifier.size(118.dp),
            contentScale = ContentScale.Fit,
        )
    }
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
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LandingTitle(
                title = "핀의 종류",
                subtitle = "다양한 핀으로 정보를 한눈에 구분할 수 있어요!",
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                contentAlignment = Alignment.Center,
            ) {
                LandingPinTypeGrid(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
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
                iconRes = R.drawable.issue,
                modifier = Modifier.weight(1f),
            )
            LandingPinTypeCard(
                label = "행사",
                title = "축제·행사",
                description = "우리 동네의\n축제 및 행사를 한번에!",
                color = FestivalContainer,
                accentColor = Festival,
                iconRes = R.drawable.festival,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                iconRes = R.drawable.shop,
                modifier = Modifier.weight(1f),
            )
            LandingPinTypeCard(
                label = "소통",
                title = "동네 소통",
                description = "이웃과 의견을\n자유롭게 나누어요!",
                color = Communication,
                accentColor = BrandColor,
                iconRes = R.drawable.communicate,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(196.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.88f))
            .border(4.dp, White.copy(alpha = 0.72f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = IssueTypo.Bold12.copy(color = White),
            modifier = Modifier
                .align(Alignment.Start)
                .background(accentColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )

        Box(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 6.dp)
                .size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(46.dp),
            )
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(38.dp),
            )
        }

        Text(
            text = title,
            style = IssueTypo.ExtraBold18.copy(color = Title),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = IssueTypo.Regular15.copy(
                color = Gray_7,
                lineHeight = 21.sp,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LandingPinShowcaseCard(
    type: LandingPinShowcaseType,
    guideFocus: LandingPinGuideFocus = LandingPinGuideFocus.NONE,
    modifier: Modifier = Modifier,
) {
    val data = when (type) {
        LandingPinShowcaseType.ISSUE -> LandingPinShowcaseData(
            title = "쓰레기 무단투기",
            description = "최근 골목에서 쓰레기 무단투기가 지속적으로 발생하고 있습니다. " +
                "주변 환경과 위생 문제로 인해 주민 불편이 커지고 있어요.",
            badge = "해결 완료",
            reaction = "😩 3   🤬 1   😡 1   +11",
            color = IssueContainer,
            accentColor = Issue,
            iconRes = R.drawable.issue,
        )
        LandingPinShowcaseType.FESTIVAL -> LandingPinShowcaseData(
            title = "버룩시장 행사",
            description = "함께 나누는 따뜻한 하루, 이웃과 함께하는 특별한 하루를 만나보세요.",
            badge = "#소소한 행복",
            reaction = "👍 34",
            color = FestivalContainer,
            accentColor = Festival,
            iconRes = R.drawable.festival,
        )
        LandingPinShowcaseType.SHOP -> LandingPinShowcaseData(
            title = "커피빈",
            description = "전 품목 50% 할인! 소중한 고객님께 감사하는 마음을 담아 진행하는 봄맞이 행사입니다.",
            badge = "전 품목 50% 할인",
            reaction = "👍 6",
            color = ShopContainer,
            accentColor = Shop,
            iconRes = R.drawable.shop,
        )
        LandingPinShowcaseType.COMMUNICATION -> LandingPinShowcaseData(
            title = "비둘기 폭탄 맞음",
            description = "진짜 요즘 비둘기 때문에 너무 불편함. 사람들이 많이 지나다니는 곳에 " +
                "비둘기가 계속 모여 있으니까 은근 스트레스임.",
            badge = "#동네 소통",
            reaction = "😨 3   🤣 3   💢 1",
            color = Communication,
            accentColor = BrandColor,
            iconRes = R.drawable.communicate,
        )
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(data.color)
            .padding(22.dp),
    ) {
        val contentAlpha =
            if (guideFocus == LandingPinGuideFocus.NONE) 1f else 0.35f

        Row(
            modifier = Modifier.alpha(contentAlpha),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(data.iconRes),
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = data.title,
                style = IssueTypo.ExtraBold30.copy(color = Gray_7),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.alpha(contentAlpha),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "서울 마포구 홍익로 6길 34",
                style = IssueTypo.Regular12.copy(color = Gray_7),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = data.badge,
            style = IssueTypo.Bold12.copy(color = data.accentColor),
            modifier = Modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(16.dp))
                .border(1.dp, data.accentColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .alpha(contentAlpha),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = data.description,
            modifier = Modifier.alpha(contentAlpha),
            style = IssueTypo.Regular15.copy(
                color = Gray_7,
                lineHeight = 22.sp,
            ),
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = data.reaction,
                modifier = Modifier.alpha(
                    if (
                        guideFocus == LandingPinGuideFocus.NONE ||
                        guideFocus == LandingPinGuideFocus.REACTIONS
                    ) {
                        1f
                    } else {
                        0.35f
                    }
                ),
                style = IssueTypo.Regular16.copy(color = Title),
            )
            Text(
                text = "커뮤니티 ›",
                style = IssueTypo.Bold12.copy(color = White),
                modifier = Modifier
                    .background(data.accentColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .alpha(
                        if (
                            guideFocus == LandingPinGuideFocus.NONE ||
                            guideFocus == LandingPinGuideFocus.COMMUNITY_BUTTON
                        ) {
                            1f
                        } else {
                            0.35f
                        }
                    ),
            )
        }
    }
}

private data class LandingPinShowcaseData(
    val title: String,
    val description: String,
    val badge: String,
    val reaction: String,
    val color: Color,
    val accentColor: Color,
    val iconRes: Int,
)

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
                        .padding(20.dp),
                ) {
                    Text(
                        text = "‹             이슈 작성",
                        style = IssueTypo.Bold12.copy(color = Title),
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    LandingFormField("사진", "사진 추가")
                    LandingFormField("등록 장소", "서울시 마포구")
                    LandingFormField("제목", "제목을 작성해 주세요.")
                    LandingFormField("내용", "상세 설명을 작성해 주세요.", height = 92.dp)
                    Text(
                        text = "#간편하게  #친근하게  #부드럽게",
                        style = IssueTypo.Regular12.copy(color = Gray_7),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .background(Gray_4, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("작성 완료", style = IssueTypo.Bold12.copy(color = White))
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
) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(label, style = IssueTypo.Regular12.copy(color = Gray_7))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .border(1.dp, Gray_4, RoundedCornerShape(8.dp))
                .padding(10.dp),
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
                HistoryItem("쓰레기 무단투기", "해결 전", IssueContainer, Gray_7)
                HistoryItem("보도 블럭 파손", "진행 중", IssueContainer.copy(alpha = 0.6f), Issue)
                HistoryItem("신호등 고장", "해결 완료", Communication, BrandColor)
            }
        }
    }
}

@Composable
private fun HistoryItem(
    title: String,
    status: String,
    color: Color,
    statusColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .background(color, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = IssueTypo.Bold12.copy(color = Title))
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = "서울 마포구 홍익로",
                    style = IssueTypo.Regular12.copy(color = Gray_7),
                )
            }
        }
        Text(
            text = status,
            style = IssueTypo.Bold12.copy(color = White),
            modifier = Modifier
                .background(statusColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
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
                        .background(White)
                        .padding(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        listOf("🔥 HOT", "📍 이슈", "🏪 가게", "🎉 행사").forEach { label ->
                            Text(
                                text = label,
                                style = IssueTypo.Bold12.copy(color = Gray_7),
                                modifier = Modifier
                                    .background(Color(0xFFF4F4F4), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 6.dp, vertical = 5.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "우리 동네 인기 소식",
                        style = IssueTypo.ExtraBold18.copy(color = Title),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CommunityFeedItem(
                        icon = Icons.Default.Storefront,
                        title = "라멘야",
                        subtitle = "오늘만 특별 할인",
                        color = ShopContainer,
                    )
                    CommunityPostPreview()
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

@Composable
private fun CommunityPostPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFD), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_character_default),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("밤티", style = IssueTypo.Bold12.copy(color = Title))
                Text("04.07 18:24 · 조회 51", style = IssueTypo.Regular12.copy(color = Gray_7))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("쓰레기 무단투기", style = IssueTypo.Bold18.copy(color = Title))
        Text(
            "최근 골목에서 쓰레기 무단투기가 지속적으로 발생하고 있습니다.",
            style = IssueTypo.Regular12.copy(color = Gray_7, lineHeight = 17.sp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text("❤️ 30  🤯 10  😡 3  👊 1", style = IssueTypo.Regular12.copy(color = Title))
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = BrandColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("저건 좀 아니다..", style = IssueTypo.Regular12.copy(color = Gray_7))
        }
    }
}

@Composable
private fun CommunityFeedItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(color.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandColor,
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = IssueTypo.Bold18.copy(color = Title))
            Text(subtitle, style = IssueTypo.Regular12.copy(color = Gray_7))
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
        style = IssueTypo.ExtraBold30.copy(color = Color.Black),
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = subtitle,
        style = IssueTypo.Regular16.copy(
            color = Gray_7,
            lineHeight = 24.sp,
        ),
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
