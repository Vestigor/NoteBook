package com.github.vestigor.notebook.database.converters

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {
    // 将数据库中存储的 Long（时间戳）转换为 Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    // 将 Date 转换为 Long（时间戳），用于存储到数据库
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}