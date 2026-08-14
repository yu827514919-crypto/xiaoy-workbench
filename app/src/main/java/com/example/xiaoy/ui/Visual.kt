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
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.xiaoy.data.RecordStatus
import com.example.xiaoy.data.RecordType
import com.example.xiaoy.ui.theme.LocalXiaoYColors

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

@Composable
fun RecordType.tint(): Color = when (this) {
    RecordType.ROUTINE -> LocalXiaoYColors.current.primary
    RecordType.READING -> LocalXiaoYColors.current.secondary
    RecordType.STUDY -> LocalXiaoYColors.current.accent
    RecordType.GROWTH -> LocalXiaoYColors.current.leaf
    RecordType.BODY -> LocalXiaoYColors.current.typeBody
    RecordType.INTEREST -> LocalXiaoYColors.current.typeInterest
    RecordType.ACTIVITY -> LocalXiaoYColors.current.typeActivity
    RecordType.ITEM -> LocalXiaoYColors.current.typeItem
}

@Composable
fun RecordStatus.color(): Color = when (this) {
    RecordStatus.DONE -> LocalXiaoYColors.current.leaf
    RecordStatus.DOING -> LocalXiaoYColors.current.primary
    RecordStatus.TODO -> LocalXiaoYColors.current.secondary
    RecordStatus.PLANNED -> LocalXiaoYColors.current.inkSoft
    RecordStatus.ATTENTION -> LocalXiaoYColors.current.accent
}

@Composable
fun RecordStatus.container(): Color = when (this) {
    RecordStatus.DONE -> LocalXiaoYColors.current.leafSoft
    RecordStatus.DOING -> LocalXiaoYColors.current.primarySoft
    RecordStatus.TODO -> LocalXiaoYColors.current.secondarySoft
    RecordStatus.PLANNED -> LocalXiaoYColors.current.plannedContainer
    RecordStatus.ATTENTION -> LocalXiaoYColors.current.accentSoft
}
