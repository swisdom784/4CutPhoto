package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoThumbnailDisplayStateTest {
    @Test
    fun localVideoPathCanGenerateThumbnail() {
        val state = buildVideoThumbnailDisplayState(
            source = "C:/app/files/movie.mp4",
            mimeType = "video/mp4",
            thumbnailPath = null
        )

        assertTrue(state.shouldGenerateThumbnail)
        assertTrue(state.showFallbackTile)
    }

    @Test
    fun pickedVideoUriCanGenerateThumbnail() {
        val state = buildVideoThumbnailDisplayState(
            source = "content://sample.invalid/video/1",
            mimeType = "video/mp4",
            thumbnailPath = null
        )

        assertTrue(state.shouldGenerateThumbnail)
    }

    @Test
    fun remoteVideoUsesFallbackWithoutGeneration() {
        val state = buildVideoThumbnailDisplayState(
            source = "https://sample.invalid/video.mp4",
            mimeType = "video/mp4",
            thumbnailPath = null
        )

        assertFalse(state.shouldGenerateThumbnail)
        assertTrue(state.showFallbackTile)
    }

    @Test
    fun thumbnailPathUsesPreviewImageSource() {
        val state = buildVideoThumbnailDisplayState(
            source = "C:/app/files/movie.mp4",
            mimeType = "video/mp4",
            thumbnailPath = "C:/app/cache/thumb.jpg"
        )

        assertEquals("C:/app/cache/thumb.jpg", state.imageModel)
        assertTrue(state.showThumbnailImage)
        assertFalse(state.showFallbackTile)
    }

    @Test
    fun imageMediaKeepsImageDisplaySource() {
        val source = buildPreviewMediaDisplaySource(
            PreviewMedia(
                localPath = "https://sample.invalid/photo.jpg",
                mimeType = "image/jpeg",
                fileName = "photo.jpg"
            )
        )

        assertTrue(source.showImage)
        assertFalse(source.showVideoTile)
    }
}
