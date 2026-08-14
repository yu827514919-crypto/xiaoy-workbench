package com.example.xiaoy.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.xiaoy.MainActivity
import com.example.xiaoy.R

/** 桌面小组件：今日待办 + 成长天数 */
class GrowthWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, buildViews(context))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, GrowthWidgetProvider::class.java))
            if (ids.isNotEmpty()) onUpdate(context, mgr, ids)
        }
    }

    private fun buildViews(context: Context): RemoteViews {
        val sp = context.getSharedPreferences(WidgetData.PREFS, Context.MODE_PRIVATE)
        val child = sp.getString("child", "") ?: ""
        val age = sp.getString("age", "") ?: ""
        val done = sp.getInt("done", 0)
        val total = sp.getInt("total", 0)
        val days = sp.getInt("days", 0)
        val leaves = sp.getInt("leaves", 0)

        val views = RemoteViews(context.packageName, R.layout.widget_growth)

        val title = if (child.isBlank()) "小芽" else "$child · ${age.ifBlank { "成长中" }}"
        views.setTextViewText(R.id.widget_title, title)
        views.setTextViewText(R.id.widget_todo, "$done/$total")
        views.setTextViewText(R.id.widget_days, "$days 天")
        views.setTextViewText(R.id.widget_leaves, "成长树已有 $leaves 片叶子")

        // 点击打开 App
        val launch = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_root, launch)

        // 刷新按钮
        val refresh = PendingIntent.getBroadcast(
            context, 1,
            Intent(context, GrowthWidgetProvider::class.java).setAction(ACTION_REFRESH),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_refresh, refresh)

        return views
    }

    companion object {
        const val ACTION_REFRESH = "com.example.xiaoy.widget.REFRESH"
    }
}
