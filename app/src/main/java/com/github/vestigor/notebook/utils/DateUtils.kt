package com.github.vestigor.notebook.utils

import java.text.SimpleDateFormat
import java.util.*

// 日期工具类
object DateUtils {
    // 仅显示日期
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    // 仅显示时间
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    // 显示完整日期和时间
    private val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun formatDate(date: Date): String {
        val now = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply { time = date }

        return when {
            isSameDay(now, dateCalendar) -> "今天 ${timeFormat.format(date)}"
            isYesterday(now, dateCalendar) -> "昨天 ${timeFormat.format(date)}"
            isSameYear(now, dateCalendar) -> {
                val monthDayFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                monthDayFormat.format(date)
            }
            else -> fullFormat.format(date)
        }
    }

    // 判断两个日期是否在同一天
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // 判断日期是否为昨天
    private fun isYesterday(now: Calendar, date: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            time = now.time
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(yesterday, date)
    }
// 判断两个日期是否在同一年
    private fun isSameYear(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }
}