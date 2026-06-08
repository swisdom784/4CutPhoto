package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoThumbnailStoreTest {
    @Test
    fun sameVideoSourceUsesSameCacheKey() {
        val first = buildVideoThumbnailCacheKey("content://sample.invalid/video/1")
        val second = buildVideoThumbnailCacheKey("content://sample.invalid/video/1")

        assertEquals(first, second)
    }

    @Test
    fun thumbnailFileNameIsDeterministicJpg() {
        val fileName = buildVideoThumbnailFileName("content://sample.invalid/video/1")

        assertTrue(fileName.endsWith(".jpg"))
        assertFalse(fileName.contains("content://"))
        assertFalse(fileName.contains("video/1"))
    }

    @Test
    fun localAndRemoteSourcesHaveDifferentCacheKeys() {
        val local = buildVideoThumbnailCacheKey("content://sample.invalid/video/1")
        val remote = buildVideoThumbnailCacheKey("https://sample.invalid/video/1")

        assertFalse(local == remote)
    }
}
