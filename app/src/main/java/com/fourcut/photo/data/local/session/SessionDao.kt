package com.fourcut.photo.data.local.session

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.fourcut.photo.data.local.tag.PersonTagEntity
import kotlinx.coroutines.flow.Flow

data class SessionWithDetails(
    @Embedded val session: PhotoSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val media: List<MediaItemEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SessionTagCrossRef::class,
            parentColumn = "sessionId",
            entityColumn = "tagId"
        )
    )
    val tags: List<PersonTagEntity>
)

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: PhotoSessionEntity): Long

    @Update
    suspend fun updateSession(session: PhotoSessionEntity)

    @Insert
    suspend fun insertMedia(media: MediaItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSessionTag(crossRef: SessionTagCrossRef)

    @Transaction
    @Query("SELECT * FROM photo_sessions WHERE id = :sessionId")
    suspend fun getSessionWithDetails(sessionId: Long): SessionWithDetails

    @Query(
        """
        SELECT * FROM photo_sessions
        WHERE capturedAt >= :startMillis AND capturedAt < :endMillis
        ORDER BY capturedAt ASC, id ASC
        """
    )
    suspend fun getSessionsForDay(startMillis: Long, endMillis: Long): List<PhotoSessionEntity>

    @Transaction
    @Query("SELECT * FROM photo_sessions ORDER BY capturedAt DESC, id DESC")
    fun observeSessionsWithDetails(): Flow<List<SessionWithDetails>>
}
