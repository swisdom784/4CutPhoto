package com.fourcut.photo.data.local.tag

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PersonTagDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tag: PersonTagEntity): Long

    @Update
    suspend fun update(tag: PersonTagEntity)

    @Query("SELECT * FROM person_tags ORDER BY lastUsedAt DESC, name COLLATE NOCASE ASC")
    suspend fun getAll(): List<PersonTagEntity>

    @Query(
        """
        SELECT * FROM person_tags
        WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%'
        ORDER BY lastUsedAt DESC, name COLLATE NOCASE ASC
        """
    )
    suspend fun searchByName(query: String): List<PersonTagEntity>

    @Query("SELECT * FROM person_tags WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): PersonTagEntity?

    @Query("DELETE FROM person_tags WHERE id = :tagId")
    suspend fun deleteById(tagId: Long)
}
