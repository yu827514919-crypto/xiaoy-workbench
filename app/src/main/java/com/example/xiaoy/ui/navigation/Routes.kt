package com.example.xiaoy.ui.navigation

import com.example.xiaoy.data.RecordType

/** 应用路由（单 Activity + 自建返回栈） */
sealed class Route {
    object Home : Route()
    object Records : Route()
    object Plan : Route()
    object Insights : Route()
    object Gallery : Route()
    object Settings : Route()
    object Onboarding : Route()

    /** 详情页 */
    data class Detail(val id: String) : Route()

    /** 编辑/新增页；id 为 null 表示新增，presetType 指定预设类型 */
    data class Edit(val id: String?, val presetType: String?) : Route()

    /** 大图查看：某条记录的第 index 张图片 */
    data class ImageView(val recordId: String, val index: Int) : Route()

    /** 阶段报告（按类型汇总） */
    data class Report(val type: String) : Route()

    companion object {
        /** 底部导航的一级页面 */
        val bottomTabs = listOf(
            Home, Records, Plan, Insights, Settings
        )

        fun tabIndex(route: Route): Int = when (route) {
            is Home -> 0
            is Records -> 1
            is Plan -> 2
            is Insights -> 3
            is Settings -> 4
            else -> -1
        }

        fun isBottomTab(route: Route): Boolean = tabIndex(route) >= 0

        /** 类型到记录库的快捷入口 */
        fun recordTabType(route: Route): RecordType? = null
    }
}
