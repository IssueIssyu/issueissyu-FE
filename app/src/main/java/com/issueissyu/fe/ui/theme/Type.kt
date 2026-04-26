package com.issueissyu.fe.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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

object IssueTypo {
    val ExtraBold30 = TextStyle(
        fontFamily = suiteFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp
    )

    val ExtraBold15 = TextStyle(
    fontFamily = suiteFontFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 15.sp
    )

    val ExtraBold12 = TextStyle(
    fontFamily = suiteFontFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 12.sp
    )

    val Bold18 = TextStyle(
    fontFamily = suiteFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp
    )

    val Bold12 = TextStyle(
    fontFamily = suiteFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 12.sp
    )

    val Regular18 = TextStyle(
        fontFamily = suiteFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    )

    val Regular16 = TextStyle(
        fontFamily = suiteFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )

    val Regular15 = TextStyle(
    fontFamily = suiteFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp
    )

    val Regular12 = TextStyle(
    fontFamily = suiteFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp
    )
}