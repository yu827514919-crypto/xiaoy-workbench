package com.example.xiaoy.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun startOfDay(epoch: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = epoch
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

fun startOfToday(): Long = startOfDay(System.currentTimeMillis())

fun daysAgo(n: Int): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = startOfToday()
    c.add(Calendar.DAY_OF_YEAR, -n)
    return c.timeInMillis
}

fun daysFromNow(n: Int): Long = daysAgo(-n)

fun isSameDay(a: Long, b: Long): Boolean {
    val ca = Calendar.getInstance().apply { timeInMillis = a }
    val cb = Calendar.getInstance().apply { timeInMillis = b }
    return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
        ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
}

fun formatDate(epoch: Long): String {
    val today = startOfToday()
    return when {
        isSameDay(epoch, today) -> "今天"
        isSameDay(epoch, daysAgo(1)) -> "昨天"
        isSameDay(epoch, daysFromNow(1)) -> "明天"
        else -> SimpleDateFormat("M月d日", Locale.CHINA).format(epoch)
    }
}

fun formatFullDate(epoch: Long): String =
    SimpleDateFormat("yyyy年M月d日", Locale.CHINA).format(epoch)

fun formatDateWithWeekday(epoch: Long): String =
    SimpleDateFormat("M月d日 E", Locale.CHINA).format(epoch)

fun formatWeekday(epoch: Long): String =
    SimpleDateFormat("EEEE", Locale.CHINA).format(epoch)

fun formatTime(epoch: Long): String =
    SimpleDateFormat("HH:mm", Locale.CHINA).format(epoch)

fun monthLabel(epoch: Long): String =
    SimpleDateFormat("yyyy年M月", Locale.CHINA).format(epoch)

fun monthKey(epoch: Long): String =
    SimpleDateFormat("yyyy-MM", Locale.CHINA).format(epoch)

fun dayKey(epoch: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(epoch)

/** 取某个月第一天的 0 点毫秒 */
fun firstDayOfMonth(year: Int, month: Int): Long {
    val c = Calendar.getInstance()
    c.set(year, month, 1, 0, 0, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

/** 本周一 0 点 */
fun startOfThisWeek(): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = startOfToday()
    val dow = c.get(Calendar.DAY_OF_WEEK)
    val offset = if (dow == Calendar.SUNDAY) 6 else dow - 2
    c.add(Calendar.DAY_OF_YEAR, -offset)
    return c.timeInMillis
}

/** 本月 1 日 0 点 */
fun startOfThisMonth(): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = startOfToday()
    return firstDayOfMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
}

fun Calendar.monthYear(): Pair<Int, Int> = get(Calendar.YEAR) to get(Calendar.MONTH)

/** 由出生日期 "yyyy-MM-dd" 计算年龄标签，如 "6岁5个月" */
fun ageLabel(birthday: String): String = try {
    val birth = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(birthday) ?: return ""
    val now = Calendar.getInstance()
    val b = Calendar.getInstance().apply { time = birth }
    var years = now.get(Calendar.YEAR) - b.get(Calendar.YEAR)
    var months = now.get(Calendar.MONTH) - b.get(Calendar.MONTH)
    if (months < 0) { years -= 1; months += 12 }
    when {
        years <= 0 -> "${months}个月"
        months == 0 -> "${years}岁"
        else -> "${years}岁${months}个月"
    }
} catch (_: Exception) {
    ""
}

/** 精确年龄，如 "6岁5个月12天" */
fun ageDetail(birthday: String): String = try {
    val birth = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(birthday) ?: return ""
    val now = Calendar.getInstance()
    val b = Calendar.getInstance().apply { time = birth }
    var years = now.get(Calendar.YEAR) - b.get(Calendar.YEAR)
    var months = now.get(Calendar.MONTH) - b.get(Calendar.MONTH)
    var days = now.get(Calendar.DAY_OF_MONTH) - b.get(Calendar.DAY_OF_MONTH)
    if (days < 0) {
        months -= 1
        val prev = Calendar.getInstance().apply { timeInMillis = now.timeInMillis; add(Calendar.MONTH, -1) }
        days += prev.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    if (months < 0) { years -= 1; months += 12 }
    when {
        years <= 0 -> "${months}个月${days}天"
        months == 0 -> "${years}岁${days}天"
        else -> "${years}岁${months}个月${days}天"
    }
} catch (_: Exception) {
    ""
}

/** 距下次生日还有多少天（今天生日则为 0） */
fun daysToNextBirthday(birthday: String): Int? = try {
    val birth = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(birthday) ?: return null
    val now = Calendar.getInstance()
    val next = Calendar.getInstance().apply {
        time = birth
        set(Calendar.YEAR, now.get(Calendar.YEAR))
    }
    if (next.timeInMillis < startOfToday()) next.add(Calendar.YEAR, 1)
    ((startOfDay(next.timeInMillis) - startOfToday()) / 86400000L).toInt()
} catch (_: Exception) {
    null
}

/** 从出生到今天的总天数（成长天数） */
fun daysSinceBirth(birthday: String): Int? = try {
    val birth = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(birthday) ?: return null
    ((startOfToday() - startOfDay(birth.time)) / 86400000L).toInt().coerceAtLeast(0)
} catch (_: Exception) {
    null
}
