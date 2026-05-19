package com.fourcut.photo.domain.model

import com.fourcut.photo.data.local.session.MediaType

data class SessionMedia(
    val id: Long,
    val sessionId: Long,
    val type: MediaType,
    val localPath: String,
    val mimeType: String,
    val fileName: String
)
