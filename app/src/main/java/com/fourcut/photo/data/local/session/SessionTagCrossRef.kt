package com.fourcut.photo.data.local.session

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.fourcut.photo.data.local.tag.PersonTagEntity

@Entity(
    tableName = "session_tag_cross_ref",
    primaryKeys = ["sessionId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = PhotoSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonTagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("tagId")]
)
data class SessionTagCrossRef(
    val sessionId: Long,
    val tagId: Long
)
