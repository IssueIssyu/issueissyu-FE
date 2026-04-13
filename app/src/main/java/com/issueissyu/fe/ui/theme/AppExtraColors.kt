package com.issueissyu.fe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Stable
data class AppExtraColors(
    val text: Color,
    val title: Color,

    val issue: Color,
    val shop: Color,
    val festival: Color,
    val communication: Color,
    val issueContainer: Color,
    val shopContainer: Color,
    val festivalContainer: Color,
    val communicationContainer: Color,

    val issueContainerLight: Color,
    val shopContainerLight: Color,
    val festivalContainerLight: Color,
    val communicationContainerLight: Color,

    val success: Color,
    val warning: Color,
    val infoBlue: Color,
    val lime: Color,
    val gold: Color,
    val mint: Color,
)

val LightAppExtraColors = AppExtraColors(
    text = Text,
    title = Title,

    issue = Issue,
    shop = Shop,
    festival = Festival,
    communication = Communication,
    issueContainer = IssueContainer,
    shopContainer = ShopContainer,
    festivalContainer = FestivalContainer,
    communicationContainer = CommunicationContainer,

    issueContainerLight = IssueContainerLight,
    shopContainerLight = ShopContainerLight,
    festivalContainerLight = FestivalContainerLight,
    communicationContainerLight = CommunicationContainerLight,

    success = Success,
    warning = Warning,
    infoBlue = InfoBlue,
    lime = Lime,
    gold = Gold,
    mint = Mint,
)

val LocalAppExtraColors = staticCompositionLocalOf<AppExtraColors> {
    error("No AppExtraColors provided")
}

val MaterialTheme.appExtraColors: AppExtraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppExtraColors.current