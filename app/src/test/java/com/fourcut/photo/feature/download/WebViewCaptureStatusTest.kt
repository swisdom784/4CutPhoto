package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Test

class WebViewCaptureStatusTest {
    @Test
    fun waitingStatusGuidesUserToTapDownloadButton() {
        val message = webViewCaptureStatusMessage(WebViewCaptureStatus.Waiting)

        assertEquals("Tap the photo or video download button on the source page.", message)
    }

    @Test
    fun capturedStatusShowsCapturedItemCount() {
        val message = webViewCaptureStatusMessage(WebViewCaptureStatus.Captured(itemCount = 2))

        assertEquals("Captured 2 items. Continue selecting downloads or review the session.", message)
    }

    @Test
    fun ignoredStatusExplainsUnsupportedDownload() {
        val message = webViewCaptureStatusMessage(WebViewCaptureStatus.IgnoredUnsupported)

        assertEquals("That download was not a photo or video.", message)
    }
}
