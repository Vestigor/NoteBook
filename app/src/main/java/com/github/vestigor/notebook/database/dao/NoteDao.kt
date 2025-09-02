package com.github.vestigor.notebook.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.vestigor.notebook.database.entities.Note
import com.github.vestigor.notebook.models.NoteWithTag
import java.util.Date

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY modifiedDate DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("""
        SELECT n.*, t.name as tagName, t.color as tagColor 
        FROM notes n 
        LEFT JOIN tags t ON n.tagId = t.id 
        ORDER BY n.modifiedDate DESC
    """)
    fun getAllNotesWithTags(): LiveData<List<NoteWithTag>>

    @Query("""
        SELECT n.*, t.name as tagName, t.color as tagColor 
        FROM notes n 
        LEFT JOIN tags t ON n.tagId = t.id 
        WHERE n.tagId = :tagId 
        ORDER BY n.modifiedDate DESC
    """)
    fun getNotesByTag(tagId: Long): LiveData<List<NoteWithTag>>

    @Query("""
        SELECT n.*, t.name as tagName, t.color as tagColor 
        FROM notes n 
        LEFT JOIN tags t ON n.tagId = t.id 
        WHERE n.title LIKE '%' || :keyword || '%' 
        ORDER BY n.modifiedDate DESC
    """)
    fun searchNotes(keyword: String): LiveData<List<NoteWithTag>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): Note?

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteById(noteId: Long)

    @Query("UPDATE notes SET tagId = NULL WHERE tagId = :tagId")
    suspend fun removeTagFromNotes(tagId: Long)
}