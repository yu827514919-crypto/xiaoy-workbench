package com.example.xiaoy.data

// ============ 记录类型 ============
enum class RecordType(val id: String, val label: String) {
    ROUTINE("routine", "作息"),
    READING("reading", "阅读"),
    STUDY("study", "学习任务"),
    GROWTH("growth", "成长记录"),
    BODY("body", "身高体重"),
    INTEREST("interest", "兴趣课"),
    ACTIVITY("activity", "亲子活动"),
    ITEM("item", "物品清单");

    companion object {
        fun fromId(id: String): RecordType = entries.firstOrNull { it.id == id } ?: GROWTH
        fun all(): List<RecordType> = entries
    }
}

// ============ 记录状态 ============
enum class RecordStatus(val id: String, val label: String) {
    TODO("todo", "待办"),
    DOING("doing", "进行中"),
    DONE("done", "已完成"),
    PLANNED("planned", "计划"),
    ATTENTION("attention", "需注意");

    companion object {
        fun fromId(id: String): RecordStatus = entries.firstOrNull { it.id == id } ?: DOING
        fun all(): List<RecordStatus> = entries
    }
}

// ============ 一条记录（统一模型，覆盖 8 大模块） ============
data class Record(
    val id: String,
    val type: String,               // RecordType.id
    val title: String,
    val subtitle: String = "",      // 书名 / 课程名 / 场景说明
    val dateEpoch: Long,            // 主要日期（当天 0 点毫秒）
    val endDateEpoch: Long? = null, // 结束 / 下一次日期（用于提醒）
    val status: String = "doing",   // RecordStatus.id
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val images: List<String> = emptyList(), // "drawable:xxx" 或 "file:/..."
    val audioPath: String? = null,  // 录音文件绝对路径（一条记录一段声音）
    val num1: Double? = null,       // 关键数值：身高cm / 页数 / 题数 / 数量 / 连续天数
    val num2: Double? = null,       // 次要数值：体重kg / 分钟数 / 已完成数
    val text1: String = "",         // 场景专属文本：起床时间 / 上课时间
    val location: String = "",      // 地点
    val createdAt: Long = 0L
) {
    fun cover(): String? = images.firstOrNull()
    fun typeEnum() = RecordType.fromId(type)
    fun statusEnum() = RecordStatus.fromId(status)
}

// ============ 图片引用 ============
object ImageRef {
    const val DRAWABLE = "drawable"
    const val FILE = "file"
    fun of(resName: String): String = "$DRAWABLE:$resName"
    fun ofFile(path: String): String = "$FILE:$path"
    fun parts(ref: String): Pair<String, String> {
        val i = ref.indexOf(':')
        return if (i > 0) ref.substring(0, i) to ref.substring(i + 1) else DRAWABLE to ref
    }
}

// ============ 孩子与家长档案 ============
data class ChildProfile(
    val parentName: String = "",   // 家长称呼
    val childName: String = "",    // 孩子昵称
    val childBirthday: String = "", // 出生日期 "2020-03-12"
    val city: String = "",         // 城市
    val motto: String = "把孩子的每一次长大，温柔记下来"
)

// ============ 应用整体数据（持久化根对象） ============
data class AppData(
    val version: Int = 1,
    val profile: ChildProfile? = null,
    val records: List<Record> = emptyList(),
    val customTags: List<String> = emptyList(), // 用户自建分类
    val reminderEnabled: Boolean = true,
    val reminderTime: String = "20:30",
    val themeMode: String = "system" // "system" | "light" | "dark"
)
