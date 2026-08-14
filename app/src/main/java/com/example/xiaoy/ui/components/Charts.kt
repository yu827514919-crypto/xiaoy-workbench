package com.example.xiaoy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkFaint
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.LeafGreen
import com.example.xiaoy.ui.theme.Line
import com.example.xiaoy.ui.theme.LocalXiaoYColors
import com.example.xiaoy.ui.theme.PaperWarm
import com.example.xiaoy.ui.theme.Sage
import com.example.xiaoy.ui.theme.Terracotta

/** 圆环进度 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    stroke: Dp = 7.dp,
    color: Color = Apricot,
    trackColor: Color = Line,
    center: @Composable () -> Unit = {}
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val strokePx = stroke.toPx()
            val inset = strokePx / 2
            val arc = androidx.compose.ui.geometry.Size(this.size.width - strokePx, this.size.height - strokePx)
            drawArc(
                color = trackColor,
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arc,
                style = Stroke(strokePx, cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f, sweepAngle = 360f * progress.coerceIn(0f, 1f), useCenter = false,
                    topLeft = Offset(inset, inset), size = arc,
                    style = Stroke(strokePx, cap = StrokeCap.Round)
                )
            }
        }
        center()
    }
}

/** 迷你柱状（近 7 天趋势） */
@Composable
fun MiniBars(
    values: List<Int>,
    modifier: Modifier = Modifier,
    color: Color = Apricot,
    height: Dp = 48.dp,
    labels: List<String> = emptyList()
) {
    val max = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    Column(modifier) {
        Row(Modifier.height(height), verticalAlignment = Alignment.Bottom) {
            values.forEachIndexed { i, v ->
                val barH = if (v == 0) 4.dp else (height * (v.toFloat() / max)).coerceAtLeast(8.dp)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .width(10.dp)
                            .height(barH)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (v == 0) Line else color)
                    )
                }
            }
        }
        if (labels.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                labels.forEach {
                    Text(it, Modifier.weight(1f), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall, color = InkFaint)
                }
            }
        }
    }
}

/** 环形占比图 */
@Composable
fun DonutChart(
    segments: List<Pair<Color, Float>>, // color -> 占比(0..1)
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    stroke: Dp = 20.dp,
    centerTop: String = "",
    centerBottom: String = ""
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val strokePx = stroke.toPx()
            val inset = strokePx / 2
            val arc = Size(this.size.width - strokePx, this.size.height - strokePx)
            var start = -90f
            segments.forEach { (color, frac) ->
                val sweep = 360f * frac.coerceIn(0f, 1f)
                drawArc(
                    color = color, startAngle = start, sweepAngle = sweep, useCenter = false,
                    topLeft = Offset(inset, inset), size = arc,
                    style = Stroke(strokePx, cap = StrokeCap.Butt)
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (centerTop.isNotBlank()) {
                Text(centerTop, style = MaterialTheme.typography.titleMedium, color = Ink)
            }
            if (centerBottom.isNotBlank()) {
                Text(centerBottom, style = MaterialTheme.typography.labelSmall, color = InkSoft)
            }
        }
    }
}

/** 图例 */
@Composable
fun LegendItem(color: Color, label: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = InkSoft)
    }
}

// 类型占比配色（与 Visual.tint 对应，保持统一）
val chartPalette: List<Color>
    @Composable get() = listOf(
        Apricot, Sage, Terracotta, LeafGreen,
        LocalXiaoYColors.current.typeBody, LocalXiaoYColors.current.typeInterest,
        LocalXiaoYColors.current.typeActivity, LocalXiaoYColors.current.typeItem
    )
