package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Test

class PreviewMediaListTest {
    @Test
    fun addCapturedMediaAppendsNewItem() {
        val result = addCapturedPreviewMedia(
            current = listOf(
                PreviewMedia("https://example.com/photo.jpg", "image/jpeg", "photo.jpg")
            ),
            captured = PreviewMedia("https://example.com/video.mp4", "video/mp4", "video.mp4")
        )

        assertEquals(2, result.size)
    }

    @Test
    fun addCapturedMediaIgnoresDuplicateUrl() {
        val result = addCapturedPreviewMedia(
            current = listOf(
                PreviewMedia("https://example.com/photo.jpg", "image/jpeg", "photo.jpg")
            ),
            captured = PreviewMedia("https://example.com/photo.jpg", "image/jpeg", "photo.jpg")
        )

        assertEquals(1, result.size)
    }
}
