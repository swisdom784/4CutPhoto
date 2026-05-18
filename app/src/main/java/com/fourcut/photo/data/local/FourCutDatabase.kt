package com.fourcut.photo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fourcut.photo.data.local.session.MediaItemEntity
import com.fourcut.photo.data.local.session.PhotoSessionEntity
import com.fourcut.photo.data.local.session.SessionDao
import com.fourcut.photo.data.local.session.SessionTagCrossRef
import com.fourcut.photo.data.local.tag.PersonTagDao
import com.fourcut.photo.data.local.tag.PersonTagEntity

@Database(
    entities = [
        PhotoSessionEntity::class,
        MediaItemEntity::class,
        PersonTagEntity::class,
        SessionTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class FourCutDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun personTagDao(): PersonTagDao
}
