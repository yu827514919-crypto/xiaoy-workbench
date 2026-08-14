package com.example.xiaoy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.AppState
import com.example.xiaoy.data.formatFullDate
import com.example.xiaoy.ui.components.AppImage
import com.example.xiaoy.ui.navigation.Route
import com.example.xiaoy.ui.theme.Apricot

@Composable
fun ImageViewScreen(appState: AppState, recordId: String, index: Int, back: () -> Unit) {
    val record = appState.recordById(recordId)
    LaunchedEffect(recordId, record?.images?.size) {
        if (record == null || record.images.isEmpty()) back()
    }
    if (record == null || record.images.isEmpty()) {
        Box(Modifier.fillMaxSize().background(Color(0xFF1E1B16)))
        return
    }

    var idx by remember { mutableIntStateOf(index.coerceIn(0, record.images.size - 1)) }

    Box(Modifier.fillMaxSize().background(Color(0xFF1E1B16))) {
        AppImage(record.images[idx], Modifier.fillMaxSize(), contentScale = ContentScale.Fit)

        // 顶部
        Row(
            Modifier.align(Alignment.TopStart).fillMaxWidth().statusBarsPadding().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = back) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) }
            Spacer(Modifier.width(4.dp))
            Text(record.title, style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 1)
        }

        // 左右切换
        if (record.images.size > 1) {
            if (idx > 0) {
                IconButton(
                    onClick = { idx-- },
                    modifier = Modifier.align(Alignment.CenterStart).size(44.dp)
                ) { Icon(Icons.Filled.ChevronLeft, null, tint = Color.White, modifier = Modifier.size(32.dp)) }
            }
            if (idx < record.images.size - 1) {
                IconButton(
                    onClick = { idx++ },
                    modifier = Modifier.align(Alignment.CenterEnd).size(44.dp)
                ) { Icon(Icons.Filled.ChevronRight, null, tint = Color.White, modifier = Modifier.size(32.dp)) }
            }
        }

        // 底部
        Column(
            Modifier.align(Alignment.BottomStart).fillMaxWidth().navigationBarsPadding().padding(16.dp)
        ) {
            Text(formatFullDate(record.dateEpoch) + " · ${idx + 1}/${record.images.size}",
                style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.size(8.dp))
            Button(
                onClick = { back() },
                colors = ButtonDefaults.buttonColors(containerColor = Apricot),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("查看这条记录") }
        }
    }
}
