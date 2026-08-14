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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.AppState
import com.example.xiaoy.data.RecordStatus
import com.example.xiaoy.data.formatDate
import com.example.xiaoy.data.formatDateWithWeekday
import com.example.xiaoy.data.isSameDay
import com.example.xiaoy.data.monthLabel
import com.example.xiaoy.data.startOfToday
import com.example.xiaoy.ui.components.EmptyState
import com.example.xiaoy.ui.components.MonthCalendar
import com.example.xiaoy.ui.components.RecordCard
import com.example.xiaoy.ui.components.SectionTitle
import com.example.xiaoy.ui.navigation.Route
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.Paper
import com.example.xiaoy.ui.theme.Terracotta
import com.example.xiaoy.ui.tint
import java.util.Calendar

@Composable
fun PlanScreen(appState: AppState, nav: (Route) -> Unit) {
    val data by appState.data.collectAsState()
    val today = startOfToday()
    val c = Calendar.getInstance()
    var monthEpoch by remember { mutableLongStateOf(today) }
    var selectedDay by remember { mutableLongStateOf(today) }

    val cal = Calendar.getInstance().apply { timeInMillis = monthEpoch }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)

    val dayRecords = appState.recordsOn(selectedDay)
    val upcoming = appState.upcomingReminders(5)

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("成长日历", style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    TextButtonText("回到今天") { selectedDay = today; monthEpoch = today }
                }
            }

            item {
                Column(
                    Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(Paper).padding(14.dp)
                ) {
                    // 月份导航
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val cc = Calendar.getInstance().apply { timeInMillis = monthEpoch; add(Calendar.MONTH, -1) }
                            monthEpoch = cc.timeInMillis
                        }) { Icon(Icons.Filled.ChevronLeft, null, tint = InkSoft) }
                        Text(
                            monthLabel(monthEpoch),
                            Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            val cc = Calendar.getInstance().apply { timeInMillis = monthEpoch; add(Calendar.MONTH, 1) }
                            monthEpoch = cc.timeInMillis
                        }) { Icon(Icons.Filled.ChevronRight, null, tint = InkSoft) }
                    }
                    Spacer(Modifier.size(6.dp))
                    MonthCalendar(
                        year = year, month = month, selectedDay = selectedDay,
                        hasRecord = { appState.recordsOn(it).isNotEmpty() },
                        onSelectDay = { selectedDay = it }
                    )
                }
            }

            item {
                SectionTitle(
                    "${formatDateWithWeekday(selectedDay)} · ${dayRecords.size} 条记录",
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp)
                )
            }

            if (dayRecords.isEmpty()) {
                item {
                    EmptyState("这一天还没有记录", "点右下角记一笔，或回到有记录的日子看看",
                        actionText = "记一笔") { nav(Route.Edit(null, null)) }
                }
            } else {
                items(dayRecords, key = { it.id }) { rec ->
                    TimelineRow(rec, onClick = { nav(Route.Detail(rec.id)) },
                        onToggleDone = {
                            val target = if (rec.status == RecordStatus.DONE.id) RecordStatus.DOING.id else RecordStatus.DONE.id
                            appState.setStatus(rec.id, target)
                        })
                }
            }

            item {
                SectionTitle("即将提醒", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp))
            }
            if (upcoming.isEmpty()) {
                item {
                    Text("没有待提醒的事项", Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                }
            } else {
                items(upcoming, key = { it.id }) { rec ->
                    ReminderRow(rec) { nav(Route.Detail(rec.id)) }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { nav(Route.Edit(null, null)) },
            icon = { Icon(Icons.Filled.Add, null) },
            text = { Text("记一笔") },
            containerColor = Apricot, contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}

@Composable
private fun TextButtonText(text: String, onClick: () -> Unit) {
    Text(text, Modifier.clickable { onClick() }.padding(8.dp),
        style = MaterialTheme.typography.labelMedium, color = Apricot)
}

@Composable
private fun TimelineRow(rec: com.example.xiaoy.data.Record, onClick: () -> Unit, onToggleDone: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(rec.typeEnum().tint()))
            Box(Modifier.width(2.dp).weight(1f).background(Color(0xFFE8E0D2)))
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f)) {
            RecordCard(rec, onClick = onClick, onToggleDone = onToggleDone)
        }
    }
}

@Composable
private fun ReminderRow(rec: com.example.xiaoy.data.Record, onClick: () -> Unit) {
    val end = rec.endDateEpoch ?: rec.dateEpoch
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp)).background(Paper)
            .clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Event, null, tint = Terracotta, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(rec.title, style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(rec.location.ifBlank { rec.typeEnum().label }, style = MaterialTheme.typography.labelSmall, color = InkSoft, maxLines = 1)
        }
        Text(if (isSameDay(end, startOfToday())) "今天" else formatDate(end),
            style = MaterialTheme.typography.labelMedium, color = Apricot, fontWeight = FontWeight.SemiBold)
    }
}
