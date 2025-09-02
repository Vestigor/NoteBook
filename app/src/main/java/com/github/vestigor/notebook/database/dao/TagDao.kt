package com.github.vestigor.notebook.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.vestigor.notebook.database.entities.Tag
import com.github.vestigor.notebook.models.TagWithCount

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): LiveData<List<Tag>>

    @Query("""
        SELECT t.*, COUNT(n.id) as noteCount 
        FROM tags t 
        LEFT JOIN notes n ON t.id = n.tagId 
        GROUP BY t.id 
        ORDER BY t.name ASC
    """)
    fun getTagsWithCount(): LiveData<List<TagWithCount>>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): Tag?

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTagCount(): Int

    @Insert
    suspend fun insert(tag: Tag): Long

    @Update
    suspend fun update(tag: Tag)

    @Delete
    suspend fun delete(tag: Tag)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteById(tagId: Long)
}