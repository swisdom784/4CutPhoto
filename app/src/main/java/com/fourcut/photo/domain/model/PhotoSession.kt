package com.fourcut.photo.domain.model

data class PhotoSession(
    val id: Long,
    val capturedAt: Long,
    val sourceQrUrl: String,
    val sourceHost: String?,
    val sourceLabel: String?,
    val sessionIndexForDay: Int,
    val media: List<SessionMedia>,
    val tags: List<PersonTag>
)
