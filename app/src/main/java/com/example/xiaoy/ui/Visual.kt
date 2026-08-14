package com.example.xiaoy.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.xiaoy.data.RecordStatus
import com.example.xiaoy.data.RecordType
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.ApricotSoft
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.LeafGreen
import com.example.xiaoy.ui.theme.LeafGreenSoft
import com.example.xiaoy.ui.theme.Sage
import com.example.xiaoy.ui.theme.SageSoft
import com.example.xiaoy.ui.theme.Terracotta
import com.example.xiaoy.ui.theme.TerracottaSoft

/** 记录类型 → 场景图标（统一线性/剪影语言，与场景相关） */
fun RecordType.icon(): ImageVector = when (this) {
    RecordType.ROUTINE -> Icons.Filled.Bedtime
    RecordType.READING -> Icons.Filled.MenuBook
    RecordType.STUDY -> Icons.Filled.School
    RecordType.GROWTH -> Icons.Filled.ChildCare
    RecordType.BODY -> Icons.Filled.Height
    RecordType.INTEREST -> Icons.Filled.MusicNote
    RecordType.ACTIVITY -> Icons.Filled.FamilyRestroom
    RecordType.ITEM -> Icons.Filled.Checklist
}

fun RecordType.tint(): Color = when (this) {
    RecordType.ROUTINE -> Apricot
    RecordType.READING -> Sage
    RecordType.STUDY -> Terracotta
    RecordType.GROWTH -> LeafGreen
    RecordType.BODY -> Color(0xFF5B8BB0)
    RecordType.INTEREST -> Color(0xFF9A6BB0)
    RecordType.ACTIVITY -> Color(0xFFC8793A)
    RecordType.ITEM -> Color(0xFF6E7C8A)
}

fun RecordStatus.color(): Color = when (this) {
    RecordStatus.DONE -> LeafGreen
    RecordStatus.DOING -> Apricot
    RecordStatus.TODO -> Sage
    RecordStatus.PLANNED -> InkSoft
    RecordStatus.ATTENTION -> Terracotta
}

fun RecordStatus.container(): Color = when (this) {
    RecordStatus.DONE -> LeafGreenSoft
    RecordStatus.DOING -> ApricotSoft
    RecordStatus.TODO -> SageSoft
    RecordStatus.PLANNED -> Color(0xFFECE7DC)
    RecordStatus.ATTENTION -> TerracottaSoft
}
