package com.github.vestigor.notebook.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.github.vestigor.notebook.database.NoteDatabase
import com.github.vestigor.notebook.database.entities.Tag
import com.github.vestigor.notebook.models.TagWithCount
import com.github.vestigor.notebook.repository.TagRepository
import kotlinx.coroutines.launch

// 负责提供标签列表、标签数量，并处理标签的增删改操作
class TagManageViewModel(application: Application) : AndroidViewModel(application) {

    // 获取数据库实例和标签仓库
    private val database = NoteDatabase.getDatabase(application)
    private val tagRepository = TagRepository(database.tagDao(), database.noteDao())

    // 带笔记数量的标签列表
    val tagsWithCount: LiveData<List<TagWithCount>> = tagRepository.getTagsWithCount()

    // 标签总数
    private val _tagCount = MutableLiveData<Int>()
    val tagCount: LiveData<Int> = _tagCount

    init {
        // 初始化时加载标签总数
        loadTagCount()
    }

    // 加载标签总数并更新
    private fun loadTagCount() {
        viewModelScope.launch {
            _tagCount.value = tagRepository.getTagCount()
        }
    }

    // 新增标签
    fun addTag(name: String) {
        viewModelScope.launch {
            val tag = Tag(name = name)
            tagRepository.insert(tag)
            loadTagCount()
        }
    }

    // 更新标签名称
    fun updateTag(tagId: Long, newName: String) {
        viewModelScope.launch {
            val tag = tagRepository.getTagById(tagId)
            tag?.let {
                tagRepository.update(it.copy(name = newName))
            }
        }
    }

    // 删除标签
    fun deleteTag(tagId: Long) {
        viewModelScope.launch {
            tagRepository.deleteById(tagId)
            loadTagCount() // 更新标签总数
        }
    }
}