package com.example.xiaoy.ui.components

import com.example.xiaoy.data.Record
import com.example.xiaoy.data.RecordType
import com.example.xiaoy.data.formatDate
import java.util.Locale

/** 列表卡片上展示的关键数值（按类型语义化） */
fun Record.metric(): String? = when (type) {
    RecordType.BODY.id -> num1?.let { String.format(Locale.CHINA, "%.1f cm · %.1f kg", it, num2 ?: 0.0) }
    RecordType.READING.id -> num1?.let { "${it.toInt()} 页 · ${(num2 ?: 0.0).toInt()} 分钟" }
    RecordType.STUDY.id -> num1?.let { "约 ${it.toInt()} 分钟" }
    RecordType.ROUTINE.id -> num1?.let { "连续 ${it.toInt()} 天" }
    RecordType.ITEM.id -> if (num1 != null && num2 != null)
        "${(num2).toInt()} / ${(num1).toInt()} 项" else num1?.let { "${it.toInt()} 项" }
    RecordType.INTEREST.id -> text1.ifBlank { null }
    RecordType.ACTIVITY.id -> location.ifBlank { null }
    else -> null
}

fun Record.dateLabel(): String = formatDate(dateEpoch)

/** 副标题或类型说明（列表第二行） */
fun Record.subLabel(): String = subtitle.ifBlank {
    if (tags.isNotEmpty()) tags.joinToString(" · ") else typeEnum().label
}
