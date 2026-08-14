package com.example.xiaoy.ui

/** 应用信息与更新配置 */
object AppConfig {
    const val APP_NAME = "小芽"
    const val APP_FULL_NAME = "小芽 · 孩子学习与成长管理工作台"
    const val SLOGAN = "把孩子的每一次长大，温柔记下来"
    const val VERSION_NAME = "1.1.0"
    const val VERSION_CODE = 9

    /** 更新说明（每次发版同步更新） */
    const val CHANGELOG = "v1.1.0 新增：录音记录、桌面小组件、深色模式、每日本地通知提醒。"

    /** 热更新版本清单地址（腾讯云 COS，国内直连） */
    const val UPDATE_URL = "https://xiaoy-workbench-1468493405.cos.ap-guangzhou.myqcloud.com/version.json"
}
