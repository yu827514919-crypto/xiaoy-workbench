package com.example.xiaoy.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.xiaoy.R
import com.example.xiaoy.data.ImageRef
import com.example.xiaoy.data.RecordStatus
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkFaint
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.Line
import com.example.xiaoy.ui.theme.Paper
import com.example.xiaoy.ui.theme.PaperWarm
import com.example.xiaoy.ui.theme.SageSoft
import com.example.xiaoy.ui.theme.Terracotta
import com.example.xiaoy.ui.color
import com.example.xiaoy.ui.container
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 全局 Snackbar（用于保存成功/失败等轻量反馈） */
val LocalSnackbar = staticCompositionLocalOf<SnackbarHostState> { error("No SnackbarHostState") }

// ============ 图片 ============
fun drawableRes(name: String): Int? = when (name) {
    "img_logo" -> R.drawable.img_logo
    "img_scene_home" -> R.drawable.img_scene_home
    "img_art_child" -> R.drawable.img_art_child
    "img_cover_moon" -> R.drawable.img_cover_moon
    "img_cover_frog" -> R.drawable.img_cover_frog
    "img_height" -> R.drawable.img_height
    "img_pumpkin" -> R.drawable.img_pumpkin
    "img_sticker" -> R.drawable.img_sticker
    "img_tree" -> R.drawable.img_tree
    "img_empty" -> R.drawable.img_empty
    "img_ballet" -> R.drawable.img_ballet
    "img_pinyin" -> R.drawable.img_pinyin
    "img_garden" -> R.drawable.img_garden
    else -> null
}

/** 统一图片组件：内置插画直接加载，用户上传文件异步解码，失败可重试 */
@Composable
fun AppImage(
    ref: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: (() -> Unit)? = null
) {
    val m = if (onClick != null) modifier.clickable { onClick() } else modifier
    when {
        ref.isNullOrBlank() -> ImagePlaceholder(m)
        else -> {
            val (kind, value) = ImageRef.parts(ref)
            if (kind == ImageRef.DRAWABLE) {
                val resId = drawableRes(value)
                if (resId != null) {
                    Image(painterResource(resId), null, modifier = m, contentScale = contentScale)
                } else ImagePlaceholder(m)
            } else {
                FileImage(path = value, modifier = m, contentScale = contentScale)
            }
        }
    }
}

/** 无图时的主题占位（青绿底 + 嫩芽图标，非灰色块） */
@Composable
fun ImagePlaceholder(modifier: Modifier = Modifier) {
    Box(modifier.background(SageSoft), contentAlignment = Alignment.Center) {
        Icon(Icons.Filled.Spa, null, tint = Apricot, modifier = Modifier.size(28.dp))
    }
}

private fun decodeSampledBitmap(path: String, reqW: Int, reqH: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    while (bounds.outWidth / sample > reqW * 2 || bounds.outHeight / sample > reqH * 2) sample *= 2
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
}

@Composable
private fun FileImage(path: String, modifier: Modifier, contentScale: ContentScale) {
    var retry by remember(path) { mutableIntStateOf(0) }
    var bitmap by remember(path) { mutableStateOf<Bitmap?>(null) }
    var loading by remember(path) { mutableStateOf(true) }
    var failed by remember(path) { mutableStateOf(false) }

    LaunchedEffect(path, retry) {
        loading = true; failed = false
        val bmp = withContext(Dispatchers.IO) { decodeSampledBitmap(path, 1024, 1024) }
        bitmap = bmp; loading = false; failed = bmp == null
    }

    when {
        loading -> Box(modifier.background(SageSoft), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Apricot, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
        }
        failed -> Box(modifier.background(SageSoft), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.BrokenImage, null, tint = InkFaint, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(6.dp))
                Row(Modifier.clickable { retry++ }.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Refresh, null, tint = InkSoft, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("重新加载", style = MaterialTheme.typography.labelSmall, color = InkSoft)
                }
            }
        }
        bitmap != null -> Image(bitmap!!.asImageBitmap(), null, modifier, contentScale = contentScale)
    }
}

// ============ 状态徽章 ============
@Composable
fun StatusBadge(status: RecordStatus, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(status.container())
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(status.color()))
        Spacer(Modifier.width(4.dp))
        Text(status.label, style = MaterialTheme.typography.labelSmall, color = status.color())
    }
}

// ============ 标签 ============
@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val bg = if (selected) Apricot else PaperWarm
    val fg = if (selected) Color.White else InkSoft
    val base = modifier
        .clip(RoundedCornerShape(50))
        .background(bg)
        .then(if (!selected) Modifier.border(1.dp, Line, RoundedCornerShape(50)) else Modifier)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(horizontal = 12.dp, vertical = 6.dp)
    Text(text, style = MaterialTheme.typography.labelMedium, color = fg, modifier = base)
}

// ============ 分区标题 ============
@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Ink,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)) {
                Text(actionText, style = MaterialTheme.typography.labelMedium, color = Apricot)
            }
        }
    }
}

// ============ 空状态 ============
@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppImage(ImageRef.of("img_empty"), modifier = Modifier.size(120.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = InkSoft,
            modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction, colors = ButtonDefaults.buttonColors(containerColor = Apricot)) {
                Text(actionText, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ============ 二次确认删除 ============
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "删除",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = InkSoft) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = Terracotta, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = InkSoft) }
        },
        containerColor = Paper
    )
}

@Composable
fun SingleLineText(text: String, modifier: Modifier = Modifier, maxLines: Int = 1) {
    Text(text, modifier = modifier, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
}
