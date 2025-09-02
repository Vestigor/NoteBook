package com.github.vestigor.notebook.repository

import androidx.lifecycle.LiveData
import com.github.vestigor.notebook.database.dao.NoteDao
import com.github.vestigor.notebook.database.entities.Note
import com.github.vestigor.notebook.models.NoteWithTag
import java.util.Date

class NoteRepository(private val noteDao: NoteDao) {

    fun getAllNotesWithTags(): LiveData<List<NoteWithTag>> = noteDao.getAllNotesWithTags()

    fun getNotesByTag(tagId: Long): LiveData<List<NoteWithTag>> = noteDao.getNotesByTag(tagId)

    fun searchNotes(keyword: String): LiveData<List<NoteWithTag>> = noteDao.searchNotes(keyword)

    suspend fun getNoteById(noteId: Long): Note? = noteDao.getNoteById(noteId)

    suspend fun insert(note: Note): Long = noteDao.insert(note)

    suspend fun update(note: Note) {
        val updatedNote = note.copy(modifiedDate = Date())
        noteDao.update(updatedNote)
    }

    suspend fun delete(note: Note) = noteDao.delete(note)

    suspend fun deleteById(noteId: Long) = noteDao.deleteById(noteId)
}