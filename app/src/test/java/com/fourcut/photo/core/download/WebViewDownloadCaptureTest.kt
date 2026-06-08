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
    fun videoMimeTypeCreatesDownloadableMediaWhenFilenameIsMissing() {
        val media = captureWebViewDownload(
            url = "https://example.com/download/video",
            contentDisposition = null,
            mimeType = "video/mp4"
        )

        assertEquals("video/mp4", media?.mimeType)
        assertEquals("download.mp4", media?.suggestedFileName)
    }

    @Test
    fun missingMimeTypeFallsBackToUrlExtension() {
        val media = captureWebViewDownload(
            url = "https://example.com/video_001.mp4?download=sample",
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

    @Test
    fun unknownMimeTypeReturnsNullEvenWhenUrlLooksDownloadable() {
        val media = captureWebViewDownload(
            url = "https://example.com/download?id=media",
            contentDisposition = "attachment; filename=\"media.bin\"",
            mimeType = "application/octet-stream"
        )

        assertNull(media)
    }

    @Test
    fun blobUrlDownloadReturnsNullEvenWithMediaMimeType() {
        val media = captureWebViewDownload(
            url = "blob:https://sample.invalid/generated-video",
            contentDisposition = "attachment; filename=\"clip.mp4\"",
            mimeType = "video/mp4"
        )

        assertNull(media)
    }

    @Test
    fun dataUrlDownloadReturnsNullEvenWithMediaMimeType() {
        val media = captureWebViewDownload(
            url = "data:video/mp4;base64,AAAA",
            contentDisposition = "attachment; filename=\"clip.mp4\"",
            mimeType = "video/mp4"
        )

        assertNull(media)
    }

    @Test
    fun javascriptUrlDownloadReturnsNullEvenWithMediaMimeType() {
        val media = captureWebViewDownload(
            url = "javascript:downloadMedia()",
            contentDisposition = "attachment; filename=\"clip.mp4\"",
            mimeType = "video/mp4"
        )

        assertNull(media)
    }
}
