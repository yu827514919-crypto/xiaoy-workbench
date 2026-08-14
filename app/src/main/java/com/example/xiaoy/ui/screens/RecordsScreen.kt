package com.example.xiaoy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.AppState
import com.example.xiaoy.data.Record
import com.example.xiaoy.data.RecordStatus
import com.example.xiaoy.data.RecordType
import com.example.xiaoy.data.startOfThisMonth
import com.example.xiaoy.data.startOfThisWeek
import com.example.xiaoy.ui.components.EmptyState
import com.example.xiaoy.ui.components.LocalSnackbar
import com.example.xiaoy.ui.components.RecordCard
import com.example.xiaoy.ui.components.TagChip
import com.example.xiaoy.ui.navigation.Route
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.Cream
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkFaint
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.LeafGreen
import com.example.xiaoy.ui.theme.Paper
import kotlinx.coroutines.launch

private val timeRanges = listOf("全部", "本周", "本月")

@Composable
fun RecordsScreen(appState: AppState, nav: (Route) -> Unit, back: () -> Unit) {
    val data by appState.data.collectAsState()
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var timeRange by remember { mutableStateOf("全部") }
    var sortByValue by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(true) }
    var showSearch by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val filtered = remember(data.records, query, selectedType, selectedStatus, selectedTag, timeRange, sortByValue) {
        var list = data.records.filter { rec ->
            val q = query.trim()
            val matchQ = q.isEmpty() || rec.title.contains(q) || rec.subtitle.contains(q) ||
                rec.notes.contains(q) || rec.tags.any { it.contains(q) }
            val matchType = selectedType == null || rec.type == selectedType
            val matchStatus = selectedStatus == null || rec.status == selectedStatus
            val matchTag = selectedTag == null || selectedTag in rec.tags
            val matchTime = when (timeRange) {
                "本周" -> rec.dateEpoch >= startOfThisWeek()
                "本月" -> rec.dateEpoch >= startOfThisMonth()
                else -> true
            }
            matchQ && matchType && matchStatus && matchTag && matchTime
        }
        list = if (sortByValue) list.sortedByDescending { it.num1 ?: 0.0 }
        else list.sortedByDescending { it.dateEpoch }
        list
    }

    val hasAnyFilter = query.isNotBlank() || selectedType != null || selectedStatus != null ||
        selectedTag != null || timeRange != "全部"

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // 标题
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("成长记录册", style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
                    Text("共 ${data.records.size} 条记录", style = MaterialTheme.typography.labelSmall, color = InkSoft)
                }
                IconButton(onClick = { showSearch = !showSearch }) {
                    Icon(Icons.Filled.Search, null, tint = if (showSearch) Apricot else InkSoft)
                }
                IconButton(onClick = { showFilters = !showFilters }) {
                    Text(if (showFilters) "筛选 ∧" else "筛选 ∨", style = MaterialTheme.typography.labelMedium, color = Apricot)
                }
            }

            if (showSearch) {
                TextField(
                    value = query, onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("搜索标题、书名、备注、标签…", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, null) }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Paper, unfocusedContainerColor = Paper,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(Modifier.size(8.dp))
            }

            if (showFilters) {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterRow {
                        TagChip("全部", selected = selectedType == null) { selectedType = null }
                        RecordType.all().forEach { t ->
                            TagChip(t.label, selected = selectedType == t.id) { selectedType = t.id }
                        }
                    }
                    FilterRow {
                        TagChip("全部状态", selected = selectedStatus == null) { selectedStatus = null }
                        RecordStatus.all().forEach { s ->
                            TagChip(s.label, selected = selectedStatus == s.id) { selectedStatus = s.id }
                        }
                    }
                    FilterRow {
                        TagChip("全部标签", selected = selectedTag == null) { selectedTag = null }
                        appState.allTags().forEach { tag ->
                            TagChip(tag, selected = selectedTag == tag) { selectedTag = tag }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        timeRanges.forEach { r ->
                            TagChip(r, selected = timeRange == r) { timeRange = r }
                        }
                        Spacer(Modifier.weight(1f))
                        TagChip(if (sortByValue) "按数值" else "按日期", selected = sortByValue) { sortByValue = !sortByValue }
                    }
                }
                Spacer(Modifier.size(8.dp))
            }

            if (filtered.isEmpty()) {
                EmptyState(
                    if (hasAnyFilter) "没有找到匹配的记录" else "记录册还是空的",
                    if (hasAnyFilter) "换个关键词或筛选条件试试看" else "从第一条开始，记下孩子的成长",
                    actionText = if (hasAnyFilter) "清除筛选" else "记一笔",
                    onAction = {
                        if (hasAnyFilter) {
                            query = ""; selectedType = null; selectedStatus = null; selectedTag = null; timeRange = "全部"
                        } else nav(Route.Edit(null, null))
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filtered, key = { it.id }) { rec ->
                        RecordCard(rec, onClick = { nav(Route.Detail(rec.id)) },
                            onToggleDone = {
                                val target = if (rec.status == RecordStatus.DONE.id) RecordStatus.DOING.id else RecordStatus.DONE.id
                                appState.setStatus(rec.id, target)
                                if (target == RecordStatus.DONE.id) {
                                    scope.launch { snackbar.showSnackbar("已完成「${rec.title}」") }
                                }
                            })
                    }
                }
            }
        }

        // 回到顶部
        if (listState.firstVisibleItemIndex > 8) {
            IconButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 104.dp)
                    .size(40.dp).clip(CircleShape).background(Paper)
            ) {
                Icon(Icons.Filled.ArrowUpward, null, tint = InkSoft)
            }
        }

        // 新增
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
private fun FilterRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}
