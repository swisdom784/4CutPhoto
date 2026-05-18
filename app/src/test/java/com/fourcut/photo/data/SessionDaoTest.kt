package com.fourcut.photo.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fourcut.photo.data.local.FourCutDatabase
import com.fourcut.photo.data.local.session.MediaItemEntity
import com.fourcut.photo.data.local.session.MediaType
import com.fourcut.photo.data.local.session.PhotoSessionEntity
import com.fourcut.photo.data.local.session.SessionTagCrossRef
import com.fourcut.photo.data.local.tag.PersonTagEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SessionDaoTest {
    private lateinit var db: FourCutDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, FourCutDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun sessionWithMediaAndTagsCanBeLoaded() = runTest {
        val sessionId = db.sessionDao().insertSession(
            PhotoSessionEntity(
                capturedAt = 10L,
                sourceQrUrl = "https://example.com/qr",
                sourceHost = "example.com",
                sourceLabel = "Example Booth",
                sessionIndexForDay = 1,
                createdAt = 10L,
                updatedAt = 10L
            )
        )
        db.sessionDao().insertMedia(
            MediaItemEntity(
                sessionId = sessionId,
                type = MediaType.IMAGE,
                localPath = "media/sessions/$sessionId/original/image_001.jpg",
                mimeType = "image/jpeg",
                fileName = "image_001.jpg",
                createdAt = 10L
            )
        )
        val tagId = db.personTagDao().insert(PersonTagEntity(name = "Hajin", createdAt = 10L, lastUsedAt = 10L))
        db.sessionDao().insertSessionTag(SessionTagCrossRef(sessionId = sessionId, tagId = tagId))

        val result = db.sessionDao().getSessionWithDetails(sessionId)

        assertEquals("https://example.com/qr", result.session.sourceQrUrl)
        assertEquals(1, result.media.size)
        assertEquals("Hajin", result.tags.first().name)
    }

    @Test
    fun sessionsFromSameDayRemainSeparate() = runTest {
        val firstSessionId = db.sessionDao().insertSession(
            PhotoSessionEntity(
                capturedAt = 10L,
                sourceQrUrl = "https://booth-a.example/qr-1",
                sourceHost = "booth-a.example",
                sourceLabel = "Booth A",
                sessionIndexForDay = 1,
                createdAt = 10L,
                updatedAt = 10L
            )
        )
        val secondSessionId = db.sessionDao().insertSession(
            PhotoSessionEntity(
                capturedAt = 20L,
                sourceQrUrl = "https://booth-b.example/qr-2",
                sourceHost = "booth-b.example",
                sourceLabel = "Booth B",
                sessionIndexForDay = 2,
                createdAt = 20L,
                updatedAt = 20L
            )
        )

        val sessions = db.sessionDao().getSessionsForDay(startMillis = 0L, endMillis = 86_400_000L)

        assertEquals(listOf(firstSessionId, secondSessionId), sessions.map { it.id })
        assertEquals(listOf(1, 2), sessions.map { it.sessionIndexForDay })
    }
}
