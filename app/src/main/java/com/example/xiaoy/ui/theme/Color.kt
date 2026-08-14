package com.example.xiaoy.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// —— 可主题化调色板：小芽 ——
// 主色：杏黄；辅助：鼠尾草绿；强调：陶土红；纸感背景；叶绿（成长树）
@Immutable
data class XiaoYColors(
    val primary: Color, val primaryDeep: Color, val primarySoft: Color, val primaryMist: Color,
    val secondary: Color, val secondaryDeep: Color, val secondarySoft: Color,
    val accent: Color, val accentSoft: Color,
    val background: Color, val surface: Color, val surfaceWarm: Color,
    val ink: Color, val inkSoft: Color, val inkFaint: Color, val line: Color,
    val leaf: Color, val leafSoft: Color,
    val typeBody: Color, val typeInterest: Color, val typeActivity: Color, val typeItem: Color,
    val plannedContainer: Color
)

val LightXiaoYColors = XiaoYColors(
    primary = Color(0xFFC98736), primaryDeep = Color(0xFFA86F2B),
    primarySoft = Color(0xFFF3E3C8), primaryMist = Color(0xFFFBF3E4),
    secondary = Color(0xFF7C9063), secondaryDeep = Color(0xFF5F7350),
    secondarySoft = Color(0xFFDDE4CF),
    accent = Color(0xFFB9503A), accentSoft = Color(0xFFF2D9CE),
    background = Color(0xFFFAF6EF), surface = Color(0xFFFFFDF8),
    surfaceWarm = Color(0xFFF6EFE3),
    ink = Color(0xFF2F2A22), inkSoft = Color(0xFF6E665A),
    inkFaint = Color(0xFFA39A8C), line = Color(0xFFE8E0D2),
    leaf = Color(0xFF6B8E5A), leafSoft = Color(0xFFE3EDD7),
    typeBody = Color(0xFF5B8BB0), typeInterest = Color(0xFF9A6BB0),
    typeActivity = Color(0xFFC8793A), typeItem = Color(0xFF6E7C8A),
    plannedContainer = Color(0xFFECE7DC)
)

val DarkXiaoYColors = XiaoYColors(
    primary = Color(0xFFE0A85C), primaryDeep = Color(0xFFF0C98A),
    primarySoft = Color(0xFF3A3020), primaryMist = Color(0xFF28231C),
    secondary = Color(0xFF9BB485), secondaryDeep = Color(0xFFB8CEA0),
    secondarySoft = Color(0xFF2D3826),
    accent = Color(0xFFD97A5F), accentSoft = Color(0xFF3E2A22),
    background = Color(0xFF1E1A15), surface = Color(0xFF28231C),
    surfaceWarm = Color(0xFF302A22),
    ink = Color(0xFFF2ECE0), inkSoft = Color(0xFFB3A896),
    inkFaint = Color(0xFF7E7466), line = Color(0xFF3D362D),
    leaf = Color(0xFF8FB574), leafSoft = Color(0xFF2A3726),
    typeBody = Color(0xFF82ACC9), typeInterest = Color(0xFFB58FCC),
    typeActivity = Color(0xFFDC9C60), typeItem = Color(0xFF94A3AF),
    plannedContainer = Color(0xFF38322A)
)

val LocalXiaoYColors = staticCompositionLocalOf { LightXiaoYColors }

// —— Composable 语义色：保持原变量名，import 与调用处无需改动 ——
val Apricot: Color @Composable get() = LocalXiaoYColors.current.primary
val ApricotDeep: Color @Composable get() = LocalXiaoYColors.current.primaryDeep
val ApricotSoft: Color @Composable get() = LocalXiaoYColors.current.primarySoft
val ApricotMist: Color @Composable get() = LocalXiaoYColors.current.primaryMist
val Sage: Color @Composable get() = LocalXiaoYColors.current.secondary
val SageDeep: Color @Composable get() = LocalXiaoYColors.current.secondaryDeep
val SageSoft: Color @Composable get() = LocalXiaoYColors.current.secondarySoft
val Terracotta: Color @Composable get() = LocalXiaoYColors.current.accent
val TerracottaSoft: Color @Composable get() = LocalXiaoYColors.current.accentSoft
val Cream: Color @Composable get() = LocalXiaoYColors.current.background
val Paper: Color @Composable get() = LocalXiaoYColors.current.surface
val PaperWarm: Color @Composable get() = LocalXiaoYColors.current.surfaceWarm
val Ink: Color @Composable get() = LocalXiaoYColors.current.ink
val InkSoft: Color @Composable get() = LocalXiaoYColors.current.inkSoft
val InkFaint: Color @Composable get() = LocalXiaoYColors.current.inkFaint
val Line: Color @Composable get() = LocalXiaoYColors.current.line
val LeafGreen: Color @Composable get() = LocalXiaoYColors.current.leaf
val LeafGreenSoft: Color @Composable get() = LocalXiaoYColors.current.leafSoft
