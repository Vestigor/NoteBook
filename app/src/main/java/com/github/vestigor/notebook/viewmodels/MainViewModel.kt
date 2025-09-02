package com.github.vestigor.notebook.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.github.vestigor.notebook.database.NoteDatabase
import com.github.vestigor.notebook.database.entities.Note
import com.github.vestigor.notebook.database.entities.Tag
import com.github.vestigor.notebook.models.NoteWithTag
import com.github.vestigor.notebook.repository.NoteRepository
import com.github.vestigor.notebook.repository.TagRepository
import kotlinx.coroutines.launch

// 管理笔记和标签的展示、选择和删除操作
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // 获取数据库实例
    private val database = NoteDatabase.getDatabase(application)
    // 笔记仓库，用于操作 Note 数据
    private val noteRepository = NoteRepository(database.noteDao())
    // 标签仓库，用于操作 Tag 数据及其与笔记的关联
    private val tagRepository = TagRepository(database.tagDao(), database.noteDao())

    // 当前选中的标签 ID，null 表示不筛选
    private val _selectedTagId = MutableLiveData<Long?>()
    val selectedTagId: LiveData<Long?> = _selectedTagId

    // 根据选中的标签动态获取笔记列表：如果未选中标签，返回所有笔记； 否则返回指定标签下的笔记
    val notes: LiveData<List<NoteWithTag>> = _selectedTagId.switchMap { tagId ->
        if (tagId == null) {
            noteRepository.getAllNotesWithTags()
        } else {
            noteRepository.getNotesByTag(tagId)
        }
    }

    // 获取所有标签列表
    val tags: LiveData<List<Tag>> = tagRepository.getAllTags()

    fun selectTag(tagId: Long?) {
        _selectedTagId.value = tagId
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            noteRepository.deleteById(noteId)
        }
    }

    fun exportNoteToTxt(note: Note) {
        // 在Activity中处理
    }

    fun exportNoteToPdf(note: Note) {
        // 在Activity中处理
    }
}