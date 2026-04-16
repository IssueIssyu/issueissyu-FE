package com.issueissyu.fe.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.issueissyu.fe.R

val chabFontFamily = FontFamily(
    Font(R.font.chab, FontWeight.Normal)
)

val suiteFontFamily = FontFamily(
    Font(R.font.suite_light, FontWeight.Light),
    Font(R.font.suite_regular, FontWeight.Normal),
    Font(R.font.suite_medium, FontWeight.Medium),
    Font(R.font.suite_semi_bold, FontWeight.SemiBold),
    Font(R.font.suite_bold, FontWeight.Bold),
    Font(R.font.suite_extra_bold, FontWeight.ExtraBold),
    Font(R.font.suite_heavy, FontWeight.Black)
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = chabFontFamily),
    bodyLarge = TextStyle(fontFamily = suiteFontFamily),
    titleLarge = TextStyle(fontFamily = suiteFontFamily)
)