# 小芽 · 孩子学习与成长管理工作台

把孩子的每一次长大，温柔记下来。

「小芽」是面向家长个人的孩子学习与成长管理工作台（单端移动应用）。在碎片时间里快速看懂今天要做什么、当前进展、需要注意的事项、最近的记录与已经获得的成果。

## 技术栈

- Kotlin + Jetpack Compose（Material 3）
- 本地 JSON 持久化（Gson，应用私有目录）
- 自建导航与返回栈（单 Activity）
- 原创品牌 Logo / 应用图标 / 场景插画

## 模块

作息 · 阅读 · 学习任务 · 成长记录 · 身高体重 · 兴趣课 · 亲子活动 · 物品清单

## 一级页面

首页（场景主视觉）、成长记录册、成长日历、数据洞察、成长相册、我的

## 热更新（版本管理）

每次发版必须同步更新以下四处并追加更新说明：

1. `app/build.gradle.kts` —— `versionCode` / `versionName`
2. `app/src/main/java/com/example/xiaoy/ui/AppConfig.kt` —— `VERSION_CODE` / `VERSION_NAME` / `CHANGELOG`
3. 根目录 `version.json`（应用内「检查更新」读取此文件比对版本）
4. `CHANGELOG.md`（更新说明）

应用内「我的 → 检查更新」会读取 `version.json` 的 `versionName` 与本地版本比对。

## 构建

```bash
./gradlew :app:assembleDebug
```

APK 输出于 `app/build/outputs/apk/debug/app-debug.apk`。
