package com.fourcut.photo.data.local.tag

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "person_tags",
    indices = [Index(value = ["name"], unique = true)]
)
data class PersonTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val createdAt: Long,
    val lastUsedAt: Long
)
