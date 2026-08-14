package com.example.xiaoy.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.AppState
import com.example.xiaoy.data.RecordStatus
import com.example.xiaoy.data.RecordType
import com.example.xiaoy.data.formatDate
import com.example.xiaoy.ui.components.DonutChart
import com.example.xiaoy.ui.components.LegendItem
import com.example.xiaoy.ui.components.MiniBars
import com.example.xiaoy.ui.components.SectionTitle
import com.example.xiaoy.ui.components.TagChip
import com.example.xiaoy.ui.components.chartPalette
import com.example.xiaoy.ui.navigation.Route
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.LeafGreen
import com.example.xiaoy.ui.theme.Line
import com.example.xiaoy.ui.theme.Paper
import com.example.xiaoy.ui.theme.Sage
import com.example.xiaoy.ui.theme.Terracotta
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun InsightsScreen(appState: AppState, nav: (Route) -> Unit) {
    val data by appState.data.collectAsState()
    var range by remember { mutableIntStateOf(7) }

    val doneDays = appState.dailyDone(range)
    val totalDone = doneDays.sumOf { it.second }
    val labels = doneDays.map { SimpleDateFormat("M/d", Locale.CHINA).format(it.first) }

    val typeCounts = appState.typeCounts().filter { it.second > 0 }
    val totalRecords = data.records.size
    val donut = typeCounts.map { (t, n) -> t to (n.toFloat() / totalRecords.coerceAtLeast(1)) }

    val bodyRecords = data.records.filter { it.type == RecordType.BODY.id }.sortedBy { it.dateEpoch }
    val lastHeight = bodyRecords.lastOrNull()?.num1
    val prevHeight = bodyRecords.dropLast(1).lastOrNull()?.num1
    val heightDelta = if (lastHeight != null && prevHeight != null) lastHeight - prevHeight else null

    val thisWeekDone = appState.thisWeekDone()
    val lastWeekDone = appState.lastWeekDone()
    val todayAttention = data.records.filter { it.status == RecordStatus.ATTENTION.id || it.status == RecordStatus.TODO.id }
        .sortedByDescending { it.dateEpoch }.firstOrNull()

    val milestones = data.records.filter { it.type == RecordType.GROWTH.id }
        .sortedByDescending { it.dateEpoch }.take(8)

    val heatmap = appState.dailyDone(105)

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("数据洞察", style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
                Text("看看这段时间的成长，数据都来自你的记录", style = MaterialTheme.typography.labelSmall, color = InkSoft)
            }
        }

        // 完成趋势
        item {
            SectionTitle("完成趋势", modifier = Modifier.padding(horizontal = 16.dp))
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
                .clip(RoundedCornerShape(20.dp)).background(Paper).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("近 $range 天共完成 $totalDone 项", style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    listOf(7, 30).forEach { r ->
                        TagChip("近${r}天", selected = range == r, modifier = Modifier.padding(start = 4.dp)) { range = r }
                    }
                }
                Spacer(Modifier.height(16.dp))
                MiniBars(doneDays.map { it.second }, color = Apricot, height = 80.dp, labels = labels)
                Spacer(Modifier.height(8.dp))
                Text("数据来源：已完成记录数按日期统计", style = MaterialTheme.typography.labelSmall, color = InkSoft)
            }
        }

        // 打卡热力图
        item {
            SectionTitle("打卡热力图", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp))
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
                .clip(RoundedCornerShape(20.dp)).background(Paper).padding(16.dp)) {
                HabitHeatmap(heatmap)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("浅", style = MaterialTheme.typography.labelSmall, color = InkSoft)
                    Spacer(Modifier.width(4.dp))
                    Box(Modifier.size(11.dp).clip(RoundedCornerShape(3.dp)).background(Line))
                    Spacer(Modifier.width(3.dp))
                    Box(Modifier.size(11.dp).clip(RoundedCornerShape(3.dp)).background(Sage.copy(alpha = 0.5f)))
                    Spacer(Modifier.width(3.dp))
                    Box(Modifier.size(11.dp).clip(RoundedCornerShape(3.dp)).background(Sage))
                    Spacer(Modifier.width(3.dp))
                    Box(Modifier.size(11.dp).clip(RoundedCornerShape(3.dp)).background(LeafGreen))
                    Spacer(Modifier.width(6.dp))
                    Text("多", style = MaterialTheme.typography.labelSmall, color = InkSoft)
                    Spacer(Modifier.weight(1f))
                    Text("近 15 周", style = MaterialTheme.typography.labelSmall, color = InkSoft)
                }
                Spacer(Modifier.height(6.dp))
                Text("数据来源：每天有完成记录的天数", style = MaterialTheme.typography.labelSmall, color = InkSoft)
            }
        }

        // 分类占比
        item {
            SectionTitle("分类占比", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp))
        }
        item {
            Row(
                Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
                    .clip(RoundedCornerShape(20.dp)).background(Paper).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DonutChart(
                    segments = donut.mapIndexed { i, (_, frac) -> chartPalette[i % chartPalette.size] to frac },
                    centerTop = "$totalRecords", centerBottom = "条记录"
                )
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    typeCounts.forEachIndexed { i, (t, n) ->
                        LegendItem(chartPalette[i % chartPalette.size], "${t.label} · $n 条")
                    }
                }
            }
        }

        // 近期变化
        item {
            SectionTitle("近期变化", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp))
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
                .clip(RoundedCornerShape(20.dp)).background(Paper).padding(16.dp)) {
                if (bodyRecords.size >= 2) {
                    HeightTrendChart(bodyRecords)
                    Spacer(Modifier.height(8.dp))
                }
                ChangeRow("身高", lastHeight?.let { String.format(Locale.CHINA, "%.1f cm", it) } ?: "—",
                    heightDelta?.let { if (it >= 0) "较上次 +${String.format(Locale.CHINA, "%.1f", it)} cm" else "较上次 ${String.format(Locale.CHINA, "%.1f", it)} cm" } ?: "—",
                    color = LeafGreen)
                ChangeRow("本周完成", "$thisWeekDone 项",
                    if (thisWeekDone - lastWeekDone >= 0) "较上周 +${thisWeekDone - lastWeekDone}" else "较上周 ${thisWeekDone - lastWeekDone}",
                    color = Sage)
                ChangeRow("成长里程碑", "${appState.milestones().size} 个", "每一次长大都算数", color = Terracotta)
            }
        }

        // 成长里程碑时间轴
        if (milestones.isNotEmpty()) {
            item {
                SectionTitle("成长里程碑", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp))
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
                    .clip(RoundedCornerShape(20.dp)).background(Paper).padding(16.dp)) {
                    MilestoneTimeline(milestones)
                }
            }
        }

        // 建议
        item {
            SectionTitle("给悦悦妈的小建议", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp))
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
                .clip(RoundedCornerShape(20.dp)).background(Paper).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (thisWeekDone < lastWeekDone) {
                    TipRow("本周完成数比上周少，可以把计划精简，聚焦最想完成的 2～3 件。")
                } else {
                    TipRow("本周节奏很稳，保持住，别忘了给孩子一点小鼓励。")
                }
                todayAttention?.let { r ->
                    TipRow("今天有一件「${r.statusEnum().label}」的事项「${r.title}」，记得优先处理。")
                }
                if (heightDelta != null && heightDelta < 0.3) {
                    TipRow("身高变化不大是正常的，关注饮食和睡眠，下次体检再对比看看。")
                }
            }
        }

        // 阶段报告
        item {
            Column(Modifier.padding(horizontal = 16.dp).padding(top = 20.dp)) {
                androidx.compose.material3.Button(
                    onClick = { nav(Route.Report(RecordType.GROWTH.id)) },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Apricot),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("生成成长阶段报告")
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.ChevronRight, null)
                }
            }
        }
    }
}

@Composable
private fun ChangeRow(label: String, value: String, delta: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(12.dp))
        Text(delta, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun TipRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(Apricot))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Ink)
    }
}

@Composable
private fun HeightTrendChart(records: List<com.example.xiaoy.data.Record>) {
    val heights = records.mapNotNull { it.num1?.toFloat() }
    val labels = records.map { formatDate(it.dateEpoch) }
    Column {
        Text("身高曲线", style = MaterialTheme.typography.labelMedium, color = InkSoft)
        Spacer(Modifier.height(6.dp))
        Canvas(Modifier.fillMaxWidth().height(80.dp)) {
            if (heights.size >= 2) {
                val min = heights.minOrNull()!! - 1f
                val max = heights.maxOrNull()!! + 1f
                val stepX = if (heights.size == 1) 0f else size.width / (heights.size - 1)
                val pts = heights.mapIndexed { i, h ->
                    Offset(i * stepX, size.height - (h - min) / (max - min) * size.height)
                }
                val path = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    pts.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(path, color = Apricot, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                pts.forEach { p -> drawCircle(LeafGreen, radius = 4.dp.toPx(), center = p) }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Text(labels.first(), style = MaterialTheme.typography.labelSmall, color = InkSoft)
            Spacer(Modifier.weight(1f))
            Text(labels.last(), style = MaterialTheme.typography.labelSmall, color = InkSoft)
        }
    }
}

@Composable
private fun MilestoneTimeline(records: List<com.example.xiaoy.data.Record>) {
    Column {
        records.forEachIndexed { i, r ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(if (i == 0) Apricot else Sage))
                    if (i != records.lastIndex) {
                        Box(Modifier.width(2.dp).height(48.dp).background(Line))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f).padding(bottom = if (i == records.lastIndex) 0.dp else 12.dp)) {
                    Text(formatDate(r.dateEpoch), style = MaterialTheme.typography.labelSmall, color = InkSoft)
                    Spacer(Modifier.height(2.dp))
                    Text(r.title, style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.SemiBold)
                    if (r.notes.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(r.notes, style = MaterialTheme.typography.bodySmall, color = InkSoft, maxLines = 2)
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitHeatmap(data: List<Pair<Long, Int>>) {
    val weeks = data.size / 7
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        for (w in 0 until weeks) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                for (d in 0 until 7) {
                    val idx = w * 7 + d
                    if (idx < data.size) {
                        val count = data[idx].second
                        Box(Modifier.size(11.dp).clip(RoundedCornerShape(3.dp)).background(heatColor(count)))
                    }
                }
            }
        }
    }
}

@Composable
private fun heatColor(count: Int): Color = when {
    count == 0 -> Line
    count <= 1 -> Sage.copy(alpha = 0.55f)
    count <= 2 -> Sage
    else -> LeafGreen
}
