package com.example.xiaoy.ui.screens

import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.AppState
import com.example.xiaoy.data.ImageRef
import com.example.xiaoy.data.ageLabel
import com.example.xiaoy.ui.AppConfig
import com.example.xiaoy.ui.components.AppImage
import com.example.xiaoy.ui.components.LocalSnackbar
import com.example.xiaoy.ui.components.TagChip
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.Paper
import com.example.xiaoy.ui.theme.PaperWarm
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private val timeOptions = listOf("20:00", "20:30", "21:00", "21:30")

@Composable
fun SettingsScreen(appState: AppState, nav: (com.example.xiaoy.ui.navigation.Route) -> Unit) {
    val data by appState.data.collectAsState()
    val context = LocalContext.current
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val profile = data.profile

    var showProfileEdit by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }

    // 检查更新状态
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var downloadFailed by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("我的", style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
                Text(AppConfig.SLOGAN, style = MaterialTheme.typography.labelSmall, color = InkSoft)
            }
        }

        // 档案
        item {
            Row(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)).background(PaperWarm).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppImage(ImageRef.of("img_logo"), Modifier.size(52.dp).clip(CircleShape))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(profile?.parentName?.ifBlank { "未填写" } ?: "未填写",
                        style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
                    Text(
                        listOfNotNull(
                            profile?.childName?.ifBlank { null },
                            ageLabel(profile?.childBirthday ?: "").ifBlank { null },
                            profile?.city?.ifBlank { null }
                        ).joinToString(" · ").ifBlank { "点击补充孩子信息" },
                        style = MaterialTheme.typography.bodySmall, color = InkSoft
                    )
                }
                TextButton(onClick = { showProfileEdit = true }) { Text("编辑", color = Apricot) }
            }
        }

        // 提醒
        item { SectionLabel("每日提醒") }
        item {
            Column(Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(Paper).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("睡前提醒", style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.SemiBold)
                        Text("每晚提醒我记录今天的小事", style = MaterialTheme.typography.labelSmall, color = InkSoft)
                    }
                    Switch(
                        checked = data.reminderEnabled,
                        onCheckedChange = { appState.setReminder(it, data.reminderTime) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Apricot)
                    )
                }
                Spacer(Modifier.size(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    timeOptions.forEach { t ->
                        TagChip(t, selected = data.reminderTime == t) { appState.setReminder(data.reminderEnabled, t) }
                    }
                }
            }
        }

        // 分类 / 标签
        item { SectionLabel("分类 · 标签") }
        item {
            Column(Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(Paper).padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = newTag, onValueChange = { newTag = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("新增一个分类…", style = MaterialTheme.typography.bodyMedium) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = PaperWarm, unfocusedContainerColor = PaperWarm,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    androidx.compose.material3.IconButton(
                        onClick = {
                            if (newTag.isNotBlank()) { appState.addCustomTag(newTag.trim()); newTag = "" }
                        },
                        modifier = Modifier.clip(CircleShape).background(Apricot).size(48.dp)
                    ) {
                        Icon(Icons.Filled.Add, null, tint = Color.White)
                    }
                }
                Spacer(Modifier.size(12.dp))
                Text("新分类会立即出现在筛选、统计图例和新增表单里", style = MaterialTheme.typography.labelSmall, color = InkSoft)
                Spacer(Modifier.size(8.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    appState.allTags().forEach { tag ->
                        TagChip(tag)
                    }
                }
            }
        }

        // 数据
        item { SectionLabel("数据") }
        item {
            Column(Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(Paper)) {
                SettingRow("导出数据", "以 JSON 分享全部记录") {
                    val json = Gson().toJson(appState.data.value)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "小芽数据备份")
                        putExtra(Intent.EXTRA_TEXT, json)
                    }
                    context.startActivity(Intent.createChooser(intent, "导出数据"))
                }
                SettingRow("本地存储", "数据仅保存在本机，删除应用会一并清除") {}
            }
        }

        // 关于
        item { SectionLabel("关于") }
        item {
            Column(Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(Paper)) {
                SettingRow("当前版本", "v${AppConfig.VERSION_NAME}") {}
                SettingRow("更新说明", AppConfig.CHANGELOG) {}
                SettingRow("检查更新", "从 GitHub 获取最新版本") {
                    scope.launch {
                        val info = withContext(Dispatchers.IO) { fetchUpdateInfo() }
                        when {
                            info == null -> snackbar.showSnackbar("检查失败，请检查网络后重试")
                            info.versionName != AppConfig.VERSION_NAME -> {
                                updateInfo = info; showUpdateDialog = true
                            }
                            else -> snackbar.showSnackbar("已是最新版本 v${AppConfig.VERSION_NAME}")
                        }
                    }
                }
            }
        }
    }

    if (showProfileEdit) {
        ProfileEditDialog(appState, profile, onDismiss = { showProfileEdit = false })
    }

    if (showUpdateDialog && updateInfo != null) {
        UpdateDialog(
            info = updateInfo!!,
            downloading = downloading,
            progress = downloadProgress,
            failed = downloadFailed,
            onDownload = {
                scope.launch {
                    downloading = true; downloadFailed = false; downloadProgress = 0
                    val dest = File(context.filesDir, "update/update.apk").apply { parentFile?.mkdirs() }
                    val ok = downloadFile(updateInfo!!.downloadUrl, dest) { downloadProgress = it }
                    downloading = false
                    if (ok) { showUpdateDialog = false; installApk(context, dest) }
                    else downloadFailed = true
                }
            },
            onDismiss = { if (!downloading) showUpdateDialog = false }
        )
    }
}

private data class UpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val changelog: String,
    val downloadUrl: String
)

private fun fetchUpdateInfo(): UpdateInfo? = try {
    val conn = URL(AppConfig.UPDATE_URL).openConnection() as HttpURLConnection
    conn.connectTimeout = 8000; conn.readTimeout = 8000
    val text = conn.inputStream.bufferedReader().readText()
    conn.disconnect()
    val obj = JSONObject(text)
    val name = obj.optString("versionName")
    if (name.isBlank()) null else UpdateInfo(
        versionName = name,
        versionCode = obj.optInt("versionCode", 0),
        changelog = obj.optString("changelog", ""),
        downloadUrl = obj.optString("downloadUrl", "")
    )
} catch (_: Exception) { null }

/** 下载 APK 到本地，返回是否成功（onProgress 0..100） */
private suspend fun downloadFile(url: String, dest: File, onProgress: (Int) -> Unit): Boolean =
    withContext(Dispatchers.IO) {
        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 60000
            conn.instanceFollowRedirects = true
            val length = conn.contentLength.toLong()
            conn.inputStream.use { input ->
                dest.outputStream().use { output ->
                    val buf = ByteArray(16384)
                    var total = 0L
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        output.write(buf, 0, n)
                        total += n
                        if (length > 0) onProgress((total * 100 / length).toInt().coerceIn(0, 100))
                    }
                }
            }
            conn.disconnect()
            dest.length() > 0
        } catch (_: Exception) {
            false
        }
    }

/** 通过 FileProvider 唤起系统安装器安装 APK */
private fun installApk(context: android.content.Context, apkFile: File) {
    try {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // 无 FileProvider 时降级：提示用户自行安装
        android.widget.Toast.makeText(context, "下载完成，请前往应用更新目录安装", android.widget.Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun UpdateDialog(
    info: UpdateInfo,
    downloading: Boolean,
    progress: Int,
    failed: Boolean,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发现新版本 v${info.versionName}", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                if (info.changelog.isNotBlank()) {
                    Text(info.changelog, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                }
                if (downloading) {
                    Spacer(Modifier.size(12.dp))
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Apricot
                    )
                    Spacer(Modifier.size(6.dp))
                    Text("正在下载 $progress%", style = MaterialTheme.typography.labelMedium, color = InkSoft)
                }
                if (failed) {
                    Spacer(Modifier.size(12.dp))
                    Text("下载失败，请重试", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFB9503A))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDownload, enabled = !downloading) {
                Text(if (downloading) "下载中…" else "立即更新", color = Apricot, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !downloading) { Text("取消", color = InkSoft) }
        },
        containerColor = Paper
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, Modifier.padding(horizontal = 16.dp).padding(top = 18.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.Bold)
}

@Composable
private fun SettingRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.size(2.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = InkSoft)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = InkSoft)
    }
}

@Composable
private fun ProfileEditDialog(
    appState: AppState,
    current: com.example.xiaoy.data.ChildProfile?,
    onDismiss: () -> Unit
) {
    var parent by remember { mutableStateOf(current?.parentName ?: "") }
    var child by remember { mutableStateOf(current?.childName ?: "") }
    var birthday by remember { mutableStateOf(current?.childBirthday ?: "") }
    var city by remember { mutableStateOf(current?.city ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑资料", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EditField("家长称呼", parent) { parent = it }
                EditField("孩子昵称", child) { child = it }
                EditField("出生日期（如 2020-03-12）", birthday) { birthday = it }
                EditField("所在城市", city) { city = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                appState.saveProfile(
                    com.example.xiaoy.data.ChildProfile(
                        parentName = parent, childName = child,
                        childBirthday = birthday, city = city,
                        motto = current?.motto ?: AppConfig.SLOGAN
                    )
                )
                onDismiss()
            }) { Text("保存", color = Apricot, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = InkSoft) } },
        containerColor = Paper
    )
}

@Composable
private fun EditField(label: String, value: String, onChange: (String) -> Unit) {
    TextField(
        value = value, onValueChange = onChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = PaperWarm, unfocusedContainerColor = PaperWarm,
            focusedIndicatorColor = Apricot, unfocusedIndicatorColor = Color.Transparent
        )
    )
}
