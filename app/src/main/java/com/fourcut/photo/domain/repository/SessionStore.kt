package com.fourcut.photo.domain.repository

import com.fourcut.photo.data.repository.SaveMediaInput

interface SessionStore {
    suspend fun saveSession(
        capturedAt: Long,
        sourceQrUrl: String,
        sourceHost: String?,
        sourceLabel: String?,
        media: List<SaveMediaInput>,
        tagNames: List<String>,
        persistMedia: suspend (Long, SaveMediaInput) -> SaveMediaInput = { _, input -> input }
    ): Long
}
