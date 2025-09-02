package com.github.vestigor.notebook.models

import androidx.room.Embedded
import com.github.vestigor.notebook.database.entities.Note
import java.util.Date

data class NoteWithTag(
    val id: Long,
    val title: String,
    val content: String,
    val formattedContent: String,
    val tagId: Long?,
    val createdDate: Date,
    val modifiedDate: Date,
    val tagName: String?,
    val tagColor: String?
) {
    fun toNote(): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            formattedContent = formattedContent,
            tagId = tagId,
            createdDate = createdDate,
            modifiedDate = modifiedDate
        )
    }
}