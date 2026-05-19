package com.fourcut.photo.core.media

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
class AppMediaStorageTest {
    @Test
    fun saveOriginalCreatesSessionScopedFile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = AppMediaStorage(context)

        val result = storage.saveOriginal(
            sessionId = 42L,
            fileName = "image_001.jpg",
            input = ByteArrayInputStream("image bytes".toByteArray())
        )

        assertTrue(result.invariantPath().contains("media/sessions/42/original/image_001.jpg"))
        assertTrue(result.exists())
        assertEquals("image bytes", result.readText())
    }

    @Test
    fun saveOriginalSanitizesPathSeparators() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = AppMediaStorage(context)

        val result = storage.saveOriginal(
            sessionId = 43L,
            fileName = "../unsafe\\image.jpg",
            input = ByteArrayInputStream("image bytes".toByteArray())
        )

        assertTrue(result.invariantPath().contains("media/sessions/43/original/.._unsafe_image.jpg"))
        assertTrue(result.exists())
    }

    private fun java.io.File.invariantPath(): String = path.replace('\\', '/')
}
