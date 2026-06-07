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
import org.junit.Assert.assertTrue
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
    fun sameDayMultipleQrSessionsStaySeparateAndCanContainMultipleMedia() = runTest {
        val firstId = sessionRepository.saveSession(
            capturedAt = 1_000L,
            sourceQrUrl = "https://sample.invalid/qr/first",
            sourceHost = "sample.invalid",
            sourceLabel = "Sample Booth",
            media = listOf(
                SaveMediaInput(MediaType.IMAGE, "path/first-photo.jpg", "image/jpeg", "first-photo.jpg"),
                SaveMediaInput(MediaType.VIDEO, "path/first-video.mp4", "video/mp4", "first-video.mp4")
            ),
            tagNames = listOf("하진")
        )
        val secondId = sessionRepository.saveSession(
            capturedAt = 2_000L,
            sourceQrUrl = "https://sample.invalid/qr/second",
            sourceHost = "sample.invalid",
            sourceLabel = "Sample Booth",
            media = listOf(
                SaveMediaInput(MediaType.IMAGE, "path/second-photo.jpg", "image/jpeg", "second-photo.jpg"),
                SaveMediaInput(MediaType.VIDEO, "path/second-video.mp4", "video/mp4", "second-video.mp4")
            ),
            tagNames = listOf("정현")
        )

        val first = db.sessionDao().getSessionWithDetails(firstId)
        val second = db.sessionDao().getSessionWithDetails(secondId)
        val sessionsForDay = db.sessionDao().getSessionsForDay(0L, 86_400_000L)

        assertEquals(listOf(firstId, secondId), sessionsForDay.map { it.id })
        assertEquals(1, first.session.sessionIndexForDay)
        assertEquals(2, second.session.sessionIndexForDay)
        assertEquals(listOf(MediaType.IMAGE, MediaType.VIDEO), first.media.map { it.type })
        assertEquals(listOf(MediaType.IMAGE, MediaType.VIDEO), second.media.map { it.type })
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

    @Test
    fun deleteSessionRemovesSessionAndDeletesStoredMedia() = runTest {
        val deletedSessionIds = mutableListOf<Long>()
        val sessionId = sessionRepository.saveSession(
            capturedAt = 500L,
            sourceQrUrl = "https://example.com/qr",
            sourceHost = "example.com",
            sourceLabel = "Example Booth",
            media = listOf(SaveMediaInput(MediaType.IMAGE, "path/image.jpg", "image/jpeg", "image.jpg")),
            tagNames = listOf("Hajin")
        )

        sessionRepository.deleteSession(
            sessionId = sessionId,
            deleteMedia = { deletedSessionIds.add(it) }
        )

        val sessionsForDay = db.sessionDao().getSessionsForDay(
            startMillis = 0L,
            endMillis = 86_400_000L
        )

        assertTrue(sessionsForDay.isEmpty())
        assertEquals(listOf(sessionId), deletedSessionIds)
    }
}
