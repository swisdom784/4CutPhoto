package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewMediaDisplaySourceTest {
    @Test
    fun remoteImageUrlCanBeUsedAsDisplaySource() {
        val source = buildPreviewMediaDisplaySource(
            PreviewMedia(
                localPath = "https://sample.invalid/photo.jpg",
                mimeType = "image/jpeg",
                fileName = "photo.jpg"
            )
        )

        assertTrue(source.showImage)
        assertEquals("https://sample.invalid/photo.jpg", source.model)
    }

    @Test
    fun localImagePathUsesLocalPathFirst() {
        val source = buildPreviewMediaDisplaySource(
            PreviewMedia(
                localPath = "C:/app/files/photo.jpg",
                mimeType = "image/jpeg",
                fileName = "photo.jpg"
            )
        )

        assertTrue(source.showImage)
        assertEquals("C:/app/files/photo.jpg", source.model)
    }

    @Test
    fun remoteVideoUsesVideoTileFallback() {
        val source = buildPreviewMediaDisplaySource(
            PreviewMedia(
                localPath = "https://sample.invalid/movie.mp4",
                mimeType = "video/mp4",
                fileName = "movie.mp4"
            )
        )

        assertFalse(source.showImage)
        assertTrue(source.showVideoTile)
    }
}
