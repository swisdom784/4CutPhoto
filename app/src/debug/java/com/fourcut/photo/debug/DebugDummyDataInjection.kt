package com.fourcut.photo.debug

import com.fourcut.photo.core.media.AppMediaStorage
import com.fourcut.photo.data.local.session.MediaType
import com.fourcut.photo.data.repository.SaveMediaInput
import com.fourcut.photo.data.repository.SessionRepository
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicLong

data class DebugDummySeedResult(
    val sessionCount: Int
)

object DebugDummyDataInjection {
    val isAvailable: Boolean = true

    suspend fun seed(
        sessionRepository: SessionRepository,
        mediaStorage: AppMediaStorage
    ): DebugDummySeedResult {
        val seedId = nextSeedId()
        return seed(
            sessionRepository = sessionRepository,
            mediaStorage = mediaStorage,
            seedId = seedId,
            capturedAt = seedId
        )
    }

    internal suspend fun seed(
        sessionRepository: SessionRepository,
        mediaStorage: AppMediaStorage,
        seedId: Long,
        capturedAt: Long
    ): DebugDummySeedResult {
        val plan = buildDummySessionSeedPlan(seedId)

        plan.forEach { dummy ->
            sessionRepository.saveSession(
                capturedAt = capturedAt,
                sourceQrUrl = dummy.sourceQrUrl,
                sourceHost = dummy.sourceHost,
                sourceLabel = dummy.sourceLabel,
                media = dummy.media,
                tagNames = dummy.tagNames,
                persistMedia = { sessionId, input ->
                    input.persistDummyMedia(sessionId, mediaStorage)
                }
            )
        }

        return DebugDummySeedResult(sessionCount = plan.size)
    }

    private fun SaveMediaInput.persistDummyMedia(
        sessionId: Long,
        mediaStorage: AppMediaStorage
    ): SaveMediaInput {
        val bytes = if (type == MediaType.IMAGE) {
            dummyPngBytes
        } else {
            "4CutPhoto debug dummy video placeholder".toByteArray()
        }
        val file = mediaStorage.saveOriginal(sessionId, fileName, ByteArrayInputStream(bytes))
        return copy(localPath = file.absolutePath)
    }
}

private val lastSeedId = AtomicLong(0L)

private fun nextSeedId(): Long {
    while (true) {
        val current = lastSeedId.get()
        val candidate = maxOf(System.currentTimeMillis(), current + 1)
        if (lastSeedId.compareAndSet(current, candidate)) {
            return candidate
        }
    }
}

private val dummyPngBytes = byteArrayOf(
    -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82,
    0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 31, 21, -60,
    -119, 0, 0, 0, 13, 73, 68, 65, 84, 120, -100, 99, -8, -49, -64,
    -16, 31, 0, 5, 0, 1, -1, -89, -103, -99, -93, 0, 0, 0, 0,
    73, 69, 78, 68, -82, 66, 96, -126
)
