package com.example.xiaoy.ui

/** 应用信息与更新配置 */
object AppConfig {
    const val APP_NAME = "小芽"
    const val APP_FULL_NAME = "小芽 · 孩子学习与成长管理工作台"
    const val SLOGAN = "把孩子的每一次长大，温柔记下来"
    const val VERSION_NAME = "1.0.6"
    const val VERSION_CODE = 7

    /** 更新说明（每次发版同步更新） */
    const val CHANGELOG = "v1.0.6 新增：记录详情可随时补拍照片；设置里可一键清除全部记录（新用户友好）。"

    /** 热更新版本清单地址（腾讯云 COS，国内直连） */
    const val UPDATE_URL = "https://xiaoy-workbench-1468493405.cos.ap-guangzhou.myqcloud.com/version.json"
}
