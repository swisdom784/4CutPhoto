package com.fourcut.photo.data.repository

import com.fourcut.photo.data.local.tag.PersonTagDao
import com.fourcut.photo.data.local.tag.PersonTagEntity
import com.fourcut.photo.domain.repository.TagStore

class TagRepository(
    private val dao: PersonTagDao
) : TagStore {
    override suspend fun getOrCreateTag(name: String, nowMillis: Long): Long {
        val normalized = name.trim()
        require(normalized.isNotBlank()) { "Tag name cannot be blank." }

        val existing = dao.findByName(normalized)
        if (existing != null) {
            dao.update(existing.copy(lastUsedAt = nowMillis))
            return existing.id
        }

        return dao.insert(
            PersonTagEntity(
                name = normalized,
                createdAt = nowMillis,
                lastUsedAt = nowMillis
            )
        )
    }

    override suspend fun search(query: String): List<PersonTagEntity> {
        val normalized = query.trim()
        return if (normalized.isBlank()) {
            dao.getAll()
        } else {
            dao.searchByName(normalized)
        }
    }

    override suspend fun deleteTag(tagId: Long) {
        dao.deleteById(tagId)
    }
}
