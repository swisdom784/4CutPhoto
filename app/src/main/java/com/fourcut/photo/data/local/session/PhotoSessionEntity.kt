package com.fourcut.photo.data.local.session

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo_sessions")
data class PhotoSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val capturedAt: Long,
    val sourceQrUrl: String,
    val sourceHost: String? = null,
    val sourceLabel: String? = null,
    val sessionIndexForDay: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val coverMediaId: Long? = null
)
