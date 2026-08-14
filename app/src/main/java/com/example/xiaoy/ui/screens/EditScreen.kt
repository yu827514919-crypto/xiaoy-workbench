package com.example.xiaoy.ui.screens

import android.Manifest
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.AppState
import com.example.xiaoy.data.ImageRef
import com.example.xiaoy.data.Record
import com.example.xiaoy.data.RecordStatus
import com.example.xiaoy.data.RecordType
import com.example.xiaoy.data.firstDayOfMonth
import com.example.xiaoy.data.formatFullDate
import com.example.xiaoy.data.startOfToday
import com.example.xiaoy.ui.components.AppImage
import com.example.xiaoy.ui.components.ConfirmDialog
import com.example.xiaoy.ui.components.LocalSnackbar
import com.example.xiaoy.ui.components.MonthCalendar
import com.example.xiaoy.ui.components.TagChip
import com.example.xiaoy.ui.icon
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.Cream
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.Paper
import com.example.xiaoy.ui.theme.PaperWarm
import com.example.xiaoy.ui.theme.Terracotta
import com.example.xiaoy.ui.tint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

@Composable
fun EditScreen(appState: AppState, id: String?, presetType: String?, back: () -> Unit) {
    val editing = id != null
    val record = id?.let { appState.recordById(it) }
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(if (editing || presetType != null) 1 else 0) }
    var type by remember { mutableStateOf(record?.type ?: presetType ?: "") }
    var title by remember { mutableStateOf(record?.title ?: "") }
    var subtitle by remember { mutableStateOf(record?.subtitle ?: "") }
    var dateEpoch by remember { mutableLongStateOf(record?.dateEpoch ?: startOfToday()) }
    var status by remember { mutableStateOf(record?.status ?: RecordStatus.DOING.id) }
    var tags by remember { mutableStateOf(record?.tags ?: emptyList()) }
    var notes by remember { mutableStateOf(record?.notes ?: "") }
    var num1 by remember { mutableStateOf(record?.num1?.let { if (it == it.toInt().toDouble()) it.toInt().toString() else it.toString() } ?: "") }
    var num2 by remember { mutableStateOf(record?.num2?.let { if (it == it.toInt().toDouble()) it.toInt().toString() else it.toString() } ?: "") }
    var text1 by remember { mutableStateOf(record?.text1 ?: "") }
    var location by remember { mutableStateOf(record?.location ?: "") }
    var endDate by remember { mutableLongStateOf(record?.endDateEpoch ?: startOfToday()) }
    var hasEndDate by remember { mutableStateOf(record?.endDateEpoch != null) }
    var images by remember { mutableStateOf(record?.images ?: emptyList()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uploading = true
            scope.launch {
                val path = withContext(Dispatchers.IO) { appState.copyImageToStorage(uri) }
                uploading = false
                if (path != null) {
                    images = images + ImageRef.ofFile(path)
                    snackbar.showSnackbar("图片已添加，第一张将作为封面")
                } else {
                    snackbar.showSnackbar("上传失败，请重试")
                }
            }
        }
    }

    // 拍照
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingCameraUri
        pendingCameraUri = null
        if (success && uri != null) {
            uploading = true
            scope.launch {
                val path = withContext(Dispatchers.IO) { appState.copyImageToStorage(uri) }
                uploading = false
                if (path != null) {
                    images = images + ImageRef.ofFile(path)
                    snackbar.showSnackbar("照片已添加，第一张将作为封面")
                } else {
                    snackbar.showSnackbar("照片保存失败，请重试")
                }
            }
        }
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

    var showSourceDialog by remember { mutableStateOf(false) }

    val selType = RecordType.fromId(type)

    fun save() {
        if (title.isBlank()) {
            scope.launch { snackbar.showSnackbar("先给这条记录起个标题吧") }
            return
        }
        val rec = Record(
            id = record?.id ?: appState.newId(),
            type = type,
            title = title.trim(),
            subtitle = subtitle.trim(),
            dateEpoch = dateEpoch,
            endDateEpoch = if (hasEndDate) endDate else null,
            status = status,
            tags = tags,
            notes = notes.trim(),
            images = images,
            num1 = num1.toDoubleOrNull(),
            num2 = num2.toDoubleOrNull(),
            text1 = text1.trim(),
            location = location.trim(),
            createdAt = record?.createdAt ?: System.currentTimeMillis()
        )
        if (editing) appState.updateRecord(rec) else appState.addRecord(rec)
        scope.launch { snackbar.showSnackbar(if (editing) "已保存修改" else "已记下「${rec.title}」") }
        back()
    }

    Column(Modifier.fillMaxSize().background(Cream).statusBarsPadding()) {
        // 顶栏
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = back) { Icon(Icons.Filled.ArrowBack, null, tint = Ink) }
            Column(Modifier.weight(1f)) {
                Text(if (editing) "编辑记录" else "记一笔", style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
                Text(stepTitle(step, editing), style = MaterialTheme.typography.labelSmall, color = InkSoft)
            }
            if (editing) {
                IconButton(onClick = { showDelete = true }) { Icon(Icons.Filled.Delete, null, tint = Terracotta) }
            }
        }

        // 步骤指示
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (0..2).forEach { i ->
                Box(
                    Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                        .background(if (i <= step) Apricot else Color(0xFFE8E0D2))
                )
            }
        }
        Spacer(Modifier.size(12.dp))

        // 内容
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            when (step) {
                0 -> TypeSelect(type) { type = it; step = 1 }
                1 -> FieldForm(
                    selType = selType, title = title, subtitle = subtitle, dateEpoch = dateEpoch,
                    status = status, tags = tags, notes = notes, num1 = num1, num2 = num2,
                    text1 = text1, location = location, hasEndDate = hasEndDate, endDate = endDate,
                    showMore = showMore, allTags = appState.allTags(),
                    onTitle = { title = it }, onSubtitle = { subtitle = it },
                    onDate = { dateEpoch = it }, onStatus = { status = it },
                    onTag = { t -> tags = if (t in tags) tags - t else tags + t },
                    onNotes = { notes = it }, onNum1 = { num1 = it }, onNum2 = { num2 = it },
                    onText1 = { text1 = it }, onLocation = { location = it },
                    onEndDate = { endDate = it }, onHasEndDate = { hasEndDate = it },
                    onShowMore = { showMore = it }, onPickDate = { pickingEnd = false; showDatePicker = true },
                    onPickEndDate = { pickingEnd = true; showDatePicker = true }
                )
                2 -> MediaStep(
                    images = images, uploading = uploading,
                    onAdd = { showSourceDialog = true },
                    onRemove = { i -> images = images.filterIndexed { idx, _ -> idx != i } }
                )
            }
            Spacer(Modifier.size(20.dp))
        }

        // 底部按钮
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (step > 0) {
                OutlinedButton(
                    onClick = { if (step == 1 && !editing) step = 0 else back() },
                    shape = RoundedCornerShape(50.dp), modifier = Modifier.weight(1f)
                ) { Text("上一步") }
            }
            Button(
                onClick = {
                    when (step) {
                        0 -> { if (type.isNotBlank()) step = 1 }
                        1 -> { if (title.isNotBlank()) step = 2 }
                        2 -> save()
                    }
                },
                enabled = when (step) {
                    0 -> type.isNotBlank()
                    1 -> title.isNotBlank()
                    else -> true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Apricot, disabledContainerColor = Apricot.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.weight(if (step > 0) 1f else 2f)
            ) {
                Text(when (step) { 0 -> "选好了"; 1 -> "下一步"; else -> if (editing) "保存修改" else "完成记录" })
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            dateEpoch,
            onSelect = { if (pickingEnd) endDate = it else dateEpoch = it; showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showDelete) {
        ConfirmDialog(
            title = "删除「${record?.title ?: ""}」？",
            message = "删除后，这条记录会从记录册、日历、统计和相册中一并移除，且无法恢复。",
            confirmText = "确认删除",
            onConfirm = { appState.deleteRecord(record!!.id); showDelete = false; back() },
            onDismiss = { showDelete = false }
        )
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("添加照片", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("想用哪种方式记录这一刻？", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                    Spacer(Modifier.size(4.dp))
                    TextButton(onClick = { showSourceDialog = false; requestCamera() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.AddAPhoto, null, tint = Apricot, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("拍照", color = Ink, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { showSourceDialog = false; pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.PhotoLibrary, null, tint = Apricot, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("从相册选择", color = Ink)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSourceDialog = false }) { Text("取消", color = InkSoft) }
            },
            containerColor = Color(0xFFFFFDF8)
        )
    }
}

private fun stepTitle(step: Int, editing: Boolean): String = when (step) {
    0 -> "第 1 步 · 选择类型"
    1 -> "第 ${if (editing) 1 else 2} 步 · 填写内容"
    else -> "第 ${if (editing) 2 else 3} 步 · 上传照片"
}

@Composable
private fun TypeSelect(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("今天想记下哪一类？", style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
        RecordType.all().chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { t ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                            .background(if (selected == t.id) t.tint().copy(alpha = 0.16f) else Paper)
                            .clickable { onSelect(t.id) }.padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(t.icon(), null, tint = t.tint(), modifier = Modifier.size(28.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(t.label, style = MaterialTheme.typography.labelLarge, color = Ink)
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldForm(
    selType: RecordType, title: String, subtitle: String, dateEpoch: Long, status: String,
    tags: List<String>, notes: String, num1: String, num2: String, text1: String, location: String,
    hasEndDate: Boolean, endDate: Long, showMore: Boolean, allTags: List<String>,
    onTitle: (String) -> Unit, onSubtitle: (String) -> Unit, onDate: (Long) -> Unit,
    onStatus: (String) -> Unit, onTag: (String) -> Unit, onNotes: (String) -> Unit,
    onNum1: (String) -> Unit, onNum2: (String) -> Unit, onText1: (String) -> Unit,
    onLocation: (String) -> Unit, onEndDate: (Long) -> Unit, onHasEndDate: (Boolean) -> Unit,
    onShowMore: (Boolean) -> Unit, onPickDate: () -> Unit, onPickEndDate: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(selType.icon(), null, tint = selType.tint(), modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(6.dp))
            Text(selType.label, style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
        }

        EditTextField("标题 *", title, onTitle)
        EditTextField("说明（可选）", subtitle, onSubtitle)

        // 日期
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(PaperWarm)
            .clickable { onPickDate() }.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CalendarMonth, null, tint = Apricot, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("日期", style = MaterialTheme.typography.labelMedium, color = InkSoft)
            Spacer(Modifier.weight(1f))
            Text(formatFullDate(dateEpoch), style = MaterialTheme.typography.bodyMedium, color = Ink)
        }

        // 状态
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecordStatus.all().forEach { s ->
                TagChip(s.label, selected = status == s.id) { onStatus(s.id) }
            }
        }

        // 类型专属字段
        TypeFields(selType, num1, num2, text1, location, hasEndDate, endDate,
            onNum1, onNum2, onText1, onLocation, onEndDate, onHasEndDate, onPickEndDate)

        // 折叠分组：标签 / 备注
        Text("更多信息", Modifier.clickable { onShowMore(!showMore) },
            style = MaterialTheme.typography.labelMedium, color = Apricot)
        if (showMore) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("标签", style = MaterialTheme.typography.labelMedium, color = InkSoft)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    allTags.forEach { t -> TagChip(t, selected = t in tags) { onTag(t) } }
                }
                EditTextField("备注", notes, onNotes, minLines = 3)
            }
        }
    }
}

@Composable
private fun TypeFields(
    selType: RecordType, num1: String, num2: String, text1: String, location: String,
    hasEndDate: Boolean, endDate: Long,
    onNum1: (String) -> Unit, onNum2: (String) -> Unit, onText1: (String) -> Unit,
    onLocation: (String) -> Unit, onEndDate: (Long) -> Unit, onHasEndDate: (Boolean) -> Unit,
    onPickEndDate: () -> Unit
) {
    when (selType) {
        RecordType.BODY -> {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EditTextField("身高 cm", num1, onNum1, Modifier.weight(1f), number = true)
                EditTextField("体重 kg", num2, onNum2, Modifier.weight(1f), number = true)
            }
        }
        RecordType.READING -> {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EditTextField("页数", num1, onNum1, Modifier.weight(1f), number = true)
                EditTextField("分钟", num2, onNum2, Modifier.weight(1f), number = true)
            }
        }
        RecordType.STUDY -> EditTextField("预计时长（分钟）", num1, onNum1, number = true)
        RecordType.ROUTINE -> {
            EditTextField("连续天数", num1, onNum1, number = true)
            EditTextField("作息（如：起床 07:20 · 入睡 21:30）", text1, onText1)
        }
        RecordType.ITEM -> {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EditTextField("总数", num1, onNum1, Modifier.weight(1f), number = true)
                EditTextField("已完成", num2, onNum2, Modifier.weight(1f), number = true)
            }
        }
        RecordType.INTEREST -> {
            EditTextField("上课时间（如：周六 10:30）", text1, onText1)
            EditTextField("地点", location, onLocation)
        }
        RecordType.ACTIVITY -> {
            EditTextField("地点", location, onLocation)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(PaperWarm)
                .clickable { onHasEndDate(!hasEndDate) }.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("设置下次日期", style = MaterialTheme.typography.labelMedium, color = InkSoft)
                    if (hasEndDate) Text(formatFullDate(endDate), style = MaterialTheme.typography.bodyMedium, color = Ink)
                }
                TagChip(if (hasEndDate) "已设" else "未设", selected = hasEndDate) { onHasEndDate(!hasEndDate) }
            }
            if (hasEndDate) {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(PaperWarm)
                    .clickable { onPickEndDate() }.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, null, tint = Apricot, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("点击修改日期", style = MaterialTheme.typography.labelMedium, color = InkSoft)
                }
            }
        }
        RecordType.GROWTH -> {
            Text("成长记录不设数字，把那一刻的心情写在备注里就好", style = MaterialTheme.typography.bodySmall, color = InkSoft)
        }
    }
}

@Composable
private fun MediaStep(images: List<String>, uploading: Boolean, onAdd: () -> Unit, onRemove: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("添加照片或作品图（可选）", style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
        Text("第一张会作为封面，照片会同步到成长相册", style = MaterialTheme.typography.bodySmall, color = InkSoft)

        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // 上传按钮
            Column(
                Modifier.size(96.dp).clip(RoundedCornerShape(14.dp)).background(PaperWarm).clickable { onAdd() },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
            ) {
                if (uploading) CircularProgressIndicator(color = Apricot, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                else Icon(Icons.Filled.AddAPhoto, null, tint = Apricot, modifier = Modifier.size(26.dp))
                Spacer(Modifier.size(4.dp))
                Text(if (uploading) "上传中" else "上传", style = MaterialTheme.typography.labelSmall, color = InkSoft)
            }
            images.forEachIndexed { i, img ->
                Box(Modifier.size(96.dp).clip(RoundedCornerShape(14.dp))) {
                    AppImage(img, Modifier.fillMaxSize())
                    if (i == 0) {
                        Box(Modifier.align(Alignment.TopStart).background(Apricot).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("封面", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                    IconButton(
                        onClick = { onRemove(i) },
                        modifier = Modifier.align(Alignment.TopEnd).size(22.dp).background(Color(0x88000000))
                    ) { Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                }
            }
        }
        Text("点击「完成记录」保存", style = MaterialTheme.typography.labelSmall, color = InkSoft)
    }
}

@Composable
private fun EditTextField(
    label: String, value: String, onChange: (String) -> Unit,
    modifier: Modifier = Modifier, number: Boolean = false, minLines: Int = 1,
    placeholder: @Composable () -> Unit = {}
) {
    TextField(
        value = value, onValueChange = onChange,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        placeholder = placeholder,
        singleLine = minLines == 1,
        minLines = minLines,
        keyboardOptions = if (number) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = PaperWarm, unfocusedContainerColor = PaperWarm,
            focusedIndicatorColor = Apricot, unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun DatePickerDialog(initial: Long, onSelect: (Long) -> Unit, onDismiss: () -> Unit) {
    val c = Calendar.getInstance().apply { timeInMillis = initial }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期", style = MaterialTheme.typography.titleMedium) },
        text = {
            MonthCalendar(
                year = c.get(Calendar.YEAR), month = c.get(Calendar.MONTH),
                selectedDay = initial, hasRecord = { false }, onSelectDay = onSelect
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = { onSelect(startOfToday()) }) {
                Text("回到今天", color = Apricot)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("取消", color = InkSoft) }
        },
        containerColor = Paper
    )
}
