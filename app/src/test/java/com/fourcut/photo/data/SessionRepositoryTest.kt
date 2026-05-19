package com.fourcut.photo.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fourcut.photo.data.local.FourCutDatabase
import com.fourcut.photo.data.local.session.MediaType
import com.fourcut.photo.data.repository.SaveMediaInput
import com.fourcut.photo.data.repository.SessionRepository
import com.fourcut.photo.data.repository.TagRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SessionRepositoryTest {
    private lateinit var db: FourCutDatabase
    private lateinit var tagRepository: TagRepository
    private lateinit var sessionRepository: SessionRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, FourCutDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        tagRepository = TagRepository(db.personTagDao())
        sessionRepository = SessionRepository(db.sessionDao(), tagRepository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun saveSessionCreatesTagsAndAppliesThem() = runTest {
        val sessionId = sessionRepository.saveSession(
            capturedAt = 100L,
            sourceQrUrl = "https://example.com/qr",
            sourceHost = "example.com",
            sourceLabel = "Example Booth",
            media = listOf(
                SaveMediaInput(MediaType.IMAGE, "path/image.jpg", "image/jpeg", "image.jpg")
            ),
            tagNames = listOf("Hajin", "JungHyun")
        )

        val saved = db.sessionDao().getSessionWithDetails(sessionId)

        assertEquals(1, saved.media.size)
        assertEquals(listOf("Hajin", "JungHyun"), saved.tags.map { it.name }.sorted())
    }

    @Test
    fun sameDaySessionsReceiveSequentialIndexes() = runTest {
        val firstId = sessionRepository.saveSession(
            capturedAt = 100L,
            sourceQrUrl = "https://example.com/first",
            sourceHost = "example.com",
            sourceLabel = "Example Booth",
            media = listOf(SaveMediaInput(MediaType.IMAGE, "path/first.jpg", "image/jpeg", "first.jpg")),
            tagNames = emptyList()
        )
        val secondId = sessionRepository.saveSession(
            capturedAt = 200L,
            sourceQrUrl = "https://example.com/second",
            sourceHost = "example.com",
            sourceLabel = "Example Booth",
            media = listOf(SaveMediaInput(MediaType.IMAGE, "path/second.jpg", "image/jpeg", "second.jpg")),
            tagNames = emptyList()
        )

        val first = db.sessionDao().getSessionWithDetails(firstId).session
        val second = db.sessionDao().getSessionWithDetails(secondId).session

        assertEquals(1, first.sessionIndexForDay)
        assertEquals(2, second.sessionIndexForDay)
    }

    @Test
    fun saveSessionCanPersistMediaAfterSessionIdIsCreated() = runTest {
        val sessionId = sessionRepository.saveSession(
            capturedAt = 300L,
            sourceQrUrl = "https://example.com/qr",
            sourceHost = "example.com",
            sourceLabel = "Example Booth",
            media = listOf(SaveMediaInput(MediaType.IMAGE, "https://example.com/image.jpg", "image/jpeg", "image.jpg")),
            tagNames = emptyList(),
            persistMedia = { createdSessionId, input ->
                input.copy(localPath = "media/sessions/$createdSessionId/original/${input.fileName}")
            }
        )

        val saved = db.sessionDao().getSessionWithDetails(sessionId)

        assertEquals("media/sessions/$sessionId/original/image.jpg", saved.media.first().localPath)
    }

    @Test
    fun replaceSessionTagsRemovesOldTagsAndAppliesNewTags() = runTest {
        val sessionId = sessionRepository.saveSession(
            capturedAt = 400L,
            sourceQrUrl = "https://example.com/qr",
            sourceHost = "example.com",
            sourceLabel = "Example Booth",
            media = listOf(SaveMediaInput(MediaType.IMAGE, "path/image.jpg", "image/jpeg", "image.jpg")),
            tagNames = listOf("Hajin", "JungHyun")
        )

        sessionRepository.replaceSessionTags(sessionId, listOf("Minji", "Hajin"))

        val saved = db.sessionDao().getSessionWithDetails(sessionId)

        assertEquals(listOf("Hajin", "Minji"), saved.tags.map { it.name }.sorted())
    }
}
