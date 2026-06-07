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
        assertTrue(session.hasVideoCover)
    }

    @Test
    fun pickedImageCoverCanRenderThumbnailFromContentUri() {
        val session = session(
            coverMimeType = "image/jpeg",
            hasVideo = false,
            coverPath = "content://sample.invalid/photo/1"
        )

        assertTrue(session.hasImageCover)
    }

    @Test
    fun videoOnlySessionCanShowVideoCoverFallback() {
        val session = session(
            coverMimeType = "video/mp4",
            hasVideo = true,
            coverPath = "content://sample.invalid/video/1"
        )

        assertTrue(session.hasVideoCover)
    }

    private fun session(
        coverMimeType: String?,
        hasVideo: Boolean,
        coverPath: String? = "/media/cover"
    ): GallerySessionUiModel {
        return GallerySessionUiModel(
            id = 1L,
            sessionTitle = "Session 1",
            timeLabel = "12:00",
            sourceLabel = null,
            coverPath = coverPath,
            tagNames = emptyList(),
            hasVideo = hasVideo,
            mediaSummary = "1 photo",
            coverMimeType = coverMimeType
        )
    }
}
