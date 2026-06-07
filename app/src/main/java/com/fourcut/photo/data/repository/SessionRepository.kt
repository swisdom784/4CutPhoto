package com.fourcut.photo.data.repository

import com.fourcut.photo.data.local.session.MediaItemEntity
import com.fourcut.photo.data.local.session.MediaType
import com.fourcut.photo.data.local.session.PhotoSessionEntity
import com.fourcut.photo.data.local.session.SessionDao
import com.fourcut.photo.data.local.session.SessionTagCrossRef
import com.fourcut.photo.domain.repository.SessionStore
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class SaveMediaInput(
    val type: MediaType,
    val localPath: String,
    val mimeType: String,
    val fileName: String,
    val width: Int? = null,
    val height: Int? = null,
    val durationMillis: Long? = null
)

class SessionRepository(
    private val sessionDao: SessionDao,
    private val tagRepository: TagRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : SessionStore {
    suspend fun saveSession(
        capturedAt: Long,
        sourceQrUrl: String,
        sourceHost: String?,
        sourceLabel: String?,
        media: List<SaveMediaInput>,
        tagNames: List<String>
    ): Long {
        return saveSession(
            capturedAt = capturedAt,
            sourceQrUrl = sourceQrUrl,
            sourceHost = sourceHost,
            sourceLabel = sourceLabel,
            media = media,
            tagNames = tagNames,
            persistMedia = { _, input -> input }
        )
    }

    override suspend fun saveSession(
        capturedAt: Long,
        sourceQrUrl: String,
        sourceHost: String?,
        sourceLabel: String?,
        media: List<SaveMediaInput>,
        tagNames: List<String>,
        persistMedia: suspend (Long, SaveMediaInput) -> SaveMediaInput
    ): Long {
        val now = System.currentTimeMillis()
        val sessionIndex = nextSessionIndexForDay(capturedAt)
        val sessionId = sessionDao.insertSession(
            PhotoSessionEntity(
                capturedAt = capturedAt,
                sourceQrUrl = sourceQrUrl,
                sourceHost = sourceHost,
                sourceLabel = sourceLabel,
                sessionIndexForDay = sessionIndex,
                createdAt = now,
                updatedAt = now
            )
        )

        val mediaIds = media.map { input ->
            val persistedInput = persistMedia(sessionId, input)
            val mediaId = sessionDao.insertMedia(
                MediaItemEntity(
                    sessionId = sessionId,
                    type = persistedInput.type,
                    localPath = persistedInput.localPath,
                    mimeType = persistedInput.mimeType,
                    fileName = persistedInput.fileName,
                    width = persistedInput.width,
                    height = persistedInput.height,
                    durationMillis = persistedInput.durationMillis,
                    createdAt = now
                )
            )
            persistedInput.type to mediaId
        }

        if (mediaIds.isNotEmpty()) {
            val saved = sessionDao.getSessionWithDetails(sessionId).session
            val coverMediaId = mediaIds.firstOrNull { it.first == MediaType.IMAGE }?.second
                ?: mediaIds.first().second
            sessionDao.updateSession(saved.copy(coverMediaId = coverMediaId, updatedAt = now))
        }

        tagNames.asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .forEach { tagName ->
                val tagId = tagRepository.getOrCreateTag(tagName, now)
                sessionDao.insertSessionTag(SessionTagCrossRef(sessionId = sessionId, tagId = tagId))
            }

        return sessionId
    }

    suspend fun replaceSessionTags(
        sessionId: Long,
        tagNames: List<String>
    ) {
        val now = System.currentTimeMillis()
        sessionDao.deleteTagsForSession(sessionId)
        tagNames.asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .forEach { tagName ->
                val tagId = tagRepository.getOrCreateTag(tagName, now)
                sessionDao.insertSessionTag(SessionTagCrossRef(sessionId = sessionId, tagId = tagId))
            }
        val saved = sessionDao.getSessionWithDetails(sessionId).session
        sessionDao.updateSession(saved.copy(updatedAt = now))
    }

    suspend fun deleteSession(
        sessionId: Long,
        deleteMedia: suspend (Long) -> Unit
    ) {
        sessionDao.deleteSessionById(sessionId)
        deleteMedia(sessionId)
    }

    private suspend fun nextSessionIndexForDay(capturedAt: Long): Int {
        val start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(capturedAt), zoneId)
            .toLocalDate()
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        val end = ZonedDateTime.ofInstant(Instant.ofEpochMilli(start), zoneId)
            .plusDays(1)
            .toInstant()
            .toEpochMilli()
        return sessionDao.getSessionsForDay(start, end).size + 1
    }
}
