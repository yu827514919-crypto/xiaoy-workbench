package com.example.xiaoy.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.AppState
import com.example.xiaoy.data.AudioPlayer
import com.example.xiaoy.data.AudioRecorder
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
import com.example.xiaoy.ui.components.drawableRes
import com.example.xiaoy.ui.navigation.Route
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.ApricotSoft
import com.example.xiaoy.ui.theme.Cream
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.Paper
import com.example.xiaoy.ui.theme.Sage
import com.example.xiaoy.ui.theme.Terracotta
import com.example.xiaoy.ui.theme.TerracottaSoft
import com.example.xiaoy.ui.tint
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    // —— 录音 ——
    val recorder = remember { AudioRecorder() }
    val player = remember { AudioPlayer() }
    var recording by remember { mutableStateOf(false) }
    var recordSeconds by remember { mutableStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var showDeleteAudio by remember { mutableStateOf(false) }

    fun startRecording() {
        val file = File(appState.audioDir(), "rec_${System.currentTimeMillis()}.m4a")
        val ok = recorder.start(file)
        if (ok != null) {
            recording = true
            recordSeconds = 0
            scope.launch {
                while (recorder.isRecording) { delay(1000); recordSeconds++ }
                recording = false
            }
        } else {
            scope.launch { snackbar.showSnackbar("无法开始录音，请检查麦克风权限") }
        }
    }

    fun stopRecording() {
        val f = recorder.stop()
        recording = false
        if (f != null && f.length() > 1000) {
            appState.setRecordAudio(record.id, f.absolutePath)
            scope.launch { snackbar.showSnackbar("声音已保存进这条记录") }
        } else {
            f?.delete()
            scope.launch { snackbar.showSnackbar("录音太短，没有保存") }
        }
    }

    fun togglePlay() {
        val path = record.audioPath ?: return
        playing = player.toggle(path)
    }

    val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startRecording()
        else scope.launch { snackbar.showSnackbar("需要麦克风权限才能录音") }
    }

    fun requestRecord() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        } else {
            audioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

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
    val typeTintArgb = type.tint().toArgb()

    var sharingCard by remember { mutableStateOf(false) }
    fun generateShareCard() {
        scope.launch {
            sharingCard = true
            val file = withContext(Dispatchers.IO) { buildShareCard(context, record, typeTintArgb) }
            sharingCard = false
            if (file != null) shareImage(context, file)
            else snackbar.showSnackbar("生成卡片失败，请重试")
        }
    }

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

        // 补照片 + 分享成长卡
        item {
            Row(Modifier.padding(horizontal = 16.dp).padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { showAddPhoto = true },
                    enabled = !addingPhoto,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Apricot),
                    shape = RoundedCornerShape(50.dp), modifier = Modifier.weight(1f)
                ) {
                    if (addingPhoto) {
                        CircularProgressIndicator(color = Apricot, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Filled.AddAPhoto, null, tint = Apricot, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(if (addingPhoto) "保存中…" else "补一张照片")
                }
                OutlinedButton(
                    onClick = { generateShareCard() },
                    enabled = !sharingCard,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Sage),
                    shape = RoundedCornerShape(50.dp), modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Share, null, tint = Sage, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (sharingCard) "生成中…" else "分享成长卡")
                }
            }
        }

        // 声音记录
        item {
            SectionTitle("声音记录", modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp))
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(16.dp)).background(Paper).padding(14.dp)) {
                val audio = record.audioPath
                val hasAudio = audio != null && File(audio).exists()
                when {
                    recording -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(TerracottaSoft), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Mic, null, tint = Terracotta, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("正在录音 ${recordSeconds}s", style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.SemiBold)
                                Text("把这一刻的声音温柔留住", style = MaterialTheme.typography.labelSmall, color = InkSoft)
                            }
                            Button(onClick = { stopRecording() }, colors = ButtonDefaults.buttonColors(containerColor = Terracotta), shape = RoundedCornerShape(50.dp)) { Text("停止") }
                        }
                    }
                    hasAudio -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(ApricotSoft).clickable { togglePlay() }, contentAlignment = Alignment.Center) {
                                Icon(if (playing) Icons.Filled.Stop else Icons.Filled.PlayArrow, null, tint = Apricot, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(if (playing) "正在播放…" else "一段孩子的录音", style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.SemiBold)
                                Text("点击左侧按钮播放或暂停", style = MaterialTheme.typography.labelSmall, color = InkSoft)
                            }
                            IconButton(onClick = { showDeleteAudio = true }) { Icon(Icons.Filled.Delete, null, tint = Terracotta) }
                        }
                    }
                    else -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(ApricotSoft), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Mic, null, tint = Apricot, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("录一段声音", style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.SemiBold)
                                Text("第一次叫妈妈、唱儿歌、背古诗…", style = MaterialTheme.typography.labelSmall, color = InkSoft)
                            }
                            Button(onClick = { requestRecord() }, colors = ButtonDefaults.buttonColors(containerColor = Apricot), shape = RoundedCornerShape(50.dp)) { Text("录音") }
                        }
                    }
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

    if (showDeleteAudio) {
        ConfirmDialog(
            title = "删除这段录音？",
            message = "删除后无法恢复，这条记录里将不再有声音。",
            confirmText = "确认删除",
            onConfirm = {
                player.stop()
                playing = false
                record.audioPath?.let { File(it).delete() }
                appState.setRecordAudio(record.id, null)
                showDeleteAudio = false
                scope.launch { snackbar.showSnackbar("录音已删除") }
            },
            onDismiss = { showDeleteAudio = false }
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
            containerColor = Paper
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

private fun loadCoverBitmap(context: android.content.Context, ref: String): Bitmap? = try {
    val (kind, value) = ImageRef.parts(ref)
    if (kind == ImageRef.DRAWABLE) {
        val resId = drawableRes(value) ?: return null
        BitmapFactory.decodeResource(context.resources, resId)
    } else {
        BitmapFactory.decodeFile(value)
    }
} catch (_: Exception) { null }

private fun buildShareCard(context: android.content.Context, record: Record, tintArgb: Int): File? = try {
    val w = 1080
    val h = 1440
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    canvas.drawColor(android.graphics.Color.parseColor("#FFF8EE"))

    var topY = 90
    val coverBmp = record.cover()?.let { loadCoverBitmap(context, it) }
    if (coverBmp != null) {
        canvas.drawBitmap(coverBmp, Rect(0, 0, coverBmp.width, coverBmp.height), Rect(0, 0, w, 620), paint)
        topY = 620
    }

    paint.color = tintArgb
    paint.textSize = 40f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(record.typeEnum().label, 60f, (topY + 100).toFloat(), paint)

    paint.color = android.graphics.Color.parseColor("#3A2E24")
    paint.textSize = 72f
    canvas.drawText(record.title, 60f, (topY + 190).toFloat(), paint)

    paint.color = android.graphics.Color.parseColor("#8A7B6C")
    paint.textSize = 40f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText(formatFullDate(record.dateEpoch), 60f, (topY + 260).toFloat(), paint)

    if (record.notes.isNotBlank()) {
        paint.color = android.graphics.Color.parseColor("#6B5D50")
        paint.textSize = 42f
        val note = if (record.notes.length > 30) record.notes.take(30) + "…" else record.notes
        canvas.drawText(note, 60f, (topY + 340).toFloat(), paint)
    }

    paint.color = android.graphics.Color.parseColor("#C98736")
    paint.textSize = 44f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("小芽 · 记录成长", 60f, (h - 90).toFloat(), paint)

    val dir = File(context.cacheDir, "share").apply { mkdirs() }
    val file = File(dir, "share_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    bmp.recycle()
    file
} catch (_: Exception) { null }

private fun shareImage(context: android.content.Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享成长卡"))
    } catch (_: Exception) { }
}
