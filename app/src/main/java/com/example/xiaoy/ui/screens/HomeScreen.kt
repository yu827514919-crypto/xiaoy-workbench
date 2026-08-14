package com.example.xiaoy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.AppState
import com.example.xiaoy.data.Record
import com.example.xiaoy.data.RecordStatus
import com.example.xiaoy.data.RecordType
import com.example.xiaoy.data.ImageRef
import com.example.xiaoy.data.ageLabel
import com.example.xiaoy.data.formatDateWithWeekday
import com.example.xiaoy.data.isSameDay
import com.example.xiaoy.data.startOfToday
import com.example.xiaoy.ui.components.AppImage
import com.example.xiaoy.ui.components.MiniBars
import com.example.xiaoy.ui.components.ProgressRing
import com.example.xiaoy.ui.components.SectionTitle
import com.example.xiaoy.ui.components.dateLabel
import com.example.xiaoy.ui.components.metric
import com.example.xiaoy.ui.navigation.Route
import com.example.xiaoy.ui.icon
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.ApricotDeep
import com.example.xiaoy.ui.theme.ApricotSoft
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkFaint
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.LeafGreen
import com.example.xiaoy.ui.theme.LeafGreenSoft
import com.example.xiaoy.ui.theme.Paper
import com.example.xiaoy.ui.theme.PaperWarm
import com.example.xiaoy.ui.theme.Sage
import com.example.xiaoy.ui.theme.Terracotta
import com.example.xiaoy.ui.tint
import java.util.Calendar

@Composable
fun HomeScreen(appState: AppState, nav: (Route) -> Unit) {
    val data by appState.data.collectAsState()
    val profile = data.profile
    val records = data.records
    val today = startOfToday()
    val (done, total, rate) = appState.todayStats()
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 11 -> "早上好"
        hour < 14 -> "中午好"
        hour < 18 -> "下午好"
        else -> "晚上好"
    }

    val weekDone = appState.thisWeekDone()
    val weekDelta = weekDone - appState.lastWeekDone()
    val streak = records.filter { it.type == RecordType.ROUTINE.id && it.status == RecordStatus.DOING.id }
        .maxOfOrNull { it.num1 ?: 0.0 }?.toInt() ?: 0
    val leaves = appState.growthLeaves()
    val milestoneCount = appState.milestones().size

    val todayPriority = records
        .filter { isSameDay(it.dateEpoch, today) &&
            it.status != RecordStatus.DONE.id && it.status != RecordStatus.PLANNED.id }
        .sortedWith(compareBy(
            { it.status != RecordStatus.ATTENTION.id },
            { it.status != RecordStatus.TODO.id }
        ))
        .take(3)

    val recent = appState.allRecords().take(3)
    val upcoming = appState.upcomingReminders(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
    ) {
        // ===== 顶部品牌 + 当天状态 =====
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppImage(ImageRef.of("img_logo"), Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("小芽", style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
                    Text("把孩子的每一次长大，温柔记下来", style = MaterialTheme.typography.labelSmall, color = InkSoft)
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatDateWithWeekday(today), style = MaterialTheme.typography.labelMedium, color = InkSoft)
                    Text("今天", style = MaterialTheme.typography.labelSmall, color = InkFaint)
                }
                if (upcoming.isNotEmpty()) {
                    IconButton(onClick = { nav(Route.Plan) }) {
                        Box {
                            Icon(Icons.Filled.Notifications, null, tint = Apricot)
                            Box(Modifier.size(8.dp).align(Alignment.TopEnd).clip(CircleShape).background(Terracotta))
                        }
                    }
                }
            }
        }

        // ===== 场景主视觉：绘本成长册 + 家庭学习角 =====
        item {
            Box(
                Modifier.padding(horizontal = 16.dp).height(200.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                AppImage(ImageRef.of("img_scene_home"), Modifier.fillMaxSize())
                Column(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth()
                        .padding(14.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xF2FFFDF8))
                        .padding(14.dp)
                ) {
                    Text("$greeting，${profile?.parentName?.ifBlank { "你好" } ?: "你好"}",
                        style = MaterialTheme.typography.titleLarge, color = Ink, fontWeight = FontWeight.Bold)
                    Text(
                        listOfNotNull(
                            profile?.childName?.ifBlank { null },
                            ageLabel(profile?.childBirthday ?: "").ifBlank { null },
                            profile?.city?.ifBlank { null }
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall, color = InkSoft
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProgressRing(
                            progress = rate / 100f, size = 52.dp, stroke = 6.dp, color = LeafGreen
                        ) {
                            Text("$rate%", style = MaterialTheme.typography.labelMedium, color = LeafGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("今日已完成 $done / $total 件事",
                                style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.SemiBold)
                            Text(if (done >= total && total > 0) "今天的小目标都完成啦" else "慢慢来，一件一件来",
                                style = MaterialTheme.typography.labelSmall, color = InkSoft)
                        }
                        Button(
                            onClick = { nav(Route.Edit(null, null)) },
                            colors = ButtonDefaults.buttonColors(containerColor = Apricot),
                            shape = RoundedCornerShape(50.dp)
                        ) { Text("记一笔") }
                    }
                }
            }
        }

        // ===== 今日优先 =====
        item {
            SectionTitle(
                "今日优先", actionText = "全部",
                onAction = { nav(Route.Records) },
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp)
            )
        }
        if (todayPriority.isEmpty()) {
            item {
                Text("今天没有待办，去记一笔吧", Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium, color = InkFaint)
            }
        } else {
            items(todayPriority, key = { it.id }) { rec ->
                PriorityRow(rec, onClick = { nav(Route.Detail(rec.id)) },
                    onToggle = {
                        val target = if (rec.status == RecordStatus.DONE.id) RecordStatus.DOING.id else RecordStatus.DONE.id
                        appState.setStatus(rec.id, target)
                    })
            }
        }

        // ===== 核心数据 =====
        item {
            SectionTitle("核心数据", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp))
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                // 大数字 + 近 7 天趋势
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(PaperWarm).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1.2f)) {
                        Text("本周完成", style = MaterialTheme.typography.labelMedium, color = InkSoft)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$weekDone", style = MaterialTheme.typography.headlineMedium, color = ApricotDeep, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Text("项", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                        }
                        Text(
                            if (weekDelta >= 0) "较上周 +$weekDelta" else "较上周 $weekDelta",
                            style = MaterialTheme.typography.labelSmall, color = LeafGreen
                        )
                    }
                    Column(Modifier.weight(1.4f)) {
                        Text("近 7 天完成趋势", style = MaterialTheme.typography.labelSmall, color = InkFaint)
                        Spacer(Modifier.height(6.dp))
                        val bars = appState.dailyDone(7)
                        MiniBars(bars.map { it.second })
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SmallStat(
                        "连续早睡", "${streak} 天", "今晚 21:30 入睡",
                        Modifier.weight(1f), icon = Icons.Filled.Spa, tint = Sage
                    )
                    SmallStat(
                        "成长里程碑", "${milestoneCount} 个", "每一刻都值得纪念",
                        Modifier.weight(1f), icon = Icons.Filled.ChevronRight, tint = Terracotta,
                        onClick = { nav(Route.Insights) }
                    )
                }
            }
        }

        // ===== 快捷操作 =====
        item {
            SectionTitle("记一笔", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp))
        }
        item {
            QuickActionsGrid { type -> nav(Route.Edit(null, type.id)) }
        }

        // ===== 最近内容 =====
        item {
            SectionTitle("最近内容", actionText = "查看全部",
                onAction = { nav(Route.Records) },
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp))
        }
        item {
            LazyRow(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recent, key = { it.id }) { rec ->
                    RecentCard(rec) { nav(Route.Detail(rec.id)) }
                }
            }
        }

        // ===== 阶段成果 + 成长树 =====
        item {
            SectionTitle("阶段成果", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp))
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GrowthTreeCard(leaves, Modifier.weight(1f)) { nav(Route.Insights) }
                    MilestoneCard(milestoneCount, Modifier.weight(1f)) { nav(Route.Records) }
                }
            }
        }
    }
}

@Composable
private fun PriorityRow(rec: Record, onClick: () -> Unit, onToggle: () -> Unit) {
    val type = rec.typeEnum()
    val isDone = rec.status == RecordStatus.DONE.id
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp)).background(Paper)
            .clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(type.tint().copy(alpha = 0.14f))
                .clickable { onToggle() }, contentAlignment = Alignment.Center
        ) {
            Icon(type.icon(), null, tint = if (isDone) LeafGreen else type.tint(), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(rec.title, style = MaterialTheme.typography.titleSmall, color = Ink,
                fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(rec.metricOrHint(), style = MaterialTheme.typography.labelSmall, color = InkSoft, maxLines = 1)
        }
        Text(rec.dateLabel(), style = MaterialTheme.typography.labelSmall, color = InkFaint)
    }
}

private fun Record.metricOrHint(): String = metric() ?: if (notes.isNotBlank()) notes else typeEnum().label

@Composable
private fun SmallStat(
    title: String, value: String, hint: String, modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: (() -> Unit)? = null
) {
    val m = modifier.clip(RoundedCornerShape(18.dp)).background(Paper)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(14.dp)
    Column(m) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = InkSoft)
            Spacer(Modifier.weight(1f))
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
        Text(hint, style = MaterialTheme.typography.labelSmall, color = InkFaint)
    }
}

@Composable
private fun QuickActionsGrid(onSelect: (RecordType) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        RecordType.all().chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { type ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Paper)
                            .clickable { onSelect(type) }.padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(type.tint().copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center) {
                            Icon(type.icon(), null, tint = type.tint(), modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(type.label, style = MaterialTheme.typography.labelMedium, color = Ink)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentCard(rec: Record, onClick: () -> Unit) {
    Column(
        Modifier.width(140.dp).clip(RoundedCornerShape(16.dp)).background(Paper)
            .clickable { onClick() }.padding(8.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(12.dp))) {
            val cover = rec.cover()
            if (cover != null) AppImage(cover, Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(rec.typeEnum().tint().copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                Icon(rec.typeEnum().icon(), null, tint = rec.typeEnum().tint(), modifier = Modifier.size(26.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(rec.title, style = MaterialTheme.typography.labelMedium, color = Ink,
            fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(rec.dateLabel() + " · " + rec.statusEnum().label,
            style = MaterialTheme.typography.labelSmall, color = InkFaint, maxLines = 1)
    }
}

@Composable
private fun GrowthTreeCard(leaves: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(LeafGreenSoft).clickable { onClick() }.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Spa, null, tint = LeafGreen, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("成长树", style = MaterialTheme.typography.labelMedium, color = LeafGreen, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        Text("$leaves", style = MaterialTheme.typography.headlineMedium, color = LeafGreen, fontWeight = FontWeight.Bold)
        Text("片叶子 · 每完成一次亲子活动就长一片",
            style = MaterialTheme.typography.labelSmall, color = InkSoft)
    }
}

@Composable
private fun MilestoneCard(count: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(ApricotSoft).clickable { onClick() }.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.ChevronRight, null, tint = ApricotDeep, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("里程碑", style = MaterialTheme.typography.labelMedium, color = ApricotDeep, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        Text("$count", style = MaterialTheme.typography.headlineMedium, color = ApricotDeep, fontWeight = FontWeight.Bold)
        Text("个值得纪念的第一次", style = MaterialTheme.typography.labelSmall, color = InkSoft)
    }
}
