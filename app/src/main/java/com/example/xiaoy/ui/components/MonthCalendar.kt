package com.example.xiaoy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.firstDayOfMonth
import com.example.xiaoy.data.isSameDay
import com.example.xiaoy.data.startOfToday
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkFaint
import com.example.xiaoy.ui.theme.LeafGreen
import java.util.Calendar

private const val DAY_MS = 24L * 3600 * 1000

/**
 * 月历（周一起始）。day 格带「有记录」圆点标记，选中格用杏黄圆底高亮。
 */
@Composable
fun MonthCalendar(
    year: Int,
    month: Int,          // 0-based
    selectedDay: Long?,
    hasRecord: (Long) -> Boolean,
    onSelectDay: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance().apply { set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDow = cal.get(Calendar.DAY_OF_WEEK) // 1=周日
    val leading = (firstDow + 5) % 7              // 周一起始的前置空格数
    val today = startOfToday()
    val monthStart = firstDayOfMonth(year, month)

    val cells = mutableListOf<Int?>()
    repeat(leading) { cells.add(null) }
    for (d in 1..daysInMonth) cells.add(d)
    while (cells.size % 7 != 0) cells.add(null)

    Column(modifier) {
        Row(Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach {
                Text(it, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall, color = InkFaint)
            }
        }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (day != null) {
                            val epoch = monthStart + (day - 1) * DAY_MS
                            val selected = selectedDay != null && isSameDay(epoch, selectedDay)
                            val isToday = isSameDay(epoch, today)
                            Column(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Apricot else Color.Transparent)
                                    .clickable { onSelectDay(epoch) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    day.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when {
                                        selected -> Color.White
                                        isToday -> Apricot
                                        else -> Ink
                                    },
                                    fontWeight = if (selected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                                Box(
                                    Modifier.size(4.dp).clip(CircleShape)
                                        .background(if (selected) Color.White else if (hasRecord(epoch)) LeafGreen else Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
