package com.example.xiaoy.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.Record
import com.example.xiaoy.data.RecordStatus
import com.example.xiaoy.ui.icon
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkFaint
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.LeafGreen
import com.example.xiaoy.ui.theme.Paper
import com.example.xiaoy.ui.tint

/**
 * 统一记录卡片：封面 / 标题 / 场景标签 / 日期 / 状态 / 关键数值。
 * 采用「档案页」形态——浅暖色底 + 左侧封面，右侧完成勾选。
 */
@Composable
fun RecordCard(
    record: Record,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleDone: (() -> Unit)? = null
) {
    val type = record.typeEnum()
    val status = record.statusEnum()
    val isDone = status == RecordStatus.DONE
    val tint = type.tint()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Paper)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 封面缩略图
        Box(Modifier.size(60.dp).clip(RoundedCornerShape(12.dp))) {
            val cover = record.cover()
            if (cover != null) {
                AppImage(cover, modifier = Modifier.size(60.dp))
            } else {
                Box(Modifier.size(60.dp).background(tint.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                    Icon(type.icon(), null, tint = tint, modifier = Modifier.size(26.dp))
                }
            }
        }
        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(type.label, style = MaterialTheme.typography.labelSmall, color = tint, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                StatusBadge(status)
            }
            Spacer(Modifier.height(4.dp))
            Text(record.title, style = MaterialTheme.typography.titleSmall, color = Ink,
                fontWeight = FontWeight.SemiBold, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(record.subLabel(), style = MaterialTheme.typography.bodySmall, color = InkSoft, maxLines = 1)
            val metric = record.metric()
            Spacer(Modifier.height(4.dp))
            Text(listOfNotNull(record.dateLabel(), metric).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall, color = InkFaint, maxLines = 1)
        }

        // 完成勾选（可快速标记完成）
        if (onToggleDone != null) {
            IconButton(onClick = onToggleDone, modifier = Modifier.size(36.dp)) {
                if (isDone) {
                    Icon(Icons.Filled.CheckCircle, null, tint = LeafGreen, modifier = Modifier.size(26.dp))
                } else {
                    Icon(Icons.Outlined.RadioButtonUnchecked, null, tint = InkFaint, modifier = Modifier.size(26.dp))
                }
            }
        }
    }
}
