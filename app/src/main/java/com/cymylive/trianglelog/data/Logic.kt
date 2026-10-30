package com.cymylive.trianglelog.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** 日期工具：统一用 yyyy-MM-dd 字符串 */
object Dates {
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): String = LocalDate.now().format(fmt)

    fun parse(s: String): LocalDate? = try {
        LocalDate.parse(s, fmt)
    } catch (e: Exception) {
        null
    }

    fun key(d: LocalDate): String = d.format(fmt)

    /** 某天的中文显示，如 10月31日 周五 */
    fun pretty(s: String): String {
        val d = parse(s) ?: return s
        val week = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
            .getOrNull(d.dayOfWeek.value - 1) ?: ""
        val md = d.monthValue
        val dd = d.dayOfMonth
        return md.toString() + "月" + dd.toString() + "日 " + week
    }
}

/** 统计结果 */
data class Stats(
    val totalDays: Int,
    val currentStreak: Int,
    val maxStreak: Int,
    val yearDays: Int
)

object Logic {

    fun recordOf(data: AppData, date: String, goalId: String): DayRecord? =
        data.records.firstOrNull { it.date == date && it.goalId == goalId }

    fun doneDates(data: AppData, goalId: String? = null): Set<String> =
        data.records
            .filter { it.done && (goalId == null || it.goalId == goalId) }
            .map { it.date }
            .toSet()

    /** 连续完成天数：从今天往回数，今天没做则从昨天算起 */
    fun currentStreak(dates: Set<String>): Int {
        if (dates.isEmpty()) return 0
        var d = LocalDate.now()
        if (!dates.contains(Dates.key(d))) {
            d = d.minusDays(1)
            if (!dates.contains(Dates.key(d))) return 0
        }
        var n = 0
        while (dates.contains(Dates.key(d))) {
            n++
            d = d.minusDays(1)
        }
        return n
    }

    fun maxStreak(dates: Set<String>): Int {
        if (dates.isEmpty()) return 0
        val sorted = dates.mapNotNull { Dates.parse(it) }.sorted()
        var best = 1
        var cur = 1
        for (i in 1 until sorted.size) {
            cur = if (sorted[i] == sorted[i - 1].plusDays(1)) cur + 1 else 1
            if (cur > best) best = cur
        }
        return best
    }

    fun stats(data: AppData, goalId: String? = null): Stats {
        val dates = doneDates(data, goalId)
        val year = LocalDate.now().year.toString()
        val yearDays = dates.count { it.startsWith(year) }
        return Stats(
            totalDays = dates.size,
            currentStreak = currentStreak(dates),
            maxStreak = maxStreak(dates),
            yearDays = yearDays
        )
    }
}
