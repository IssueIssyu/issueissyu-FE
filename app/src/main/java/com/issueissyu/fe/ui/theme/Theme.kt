package com.issueissyu.fe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
// Color.White 사용을 위해 추가. CustomAppColors를 위한 임포트도 필요합니다.
import androidx.compose.ui.graphics.Color

@Composable
fun Theme(
    content: @Composable () -> Unit
) {
    // CustomAppColors 인스턴스 생성
    val customAppColors = CustomAppColors(
        brandColor = BrandColor,
        orange = Orange,
        lightOrange = LightOrange,
        lightBrown = LightBrown,
        issue = Issue,
        shop = Shop,
        festival = Festival,
        communicationAndEtc = CommunicationAndEtc,
        title = Title,
        text = Text,
        gray1 = Gray_1,
        gray2 = Gray_2,
        gray3 = Gray_3,
        gray4 = Gray_4,
        gray5 = Gray_5,
        gray6 = Gray_6
    )

    CompositionLocalProvider(LocalCustomAppColors provides customAppColors) {

        MaterialTheme(
            colorScheme = ColorScheme(),
            typography = Typography,
            content = content
        )
    }
}
