package com.example.xiaoy.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Apricot,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = ApricotSoft,
    onPrimaryContainer = ApricotDeep,
    secondary = Sage,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = SageSoft,
    onSecondaryContainer = SageDeep,
    tertiary = Terracotta,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = TerracottaSoft,
    onTertiaryContainer = Terracotta,
    background = Cream,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperWarm,
    onSurfaceVariant = InkSoft,
    outline = Line,
    outlineVariant = Line,
    error = Terracotta,
    onError = androidx.compose.ui.graphics.Color.White
)

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
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = XiaoYShapes,
        content = content
    )
}
