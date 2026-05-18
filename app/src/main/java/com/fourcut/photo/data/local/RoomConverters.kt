package com.fourcut.photo.data.local

import androidx.room.TypeConverter
import com.fourcut.photo.data.local.session.MediaType

class RoomConverters {
    @TypeConverter
    fun mediaTypeToString(type: MediaType): String = type.name

    @TypeConverter
    fun stringToMediaType(value: String): MediaType = MediaType.valueOf(value)
}
