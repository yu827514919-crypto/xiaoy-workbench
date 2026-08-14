package com.example.xiaoy.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.AppState
import com.example.xiaoy.data.RecordStatus
import com.example.xiaoy.data.RecordType
import com.example.xiaoy.data.formatFullDate
import com.example.xiaoy.ui.components.RecordCard
import com.example.xiaoy.ui.navigation.Route
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.Cream
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.LeafGreen
import com.example.xiaoy.ui.theme.Paper
import com.example.xiaoy.ui.theme.PaperWarm

@Composable
fun ReportScreen(appState: AppState, type: String, back: () -> Unit) {
    val data by appState.data.collectAsState()
    val context = LocalContext.current
    val t = RecordType.fromId(type)
    val recs = data.records.filter { it.type == type }.sortedByDescending { it.dateEpoch }
    val done = recs.count { it.status == RecordStatus.DONE.id }
    val rate = if (recs.isEmpty()) 0 else done * 100 / recs.size
    val newest = recs.firstOrNull()
    val oldest = recs.lastOrNull()

    val reportText = buildString {
        append("【小芽 · ${t.label}阶段报告】\n")
        append("共 ${recs.size} 条记录，已完成 $done 条，完成率 $rate%。\n")
        if (oldest != null && newest != null) {
            append("记录跨度：${formatFullDate(oldest.dateEpoch)} — ${formatFullDate(newest.dateEpoch)}\n")
        }
        recs.take(10).forEachIndexed { i, r ->
            append("${i + 1}. ${r.title}（${r.statusEnum().label}）\n")
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().background(Cream).statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = back) { Icon(Icons.Filled.ArrowBack, null, tint = Ink) }
                Text("阶段报告", style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply { setType("text/plain"); putExtra(Intent.EXTRA_TEXT, reportText) }
                    context.startActivity(Intent.createChooser(intent, "分享报告"))
                }) { Icon(Icons.Filled.Share, null, tint = Apricot) }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
                .clip(RoundedCornerShape(20.dp)).background(PaperWarm).padding(18.dp)) {
                Text("${t.label} · 阶段小结", style = MaterialTheme.typography.titleLarge, color = Ink, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(10.dp))
                Row {
                    ReportStat("${recs.size}", "条记录")
                    ReportStat("$done", "已完成")
                    ReportStat("$rate%", "完成率")
                }
                Spacer(Modifier.size(12.dp))
                Text(narrative(t, recs.size, done, newest?.title), style = MaterialTheme.typography.bodyMedium, color = InkSoft)
            }
        }

        item {
            Text("记录明细", Modifier.padding(horizontal = 16.dp).padding(top = 20.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.Bold)
        }
        if (recs.isEmpty()) {
            item {
                Text("这一类型还没有记录", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyMedium, color = InkSoft)
            }
        } else {
            items(recs, key = { it.id }) { r ->
                Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    RecordCard(r, onClick = { })
                }
            }
        }

        item {
            Button(
                onClick = { back() },
                colors = ButtonDefaults.buttonColors(containerColor = Apricot),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp)
            ) { Text("返回") }
        }
    }
}

private fun narrative(t: RecordType, total: Int, done: Int, newestTitle: String?): String = when {
    total == 0 -> "还没有记录，从今天开始写下第一条吧。"
    t == RecordType.GROWTH -> "这段时间记录了 $total 个成长瞬间，其中完成了 $done 个。${newestTitle?.let { "最近一次是「$it」，这些第一次都值得被珍藏。" } ?: ""}"
    t == RecordType.READING -> "一共读了 $total 次书，$done 次已读完整。坚持共读，是送给孩子最温柔的陪伴。"
    t == RecordType.BODY -> "记录了 $total 次身高体重，$done 次已归档。定期测量，见证一点一点长高。"
    else -> "共 $total 条记录，已完成 $done 条。每一笔都是你们一起走过的日子。"
}

@Composable
private fun RowScope.ReportStat(value: String, label: String) {
    Column(Modifier.weight(1f)) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = LeafGreen, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = InkSoft)
    }
}
