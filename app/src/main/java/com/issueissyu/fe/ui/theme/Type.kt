package com.issueissyu.fe.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.issueissyu.fe.R

val appFontFamily = FontFamily(R.font.app_font_family)

val Typography = Typography(
    defaultFontFamily = appFontFamily,
    headlineSmall = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = Typography().headlineSmall.fontSize
    ),
    bodyLarge = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = Typography().bodyLarge.fontSize
    )
)
