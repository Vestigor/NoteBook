package com.github.vestigor.notebook.repository

import androidx.lifecycle.LiveData
import com.github.vestigor.notebook.database.dao.NoteDao
import com.github.vestigor.notebook.database.dao.TagDao
import com.github.vestigor.notebook.database.entities.Tag
import com.github.vestigor.notebook.models.TagWithCount

class TagRepository(
    private val tagDao: TagDao,
    private val noteDao: NoteDao
) {

    fun getAllTags(): LiveData<List<Tag>> = tagDao.getAllTags()

    fun getTagsWithCount(): LiveData<List<TagWithCount>> = tagDao.getTagsWithCount()

    suspend fun getTagById(tagId: Long): Tag? = tagDao.getTagById(tagId)

    suspend fun getTagCount(): Int = tagDao.getTagCount()

    suspend fun insert(tag: Tag): Long = tagDao.insert(tag)

    suspend fun update(tag: Tag) = tagDao.update(tag)

    suspend fun delete(tag: Tag) {
        // 删除标签时，将相关笔记的标签设为null
        noteDao.removeTagFromNotes(tag.id)
        tagDao.delete(tag)
    }

    suspend fun deleteById(tagId: Long) {
        noteDao.removeTagFromNotes(tagId)
        tagDao.deleteById(tagId)
    }
}