package com.github.vestigor.notebook.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.github.vestigor.notebook.database.NoteDatabase
import com.github.vestigor.notebook.database.entities.Note
import com.github.vestigor.notebook.database.entities.Tag
import com.github.vestigor.notebook.repository.NoteRepository
import com.github.vestigor.notebook.repository.TagRepository
import kotlinx.coroutines.launch
import java.util.Date

// 管理笔记的加载、保存以及标签选择逻辑
class NoteEditViewModel(application: Application) : AndroidViewModel(application) {

    // 数据库和仓库初始化
    private val database = NoteDatabase.getDatabase(application)
    private val noteRepository = NoteRepository(database.noteDao())
    private val tagRepository = TagRepository(database.tagDao(), database.noteDao())

    // 当前编辑的笔记
    private val _currentNote = MutableLiveData<Note?>()
    val currentNote: LiveData<Note?> = _currentNote

    // 当前选中的标签
    private val _selectedTag = MutableLiveData<Tag?>()
    val selectedTag: LiveData<Tag?> = _selectedTag

    // 所有标签列表
    val tags: LiveData<List<Tag>> = tagRepository.getAllTags()

    private var noteId: Long = 0  // 当前笔记 ID
    private var isNewNote = true  // 是否为新建笔记

    fun loadNote(id: Long) {
        if (id > 0) {
            noteId = id
            isNewNote = false
            viewModelScope.launch {
                val note = noteRepository.getNoteById(id)
                _currentNote.value = note
                note?.tagId?.let { tagId ->
                    _selectedTag.value = tagRepository.getTagById(tagId)
                }
            }
        }
    }

    // 选择标签
    fun selectTag(tag: Tag?) {
        _selectedTag.value = tag
    }

    // 保存笔记
    fun saveNote(title: String, content: String, formattedContent: String): Long {
        var savedNoteId: Long = -1
        viewModelScope.launch {
            val note = if (isNewNote) {
                Note(
                    title = title,
                    content = content,
                    formattedContent = formattedContent,
                    tagId = _selectedTag.value?.id,
                    createdDate = Date(),
                    modifiedDate = Date()
                )
            } else {
                _currentNote.value?.copy(
                    title = title,
                    content = content,
                    formattedContent = formattedContent,
                    tagId = _selectedTag.value?.id,
                    modifiedDate = Date()
                ) ?: return@launch
            }

            savedNoteId = if (isNewNote) {
                noteRepository.insert(note)
            } else {
                noteRepository.update(note)
                note.id
            }
        }
        return savedNoteId
    }
}