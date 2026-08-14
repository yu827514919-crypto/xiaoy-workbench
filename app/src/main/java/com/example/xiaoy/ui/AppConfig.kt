package com.example.xiaoy.ui

/** 应用信息与更新配置 */
object AppConfig {
    const val APP_NAME = "小芽"
    const val APP_FULL_NAME = "小芽 · 孩子学习与成长管理工作台"
    const val SLOGAN = "把孩子的每一次长大，温柔记下来"
    const val VERSION_NAME = "1.0.3"
    const val VERSION_CODE = 4

    /** 更新说明（每次发版同步更新） */
    const val CHANGELOG = "v1.0.3 修复：计划页下拉到底时，当天记录与即将提醒因列表 key 重复导致的崩溃。"

    /** 热更新版本清单地址（GitHub 原始文件） */
    const val UPDATE_URL = "https://raw.githubusercontent.com/yu827514919-crypto/xiaoy-workbench/main/version.json"
}
