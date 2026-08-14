package com.example.xiaoy.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Calendar
import java.util.UUID

/** 完成亲子目标后的鼓励反馈 */
data class Celebration(val title: String, val message: String, val leafCount: Int)

/** 应用状态中枢：持有数据、对外提供操作与派生统计，所有改动即时持久化。 */
class AppState(context: Context) {

    private val appContext = context.applicationContext
    private val repo = AppRepository(appContext)

    private val _data = MutableStateFlow(repo.load())
    val data: StateFlow<AppData> = _data.asStateFlow()

    private val _celebration = MutableStateFlow<Celebration?>(null)
    val celebration: StateFlow<Celebration?> = _celebration.asStateFlow()

    private fun update(mutate: (AppData) -> AppData) {
        val next = mutate(_data.value)
        _data.value = next
        repo.save(next)
    }

    // ============ 基础 CRUD ============
    fun addRecord(record: Record) = update { it.copy(records = listOf(record) + it.records) }

    fun updateRecord(record: Record) = update {
        it.copy(records = it.records.map { r -> if (r.id == record.id) record else r })
    }

    fun deleteRecord(id: String) = update {
        it.copy(records = it.records.filter { r -> r.id != id })
    }

    /** 给记录追加一张照片（第一张作为封面） */
    fun addImageToRecord(id: String, imagePath: String) = update {
        it.copy(records = it.records.map { r -> if (r.id == id) r.copy(images = r.images + imagePath) else r })
    }

    /** 清除全部记录（保留个人档案与分类），供新用户清空示例数据 */
    fun clearAllRecords() = update {
        it.copy(records = emptyList())
    }

    fun setStatus(id: String, status: String) {
        val rec = recordById(id)
        update { d -> d.copy(records = d.records.map { r -> if (r.id == id) r.copy(status = status) else r }) }
        // 完成亲子目标 → 成长树长新叶 + 鼓励卡
        if (rec != null && rec.type == RecordType.ACTIVITY.id &&
            status == RecordStatus.DONE.id && rec.status != RecordStatus.DONE.id
        ) {
            _celebration.value = Celebration(
                title = "成长树长出了新叶子",
                message = "「${rec.title}」完成啦，这是你们一起攒下的第 ${growthLeaves()} 片叶子。",
                leafCount = growthLeaves()
            )
        }
    }

    fun recordById(id: String): Record? = _data.value.records.firstOrNull { it.id == id }

    fun saveProfile(profile: ChildProfile) = update { it.copy(profile = profile) }

    fun addCustomTag(tag: String) = update {
        if (tag.isBlank() || tag in it.customTags) it else it.copy(customTags = it.customTags + tag)
    }

    fun setReminder(enabled: Boolean, time: String) = update {
        it.copy(reminderEnabled = enabled, reminderTime = time)
    }

    fun consumeCelebration() { _celebration.value = null }

    // ============ 图片上传（复制到私有目录） ============
    fun copyImageToStorage(uri: Uri): String? = try {
        val dir = File(appContext.filesDir, "images").apply { mkdirs() }
        val ext = when (appContext.contentResolver.getType(uri)) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val target = File(dir, "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$ext")
        val input = appContext.contentResolver.openInputStream(uri) ?: return null
        input.use { ins -> target.outputStream().use { out -> ins.copyTo(out) } }
        target.absolutePath
    } catch (_: Exception) {
        null
    }

    // ============ 派生数据 ============
    fun allRecords(): List<Record> = _data.value.records.sortedByDescending { it.dateEpoch }

    fun recordsOfType(type: RecordType): List<Record> =
        _data.value.records.filter { it.type == type.id }.sortedByDescending { it.dateEpoch }

    fun recordsOn(day: Long): List<Record> =
        _data.value.records.filter { isSameDay(it.dateEpoch, day) }.sortedByDescending { it.dateEpoch }

    fun allTags(): List<String> =
        (_data.value.customTags + _data.value.records.flatMap { it.tags }).distinct()

    fun growthLeaves(): Int =
        _data.value.records.count { it.type == RecordType.ACTIVITY.id && it.status == RecordStatus.DONE.id }

    fun milestones(): List<Record> =
        _data.value.records.filter { it.type == RecordType.GROWTH.id && "里程碑" in it.tags }

    /** 今日事项完成情况 */
    fun todayStats(): Triple<Int, Int, Int> {
        val today = startOfToday()
        val list = _data.value.records.filter {
            isSameDay(it.dateEpoch, today) && it.status != RecordStatus.PLANNED.id
        }
        val done = list.count { it.status == RecordStatus.DONE.id }
        val total = list.size
        val rate = if (total == 0) 0 else (done * 100 / total)
        return Triple(done, total, rate)
    }

    /** 本周已完成记录数 */
    fun thisWeekDone(): Int {
        val monday = startOfMonday()
        return _data.value.records.count {
            it.dateEpoch >= monday && it.status == RecordStatus.DONE.id
        }
    }

    fun lastWeekDone(): Int {
        val monday = startOfMonday()
        val prevMonday = monday - 7L * 24 * 3600 * 1000
        return _data.value.records.count {
            it.dateEpoch >= prevMonday && it.dateEpoch < monday && it.status == RecordStatus.DONE.id
        }
    }

    private fun startOfMonday(): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = startOfToday()
        val dow = c.get(Calendar.DAY_OF_WEEK) // 1=周日
        val offset = if (dow == Calendar.SUNDAY) 6 else dow - 2
        c.add(Calendar.DAY_OF_YEAR, -offset)
        return c.timeInMillis
    }

    /** 最近 N 天每日完成数（供趋势图） */
    fun dailyDone(days: Int): List<Pair<Long, Int>> {
        val today = startOfToday()
        return (days - 1 downTo 0).map { offset ->
            val day = daysAgo(offset)
            val count = _data.value.records.count {
                isSameDay(it.dateEpoch, day) && it.status == RecordStatus.DONE.id
            }
            day to count
        }
    }

    /** 各类型记录数量（供分类占比） */
    fun typeCounts(): List<Pair<RecordType, Int>> =
        RecordType.all().map { t -> t to _data.value.records.count { it.type == t.id } }

    /** 即将到来的提醒（按下一次日期） */
    fun upcomingReminders(limit: Int = 3): List<Record> =
        _data.value.records
            .filter { it.endDateEpoch != null && it.endDateEpoch >= startOfToday() }
            .sortedBy { it.endDateEpoch }
            .take(limit)

    fun newId(): String = UUID.randomUUID().toString()
}
