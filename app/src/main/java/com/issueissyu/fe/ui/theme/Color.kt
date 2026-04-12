package com.issueissyu.fe.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val BrandColor = Color(0xFF1D87FF)
val Orange = Color(0xFFFF8A46)
val LightOrange = Color(0xFFFFA264)
val LightBrown = Color(0xFFFFAC1C)
val Issue = Color(0xFFFF4800)
val Shop = Color(0xFFFFAE00)
val Festival = Color(0xFFBD1AFF)
val CommunicationAndEtc = Color(0xFF000000)
val Title = Color(0xFF1A1A1A)
val Text = Color(0xFF404040)
val Gray_1 = Color(0xFFF7F7F7)
val Gray_2 = Color(0xFFF0F0F0)
val Gray_3 = Color(0xFFE3E3E3)
val Gray_4 = Color(0xFFCCCCCC)
val Gray_5 = Color(0xFF999999)
val Gray_6 = Color(0xFF666666)

@Immutable
data class CustomAppColors(
    val brandColor: Color = BrandColor,
    val orange: Color = Orange,
    val lightOrange: Color = LightOrange,
    val lightBrown: Color = LightBrown,
    val issue: Color = Issue,
    val shop: Color = Shop,
    val festival: Color = Festival,
    val communicationAndEtc: Color = CommunicationAndEtc,
    val title: Color = Title,
    val text: Color = Text,
    val gray1: Color = Gray_1,
    val gray2: Color = Gray_2,
    val gray3: Color = Gray_3,
    val gray4: Color = Gray_4,
    val gray5: Color = Gray_5,
    val gray6: Color = Gray_6
)

val LocalCustomAppColors = staticCompositionLocalOf { CustomAppColors() }
