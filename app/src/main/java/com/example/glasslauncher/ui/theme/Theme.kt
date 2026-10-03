package com.example.glasslauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.glasslauncher.core.glass.GlassTokens

private val LightColors = lightColorScheme(
    primary = GlassTokens.accent,
    onPrimary = GlassTokens.textPrimary,
    secondary = GlassTokens.accentSoft,
    background = GlassTokens.accentSoft,
    surface = GlassTokens.glassFillStrong,
    onSurface = GlassTokens.textPrimary,
)

private val DarkColors = darkColorScheme(
    primary = GlassTokens.accentSoft,
    secondary = GlassTokens.accent,
    background = GlassTokens.textPrimary,
    surface = GlassTokens.glassFill,
    onSurface = GlassTokens.pageDotActive,
)

/** Inter is the designed font; falls back to the system sans-serif if unavailable. */
private val LauncherFontFamily = FontFamily.SansSerif

private val LauncherTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = LauncherFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = GlassTokens.titleSize,
    ),
    labelSmall = TextStyle(
        fontFamily = LauncherFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = GlassTokens.sectionHeaderSize,
        letterSpacing = 0.4.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = LauncherFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = GlassTokens.bodySize,
    ),
    bodySmall = TextStyle(
        fontFamily = LauncherFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = GlassTokens.captionSize,
    ),
)

@Composable
fun GlassLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = LauncherTypography,
        content = content,
    )
}
