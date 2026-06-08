package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PickedMediaImportTest {
    @Test
    fun imageUriBecomesImagePreviewMedia() {
        val media = buildPickedPreviewMedia(
            uri = "content://sample.invalid/photo/1",
            mimeType = "image/jpeg"
        )

        assertEquals("content://sample.invalid/photo/1", media?.localPath)
        assertEquals("image/jpeg", media?.mimeType)
        assertEquals("picked-image.jpg", media?.fileName)
    }

    @Test
    fun videoUriBecomesVideoPreviewMedia() {
        val media = buildPickedPreviewMedia(
            uri = "content://sample.invalid/video/1",
            mimeType = "video/mp4"
        )

        assertEquals("video/mp4", media?.mimeType)
        assertEquals("picked-video.mp4", media?.fileName)
    }

    @Test
    fun unknownMimeTypeIsIgnoredSafely() {
        val media = buildPickedPreviewMedia(
            uri = "content://sample.invalid/file/1",
            mimeType = "application/octet-stream"
        )

        assertNull(media)
    }

    @Test
    fun duplicatePickedItemsAreDeduped() {
        val items = addPickedPreviewMedia(
            current = listOf(
                PreviewMedia("content://sample.invalid/photo/1", "image/jpeg", "picked-image.jpg")
            ),
            picked = listOf(
                PreviewMedia("content://sample.invalid/photo/1", "image/jpeg", "picked-image.jpg"),
                PreviewMedia("content://sample.invalid/video/1", "video/mp4", "picked-video.mp4")
            )
        )

        assertEquals(2, items.size)
    }
}
