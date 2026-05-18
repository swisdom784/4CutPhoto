package com.fourcut.photo.data.local.session

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MediaType {
    IMAGE,
    VIDEO
}

@Entity(
    tableName = "media_items",
    foreignKeys = [
        ForeignKey(
            entity = PhotoSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class MediaItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sessionId: Long,
    val type: MediaType,
    val localPath: String,
    val mimeType: String,
    val fileName: String,
    val width: Int? = null,
    val height: Int? = null,
    val durationMillis: Long? = null,
    val createdAt: Long
)
