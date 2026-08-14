package com.example.xiaoy.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.AppState
import com.example.xiaoy.data.ImageRef
import com.example.xiaoy.data.Record
import com.example.xiaoy.data.RecordStatus
import com.example.xiaoy.data.RecordType
import com.example.xiaoy.data.formatDateWithWeekday
import com.example.xiaoy.data.formatFullDate
import com.example.xiaoy.ui.components.AppImage
import com.example.xiaoy.ui.components.ConfirmDialog
import com.example.xiaoy.ui.components.LocalSnackbar
import com.example.xiaoy.ui.components.RecordCard
import com.example.xiaoy.ui.components.SectionTitle
import com.example.xiaoy.ui.components.StatusBadge
import com.example.xiaoy.ui.components.TagChip
import com.example.xiaoy.ui.navigation.Route
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.Cream
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.Paper
import com.example.xiaoy.ui.theme.Terracotta
import com.example.xiaoy.ui.tint
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DetailScreen(appState: AppState, id: String, nav: (Route) -> Unit, back: () -> Unit) {
    val data by appState.data.collectAsState()
    val context = LocalContext.current
    val record = appState.recordById(id)
    var showDelete by remember { mutableStateOf(false) }

    if (record == null) {
        Box(Modifier.fillMaxSize().background(Cream).statusBarsPadding(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("这条记录已不存在", style = MaterialTheme.typography.titleMedium, color = Ink)
                Spacer(Modifier.size(12.dp))
                Button(onClick = back, colors = ButtonDefaults.buttonColors(containerColor = Apricot)) { Text("返回") }
            }
        }
        return
    }

    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var showAddPhoto by remember { mutableStateOf(false) }
    var addingPhoto by remember { mutableStateOf(false) }

    fun appendImage(uri: Uri) {
        addingPhoto = true
        scope.launch {
            val path = withContext(Dispatchers.IO) { appState.copyImageToStorage(uri) }
            addingPhoto = false
            if (path != null) {
                appState.addImageToRecord(record.id, path)
                snackbar.showSnackbar("照片已补充，第一张作为封面")
            } else {
                snackbar.showSnackbar("照片保存失败，请重试")
            }
        }
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) appendImage(uri)
    }

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingCameraUri
        pendingCameraUri = null
        if (success && uri != null) appendImage(uri)
    }

    fun launchCamera() {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "cam_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        pendingCameraUri = uri
        takePicture.launch(uri)
    }

    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera()
        else scope.launch { snackbar.showSnackbar("需要相机权限才能拍照") }
    }

    fun requestCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    val type = record.typeEnum()
    val related = data.records.filter { it.type == record.type && it.id != record.id }.take(3)
    val tagged = data.records.filter { it.id != record.id && it.tags.any { t -> t in record.tags } }.take(3)

    LazyColumn(
        Modifier.fillMaxSize().background(Cream).statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
    ) {
        // 顶栏
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = back) { Icon(Icons.Filled.ArrowBack, null, tint = Ink) }
                Text("记录详情", style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    val text = "【小芽】${record.title}\n类型：${type.label}\n日期：${formatFullDate(record.dateEpoch)}\n状态：${record.statusEnum().label}" +
                        (record.notes.ifBlank { "" }.let { if (it.isBlank()) "" else "\n备注：$it" })
                    val intent = Intent(Intent.ACTION_SEND).apply { setType("text/plain"); putExtra(Intent.EXTRA_TEXT, text) }
                    context.startActivity(Intent.createChooser(intent, "分享记录"))
                }) { Icon(Icons.Filled.Share, null, tint = InkSoft) }
                IconButton(onClick = { nav(Route.Edit(record.id, null)) }) { Icon(Icons.Filled.Edit, null, tint = Apricot) }
                IconButton(onClick = { showDelete = true }) { Icon(Icons.Filled.Delete, null, tint = Terracotta) }
            }
        }

        // 封面大图
        item {
            val cover = record.cover()
            if (cover != null) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(220.dp).clip(RoundedCornerShape(20.dp))) {
                    AppImage(cover, Modifier.fillMaxSize(), onClick = { nav(Route.ImageView(record.id, 0)) })
                }
                Spacer(Modifier.size(14.dp))
            }
        }

        // 标题区
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(type.label, style = MaterialTheme.typography.labelMedium, color = type.tint(), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    StatusBadge(record.statusEnum())
                }
                Spacer(Modifier.size(6.dp))
                Text(record.title, style = MaterialTheme.typography.headlineMedium, color = Ink, fontWeight = FontWeight.Bold)
                if (record.subtitle.isNotBlank()) {
                    Spacer(Modifier.size(4.dp))
                    Text(record.subtitle, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                }
            }
        }

        // 关键数值
        item {
            DetailMetric(record)
        }

        // 日期 / 地点
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                InfoLine(Icons.Filled.Event, formatDateWithWeekday(record.dateEpoch))
                record.endDateEpoch?.let { InfoLine(Icons.Filled.Event, "下次：${formatFullDate(it)}") }
                if (record.location.isNotBlank()) InfoLine(Icons.Filled.LocationOn, record.location)
            }
        }

        // 标签
        item {
            if (record.tags.isNotEmpty()) {
                Row(Modifier.padding(horizontal = 16.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    record.tags.forEach { TagChip(it) }
                }
            }
        }

        // 状态切换
        item {
            Column(Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)) {
                Text("标记状态", style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecordStatus.all().forEach { s ->
                        TagChip(s.label, selected = record.status == s.id) {
                            appState.setStatus(record.id, s.id)
                        }
                    }
                }
            }
        }

        // 备注
        item {
            if (record.notes.isNotBlank()) {
                Column(Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)
                    .clip(RoundedCornerShape(16.dp)).background(Paper).padding(14.dp)) {
                    Text("备注", style = MaterialTheme.typography.labelMedium, color = InkSoft)
                    Spacer(Modifier.size(4.dp))
                    Text(record.notes, style = MaterialTheme.typography.bodyMedium, color = Ink)
                }
            }
        }

        // 图片
        if (record.images.size > 1) {
            item {
                SectionTitle("照片（${record.images.size}）", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp))
            }
            item {
                Row(Modifier.padding(horizontal = 16.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    record.images.forEachIndexed { i, img ->
                        AppImage(img, Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)),
                            onClick = { nav(Route.ImageView(record.id, i)) })
                    }
                }
            }
        }

        // 关联记录
        if (related.isNotEmpty()) {
            item { SectionTitle("同类型记录", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)) }
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    related.forEach { r -> RecordCard(r, onClick = { nav(Route.Detail(r.id)) }) }
                }
            }
        }
        if (tagged.isNotEmpty()) {
            item { SectionTitle("相关活动", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)) }
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tagged.forEach { r -> RecordCard(r, onClick = { nav(Route.Detail(r.id)) }) }
                }
            }
        }

        // 补一张照片
        item {
            OutlinedButton(
                onClick = { showAddPhoto = true },
                enabled = !addingPhoto,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Apricot),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 20.dp)
            ) {
                if (addingPhoto) {
                    CircularProgressIndicator(color = Apricot, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("保存中…")
                } else {
                    Icon(Icons.Filled.AddAPhoto, null, tint = Apricot, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("补一张照片")
                }
            }
        }

        // 操作按钮
        item {
            Row(Modifier.padding(horizontal = 16.dp).padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { nav(Route.Edit(record.id, null)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Apricot),
                    shape = RoundedCornerShape(50.dp), modifier = Modifier.weight(1f)
                ) { Text("编辑") }
                OutlinedButton(
                    onClick = { showDelete = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Terracotta),
                    shape = RoundedCornerShape(50.dp), modifier = Modifier.weight(1f)
                ) { Text("删除") }
            }
        }
    }

    if (showDelete) {
        ConfirmDialog(
            title = "删除「${record.title}」？",
            message = "删除后，这条记录会从记录册、日历、统计和相册中一并移除，且无法恢复。",
            confirmText = "确认删除",
            onConfirm = {
                appState.deleteRecord(record.id)
                showDelete = false
                back()
            },
            onDismiss = { showDelete = false }
        )
    }

    if (showAddPhoto) {
        AlertDialog(
            onDismissRequest = { showAddPhoto = false },
            title = { Text("补一张照片", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("想用哪种方式记录这一刻？", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                    Spacer(Modifier.size(4.dp))
                    TextButton(onClick = { showAddPhoto = false; requestCamera() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.AddAPhoto, null, tint = Apricot, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("拍照", color = Ink, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { showAddPhoto = false; pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.PhotoLibrary, null, tint = Apricot, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("从相册选择", color = Ink)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddPhoto = false }) { Text("取消", color = InkSoft) }
            },
            containerColor = Color(0xFFFFFDF8)
        )
    }
}

@Composable
private fun InfoLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = InkSoft, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Ink)
    }
}

@Composable
private fun DetailMetric(record: Record) {
    val big: Pair<String, String>? = when (record.type) {
        RecordType.BODY.id -> record.num1?.let { String.format(Locale.CHINA, "%.1f", it) to "cm" }
        RecordType.READING.id -> record.num1?.let { it.toInt().toString() to "页" }
        RecordType.ROUTINE.id -> record.num1?.let { it.toInt().toString() to "连续天" }
        RecordType.ITEM.id -> record.num2?.let { it.toInt().toString() to "已备" }
        RecordType.STUDY.id -> record.num1?.let { it.toInt().toString() to "分钟" }
        else -> null
    }
    val sub = when (record.type) {
        RecordType.BODY.id -> record.num2?.let { String.format(Locale.CHINA, "%.1f kg", it) }
        RecordType.READING.id -> record.num2?.let { "${it.toInt()} 分钟" }
        RecordType.ITEM.id -> record.num1?.let { "共 ${it.toInt()} 项" }
        RecordType.ROUTINE.id -> record.text1.ifBlank { null }
        else -> null
    }
    if (big != null) {
        Row(
            Modifier.padding(horizontal = 16.dp).padding(top = 12.dp)
                .clip(RoundedCornerShape(16.dp)).background(Paper).padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(big.first, style = MaterialTheme.typography.headlineMedium, color = record.typeEnum().tint(), fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Text(big.second, style = MaterialTheme.typography.bodyMedium, color = InkSoft, modifier = Modifier.padding(bottom = 4.dp))
            Spacer(Modifier.weight(1f))
            sub?.let { Text(it, style = MaterialTheme.typography.titleSmall, color = Ink) }
        }
    }
}
