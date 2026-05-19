package com.fourcut.photo.feature.gallery

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GallerySessionUiModelTest {
    @Test
    fun imageCoverCanRenderThumbnailEvenWhenSessionHasVideo() {
        val session = session(coverMimeType = "image/jpeg", hasVideo = true)

        assertTrue(session.hasImageCover)
    }

    @Test
    fun videoCoverDoesNotRenderAsImageThumbnail() {
        val session = session(coverMimeType = "video/mp4", hasVideo = true)

        assertFalse(session.hasImageCover)
    }

    private fun session(
        coverMimeType: String?,
        hasVideo: Boolean
    ): GallerySessionUiModel {
        return GallerySessionUiModel(
            id = 1L,
            sessionTitle = "Session 1",
            timeLabel = "12:00",
            sourceLabel = null,
            coverPath = "/media/cover",
            tagNames = emptyList(),
            hasVideo = hasVideo,
            mediaSummary = "1 photo",
            coverMimeType = coverMimeType
        )
    }
}
