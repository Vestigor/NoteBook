package com.github.vestigor.notebook.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.github.vestigor.notebook.database.NoteDatabase
import com.github.vestigor.notebook.models.NoteWithTag
import com.github.vestigor.notebook.repository.NoteRepository

// 负责根据关键字查询笔记，并将结果提供给 UI
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    // 数据库和仓库初始化
    private val database = NoteDatabase.getDatabase(application)
    private val noteRepository = NoteRepository(database.noteDao())

    // 搜索关键字的 LiveData
    private val _searchKeyword = MutableLiveData<String>()

    // 根据 _searchKeyword 的变化自动更新
    val searchResults: LiveData<List<NoteWithTag>> = _searchKeyword.switchMap { keyword ->
        if (keyword.isNullOrEmpty()) {
            // 关键字为空时返回空列表
            MutableLiveData(emptyList())
        } else {
            // 调用仓库搜索笔记
            noteRepository.searchNotes(keyword)
        }
    }

    // 设置搜索关键字，从而触发搜索结果更新
    fun search(keyword: String) {
        _searchKeyword.value = keyword
    }
}