package com.fourcut.photo.core.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WebViewDownloadCaptureTest {
    @Test
    fun imageMimeTypeCreatesDownloadableMedia() {
        val media = captureWebViewDownload(
            url = "https://example.com/download?id=1",
            contentDisposition = "attachment; filename=\"photo.jpg\"",
            mimeType = "image/jpeg"
        )

        assertEquals(
            DownloadableMedia(
                url = "https://example.com/download?id=1",
                mimeType = "image/jpeg",
                suggestedFileName = "photo.jpg"
            ),
            media
        )
    }

    @Test
    fun missingMimeTypeFallsBackToUrlExtension() {
        val media = captureWebViewDownload(
            url = "https://example.com/video_001.mp4?token=abc",
            contentDisposition = null,
            mimeType = null
        )

        assertEquals("video/mp4", media?.mimeType)
        assertEquals("video_001.mp4", media?.suggestedFileName)
    }

    @Test
    fun unsupportedMimeTypeReturnsNull() {
        val media = captureWebViewDownload(
            url = "https://example.com/receipt.pdf",
            contentDisposition = "attachment; filename=\"receipt.pdf\"",
            mimeType = "application/pdf"
        )

        assertNull(media)
    }
}
