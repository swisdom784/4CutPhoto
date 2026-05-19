package com.fourcut.photo.core.media

import org.junit.Assert.assertEquals
import org.junit.Test

class SystemGalleryExportPlannerTest {
    @Test
    fun imageMediaUsesPicturesCollection() {
        val target = planSystemGalleryExport(
            fileName = "image_001.jpg",
            mimeType = "image/jpeg"
        )

        assertEquals(SystemGalleryCollection.Pictures, target.collection)
        assertEquals("Pictures/4CutPhoto", target.relativePath)
        assertEquals("image_001.jpg", target.displayName)
    }

    @Test
    fun videoMediaUsesMoviesCollection() {
        val target = planSystemGalleryExport(
            fileName = "video_001.mp4",
            mimeType = "video/mp4"
        )

        assertEquals(SystemGalleryCollection.Movies, target.collection)
        assertEquals("Movies/4CutPhoto", target.relativePath)
        assertEquals("video_001.mp4", target.displayName)
    }

    @Test
    fun displayNameRemovesPathSeparators() {
        val target = planSystemGalleryExport(
            fileName = "../unsafe\\image.jpg",
            mimeType = "image/jpeg"
        )

        assertEquals(".._unsafe_image.jpg", target.displayName)
    }

    @Test
    fun blankFileNameFallsBackToTypeSpecificName() {
        val imageTarget = planSystemGalleryExport(fileName = " ", mimeType = "image/jpeg")
        val videoTarget = planSystemGalleryExport(fileName = " ", mimeType = "video/mp4")

        assertEquals("fourcut_photo.jpg", imageTarget.displayName)
        assertEquals("fourcut_video.mp4", videoTarget.displayName)
    }
}
