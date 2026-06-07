package com.fourcut.photo.debug

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fourcut.photo.core.media.AppMediaStorage
import com.fourcut.photo.data.local.FourCutDatabase
import com.fourcut.photo.data.local.session.MediaType
import com.fourcut.photo.data.repository.SessionRepository
import com.fourcut.photo.data.repository.TagRepository
import java.io.File
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DebugDummyDataInjectionTest {
    private lateinit var context: Context
    private lateinit var db: FourCutDatabase
    private lateinit var tagRepository: TagRepository
    private lateinit var sessionRepository: SessionRepository
    private lateinit var mediaStorage: AppMediaStorage

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, FourCutDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        tagRepository = TagRepository(db.personTagDao())
        sessionRepository = SessionRepository(db.sessionDao(), tagRepository)
        mediaStorage = AppMediaStorage(context)
        File(context.filesDir, "media").deleteRecursively()
    }

    @After
    fun tearDown() {
        File(context.filesDir, "media").deleteRecursively()
        db.close()
    }

    @Test
    fun seedStoresSeparateSameDaySessionsWithLocalMediaAndTags() = runTest {
        val result = DebugDummyDataInjection.seed(
            sessionRepository = sessionRepository,
            mediaStorage = mediaStorage,
            seedId = 100L,
            capturedAt = 1_700_000_000_000L
        )

        val sessions = db.sessionDao().observeSessionsWithDetails().first()
            .sortedBy { it.session.sessionIndexForDay }

        assertEquals(3, result.sessionCount)
        assertEquals(3, sessions.size)
        assertEquals(listOf(1, 2, 3), sessions.map { it.session.sessionIndexForDay })
        assertEquals(
            listOf(
                "https://sample.invalid/qr/dummy-100-1",
                "https://sample.invalid/qr/dummy-100-2",
                "https://sample.invalid/qr/dummy-100-3"
            ),
            sessions.map { it.session.sourceQrUrl }
        )
        assertTrue(sessions.all { it.session.sourceHost == "sample.invalid" })
        assertEquals(listOf("더미 QR 1", "더미 QR 2", "더미 QR 3"), sessions.map { it.session.sourceLabel })
        sessions.forEach { session ->
            assertTrue(session.media.any { it.type == MediaType.IMAGE })
            assertTrue(session.media.any { it.type == MediaType.VIDEO })
            session.media.forEach { media ->
                assertTrue(media.localPath.isNotBlank())
                assertFalse(media.localPath.startsWith("http"))
                assertTrue(File(media.localPath).exists())
            }
        }
        assertEquals(listOf("친구A", "친구B"), sessions[0].tags.map { it.name }.sorted())
        assertEquals(listOf("혼자"), sessions[1].tags.map { it.name })
        assertTrue(sessions[2].tags.isEmpty())
    }

    @Test
    fun seedCanRunTwiceWithoutMergingSessionsOrReusingQrUrls() = runTest {
        DebugDummyDataInjection.seed(
            sessionRepository = sessionRepository,
            mediaStorage = mediaStorage,
            seedId = 100L,
            capturedAt = 1_700_000_000_000L
        )
        DebugDummyDataInjection.seed(
            sessionRepository = sessionRepository,
            mediaStorage = mediaStorage,
            seedId = 200L,
            capturedAt = 1_700_000_100_000L
        )

        val sessions = db.sessionDao().observeSessionsWithDetails().first()
            .sortedBy { it.session.sessionIndexForDay }

        assertEquals(6, sessions.size)
        assertEquals(listOf(1, 2, 3, 4, 5, 6), sessions.map { it.session.sessionIndexForDay })
        assertEquals(6, sessions.map { it.session.sourceQrUrl }.distinct().size)
        sessions.forEach { session ->
            assertEquals(2, session.media.size)
        }
    }
}
