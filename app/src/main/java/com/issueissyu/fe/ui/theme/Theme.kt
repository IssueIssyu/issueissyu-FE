package com.issueissyu.fe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color // Color.White 사용을 위해 추가

private val LightColorScheme = lightColorScheme(
    primary = BrandColor,
    secondary = Orange,
    tertiary = LightOrange,

    background = Gray_1,
    surface = Color.White,
    surfaceVariant = Gray_2,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,

    onBackground = Title,
    onSurface = Title,
    onSurfaceVariant = Text,

    outline = Gray_4,
    outlineVariant = Gray_3
)

@Composable
fun Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
