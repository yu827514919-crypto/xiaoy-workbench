package com.example.xiaoy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// 圆角体系：票据/档案页/相框等形态，克制不夸张
private val XiaoYShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun XiaoYTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkXiaoYColors else LightXiaoYColors
    CompositionLocalProvider(LocalXiaoYColors provides colors) {
        val scheme = if (darkTheme) {
            darkColorScheme(
                primary = colors.primary, onPrimary = Color(0xFF2A1F0E),
                primaryContainer = colors.primarySoft, onPrimaryContainer = colors.primaryDeep,
                secondary = colors.secondary, onSecondary = Color(0xFF1E2418),
                secondaryContainer = colors.secondarySoft, onSecondaryContainer = colors.secondaryDeep,
                tertiary = colors.accent, onTertiary = Color.White,
                tertiaryContainer = colors.accentSoft, onTertiaryContainer = colors.accent,
                background = colors.background, onBackground = colors.ink,
                surface = colors.surface, onSurface = colors.ink,
                surfaceVariant = colors.surfaceWarm, onSurfaceVariant = colors.inkSoft,
                outline = colors.line, outlineVariant = colors.line,
                error = colors.accent, onError = Color.White
            )
        } else {
            lightColorScheme(
                primary = colors.primary, onPrimary = Color.White,
                primaryContainer = colors.primarySoft, onPrimaryContainer = colors.primaryDeep,
                secondary = colors.secondary, onSecondary = Color.White,
                secondaryContainer = colors.secondarySoft, onSecondaryContainer = colors.secondaryDeep,
                tertiary = colors.accent, onTertiary = Color.White,
                tertiaryContainer = colors.accentSoft, onTertiaryContainer = colors.accent,
                background = colors.background, onBackground = colors.ink,
                surface = colors.surface, onSurface = colors.ink,
                surfaceVariant = colors.surfaceWarm, onSurfaceVariant = colors.inkSoft,
                outline = colors.line, outlineVariant = colors.line,
                error = colors.accent, onError = Color.White
            )
        }
        MaterialTheme(colorScheme = scheme, typography = Typography, shapes = XiaoYShapes, content = content)
    }
}
