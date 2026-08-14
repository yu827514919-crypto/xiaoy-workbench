package com.example.xiaoy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.AppState
import com.example.xiaoy.data.Record
import com.example.xiaoy.data.formatDate
import com.example.xiaoy.data.monthKey
import com.example.xiaoy.ui.components.AppImage
import com.example.xiaoy.ui.components.EmptyState
import com.example.xiaoy.ui.navigation.Route
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.Paper
import java.text.SimpleDateFormat
import java.util.Locale

private data class GalleryItem(val record: Record, val index: Int, val ref: String)

@Composable
fun GalleryScreen(appState: AppState, nav: (Route) -> Unit, back: () -> Unit) {
    val data by appState.data.collectAsState()

    val all = data.records.flatMap { rec -> rec.images.mapIndexed { i, img -> GalleryItem(rec, i, img) } }
    val groups = all.groupBy { monthKey(it.record.dateEpoch) }
        .toSortedMap(compareByDescending { it })

    if (all.isEmpty()) {
        Box(Modifier.fillMaxSize().statusBarsPadding()) {
            Column {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("成长相册", style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
                }
                EmptyState("相册还是空的", "记录时上传照片，就会出现在这里", actionText = "去记一笔") { nav(Route.Edit(null, null)) }
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.padding(vertical = 4.dp)) {
                Text("成长相册", style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
                Text("共 ${all.size} 张照片 · 点开可查看大图和关联记录", style = MaterialTheme.typography.labelSmall, color = InkSoft)
            }
        }

        groups.forEach { (month, items) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(monthTitle(month), Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
            }
            items(items, key = { it.record.id + "_" + it.index }) { gi ->
                GalleryTile(gi) { nav(Route.ImageView(gi.record.id, gi.index)) }
            }
        }
    }
}

private fun monthTitle(month: String): String = try {
    val y = month.substring(0, 4).toInt()
    val m = month.substring(5, 7).toInt()
    "${y}年${m}月"
} catch (_: Exception) { month }

@Composable
private fun GalleryTile(item: GalleryItem, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(14.dp)).background(Paper)
            .clickable { onClick() }.padding(6.dp)
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(10.dp))) {
            AppImage(item.ref, Modifier.fillMaxSize())
        }
        Spacer(Modifier.size(6.dp))
        Text(item.record.title, style = MaterialTheme.typography.labelMedium, color = Ink,
            fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(formatDate(item.record.dateEpoch) + " · " + item.record.typeEnum().label,
            style = MaterialTheme.typography.labelSmall, color = InkSoft, maxLines = 1)
    }
}
