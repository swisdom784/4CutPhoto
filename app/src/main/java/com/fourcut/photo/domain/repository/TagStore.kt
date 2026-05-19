package com.fourcut.photo.domain.repository

import com.fourcut.photo.data.local.tag.PersonTagEntity

interface TagStore {
    suspend fun getOrCreateTag(name: String, nowMillis: Long = System.currentTimeMillis()): Long
    suspend fun search(query: String): List<PersonTagEntity>
    suspend fun deleteTag(tagId: Long)
}
