package com.github.vestigor.notebook.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index  // 添加这个import
import java.util.Date

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("tagId")]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val formattedContent: String,
    val tagId: Long? = null,
    val createdDate: Date = Date(),
    val modifiedDate: Date = Date()
)