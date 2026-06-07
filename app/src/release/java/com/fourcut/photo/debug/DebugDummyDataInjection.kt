package com.fourcut.photo.debug

import com.fourcut.photo.core.media.AppMediaStorage
import com.fourcut.photo.data.repository.SessionRepository

data class DebugDummySeedResult(
    val sessionCount: Int
)

object DebugDummyDataInjection {
    val isAvailable: Boolean = false

    @Suppress("UNUSED_PARAMETER")
    suspend fun seed(
        sessionRepository: SessionRepository,
        mediaStorage: AppMediaStorage
    ): DebugDummySeedResult {
        return DebugDummySeedResult(sessionCount = 0)
    }
}
