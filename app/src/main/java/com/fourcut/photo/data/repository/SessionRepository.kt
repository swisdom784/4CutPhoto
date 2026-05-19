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
    override suspend fun saveSession(
        capturedAt: Long,
        sourceQrUrl: String,
        sourceHost: String?,
        sourceLabel: String?,
        media: List<SaveMediaInput>,
        tagNames: List<String>
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
            sessionDao.insertMedia(
                MediaItemEntity(
                    sessionId = sessionId,
                    type = input.type,
                    localPath = input.localPath,
                    mimeType = input.mimeType,
                    fileName = input.fileName,
                    width = input.width,
                    height = input.height,
                    durationMillis = input.durationMillis,
                    createdAt = now
                )
            )
        }

        if (mediaIds.isNotEmpty()) {
            val saved = sessionDao.getSessionWithDetails(sessionId).session
            sessionDao.updateSession(saved.copy(coverMediaId = mediaIds.first(), updatedAt = now))
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
