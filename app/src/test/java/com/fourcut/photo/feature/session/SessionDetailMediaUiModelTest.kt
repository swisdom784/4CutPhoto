package com.fourcut.photo.feature.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionDetailMediaUiModelTest {
    @Test
    fun imageMimeTypeIsNotVideo() {
        val media = SessionDetailMediaUiModel(
            path = "/media/photo.jpg",
            mimeType = "image/jpeg",
            fileName = "photo.jpg"
        )

        assertFalse(media.isVideo)
        assertEquals("photo.jpg", media.displayName)
    }

    @Test
    fun videoMimeTypeIsVideo() {
        val media = SessionDetailMediaUiModel(
            path = "/media/video.mp4",
            mimeType = "video/mp4",
            fileName = "video.mp4"
        )

        assertTrue(media.isVideo)
        assertEquals("video.mp4", media.displayName)
    }

    @Test
    fun blankFileNameFallsBackToMediaLabel() {
        val media = SessionDetailMediaUiModel(
            path = "/media/video.mp4",
            mimeType = "video/mp4",
            fileName = " "
        )

        assertEquals("Video", media.displayName)
    }
}
