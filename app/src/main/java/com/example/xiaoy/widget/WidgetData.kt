package com.example.xiaoy.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.xiaoy.data.AppData
import com.example.xiaoy.data.RecordStatus
import com.example.xiaoy.data.RecordType
import com.example.xiaoy.data.ageLabel
import com.example.xiaoy.data.daysSinceBirth
import com.example.xiaoy.data.isSameDay
import com.example.xiaoy.data.startOfToday

/** 把小组件需要的数据写入 SharedPreferences，并触发刷新 */
object WidgetData {

    const val PREFS = "xiaoy_widget"

    fun sync(context: Context, data: AppData) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = startOfToday()
        val todayList = data.records.filter {
            isSameDay(it.dateEpoch, today) && it.status != RecordStatus.PLANNED.id
        }
        val done = todayList.count { it.status == RecordStatus.DONE.id }
        val total = todayList.size
        val profile = data.profile
        val age = ageLabel(profile?.childBirthday ?: "")
        val days = daysSinceBirth(profile?.childBirthday ?: "")
        val leaves = data.records.count {
            it.type == RecordType.ACTIVITY.id && it.status == RecordStatus.DONE.id
        }

        sp.edit()
            .putString("child", profile?.childName ?: "")
            .putString("age", age)
            .putInt("done", done)
            .putInt("total", total)
            .putInt("days", days ?: 0)
            .putInt("leaves", leaves)
            .apply()

        refresh(context)
    }

    /** 广播触发所有小组件刷新 */
    fun refresh(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(ComponentName(context, GrowthWidgetProvider::class.java))
        if (ids.isNotEmpty()) {
            context.sendBroadcast(
                Intent(context, GrowthWidgetProvider::class.java)
                    .setAction(GrowthWidgetProvider.ACTION_REFRESH)
            )
        }
    }
}
