package com.github.vestigor.notebook.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.vestigor.notebook.database.converters.DateConverter
import com.github.vestigor.notebook.database.dao.NoteDao
import com.github.vestigor.notebook.database.dao.TagDao
import com.github.vestigor.notebook.database.entities.Note
import com.github.vestigor.notebook.database.entities.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Room 数据库类，用于管理 Note 和 Tag 两个实体
// 同时指定 DateConverter 将 Date 类型转换为 Long 存储
@Database(
    entities = [Note::class, Tag::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                    .addCallback(DatabaseCallback())   // 注册回调函数，用于数据库创建时初始化数据
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // 在数据库首次创建时初始化默认数据
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    // 添加默认标签
                    val tagDao = database.tagDao()
                    tagDao.insert(Tag(name = "工作", color = "#FF6B6B"))
                    tagDao.insert(Tag(name = "生活", color = "#4ECDC4"))
                    tagDao.insert(Tag(name = "学习", color = "#45B7D1"))
                    tagDao.insert(Tag(name = "个人", color = "#96CEB4"))
                }
            }
        }
    }
}