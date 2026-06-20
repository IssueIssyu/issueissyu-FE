package com.issueissyu.fe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme = lightColorScheme(
    primary = BrandColor,
    onPrimary = White,
    primaryContainer = CommunicationContainerLight,
    onPrimaryContainer = Title,

    secondary = Orange,
    onSecondary = White,
    secondaryContainer = LightOrange,
    onSecondaryContainer = Title,

    tertiary = LightBrown,
    onTertiary = Title,
    tertiaryContainer = ShopContainerLight,
    onTertiaryContainer = Title,

    background = Gray_1,
    onBackground = Title,

    surface = White,
    onSurface = Title,

    surfaceVariant = Gray_2,
    onSurfaceVariant = Gray_6,

    outline = Gray_4,
    outlineVariant = Gray_3,

    error = Issue,
    onError = White,
    errorContainer = IssueContainerLight,
    onErrorContainer = Title,

    inverseSurface = Title,
    inverseOnSurface = Gray_1,
    surfaceTint = BrandColor
)

@Composable
fun IssueissyuTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppExtraColors provides LightAppExtraColors
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}